package com.janev.chongqing_bus_app.ui;

import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.FragmentUtils;
import com.blankj.utilcode.util.UiMessageUtils;
import com.bumptech.glide.Glide;
import com.janev.chongqing_bus_app.R;
import com.janev.chongqing_bus_app.adapter.CenterLinearLayoutManager;
import com.janev.chongqing_bus_app.adapter.SiteListAdapter;
import com.janev.chongqing_bus_app.adapter.UpdateChildScrollListener;
import com.janev.chongqing_bus_app.databinding.FragmentSiteBinding;
import com.janev.chongqing_bus_app.db.StationPicture;
import com.janev.chongqing_bus_app.serial.ChongqingV1Handler;
import com.janev.chongqing_bus_app.system.Path;
import com.janev.chongqing_bus_app.system.UiEvent;
import com.janev.chongqing_bus_app.tcp.message.MessageUtils;
import com.janev.chongqing_bus_app.tcp.task.appResource.AppResourceManager2;
import com.janev.chongqing_bus_app.utils.MediaFile;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class SiteFragment extends BaseFragment<FragmentSiteBinding> implements UiMessageUtils.UiMessageCallback {
    @Override
    protected int getLayoutId() {
        return R.layout.fragment_site;
    }

    @Override
    protected void initView() {
        UiMessageUtils.getInstance().addListener(this);

        binding.rlvSite.setLayoutManager(new CenterLinearLayoutManager(requireContext()));
        binding.rlvSite.setAdapter(new SiteListAdapter());
        binding.rlvSite.addOnScrollListener(new UpdateChildScrollListener());
    }

    @Override
    protected void initData() {
    }

    private StationPicture currPlayStationPicture;
    @Override
    public void handleMessage(@NonNull UiMessageUtils.UiMessage localMessage) {
        SiteListAdapter siteListAdapter = (SiteListAdapter)binding.rlvSite.getAdapter();
        if(siteListAdapter == null){
            return;
        }
        switch (localMessage.getId()) {
            case UiEvent.EVENT_SITE_LIST:
                ChongqingV1Handler.SiteList siteList = (ChongqingV1Handler.SiteList) localMessage.getObject();
                siteListAdapter.setData(siteList.getList());
                binding.rlvSite.smoothScrollToPosition(0);
                break;
            case UiEvent.EVENT_BROAD_SITE:
                int[] array = (int[]) localMessage.getObject();
                int upDown = array[0];
                int inOut = array[1];
                int index = array[2];

                //进站
                if (inOut == 0) {
                    siteListAdapter.pullIn(binding.rlvSite,index);

                    //如果按了上一站，则立即停止播放
                    if(this.currPlayStationPicture != null && index < this.currPlayStationPicture.getStationNum()){
                        clearPlay();
                    }

                    //duration为0，进站时清除
                    if(isPlayToPullIn()){
                        clearPlay();
                    }

                    //检查播放内容
                    String lineName = MessageUtils.getLineName();

                    StationPicture stationPicture = AppResourceManager2.getInstance().getStationPicture(lineName, upDown, index);
                    clearPlay();
                    if(stationPicture != null){
                        play(stationPicture);
                        if(stationPicture.getDuration() > 1){
                            delayRun(stationPicture.getDuration(), this::clearPlay);
                        }
                    }
                }
                //出站
                else {
                    siteListAdapter.pullOut(binding.rlvSite,index);

                    //持续播放到车辆离站，用于处理duration为1的情况
                    if(isPlayToPullOut()){
                        clearPlay();
                    }
                }
                break;
            case UiEvent.EVENT_SCREEN_BROAD_SITE:
                String[] siteArray = (String[]) localMessage.getObject();
                showBroadSite(siteArray[0],siteArray[1],siteArray[2]);
                break;
        }
    }

    private boolean isPlayToPullIn(){
        return this.currPlayStationPicture != null && this.currPlayStationPicture.getDuration() == 0;
    }

    private boolean isPlayToPullOut(){
        return this.currPlayStationPicture != null && this.currPlayStationPicture.getDuration() == 1;
    }

    private void play(StationPicture picture){
        this.currPlayStationPicture = picture;
        String stationPicturePath = Path.getStationPicturePath(this.currPlayStationPicture);
        if(FileUtils.isFileExists(stationPicturePath)){
            MediaFile.MediaFileType fileType = MediaFile.getFileType(stationPicturePath);
            if(MediaFile.isVideoFileType(fileType.fileType)){
                playVideo(stationPicturePath);
            } else if(MediaFile.isImageFileType(fileType.fileType)){
                playImage(stationPicturePath);
            }
        } else {
            clearPlay();
        }
    }

    private void playVideo(String path){
        binding.videoView.setVisibility(View.VISIBLE);
        binding.videoView.setVideoPath(path);
        binding.videoView.setOnCompletionListener(mediaPlayer -> {
            Log.d(TAG,"播放完成：" + path);
        });
        binding.videoView.setOnErrorListener((mediaPlayer, i, i1) -> {
            Log.d(TAG,"播放失败：" + path + "[" + i + "," + i1 + "]");
            return true;
        });
        binding.videoView.setOnPreparedListener(mediaPlayer -> {
            Log.d(TAG,"播放准备完毕：" + path);
            mediaPlayer.setLooping(true);
            mediaPlayer.start();
        });
        binding.videoView.start();
        Log.d(TAG,"是视频，开始播放：" + path);
    }

    private void stopVideo(){
        binding.videoView.setVisibility(View.GONE);
        binding.videoView.stopPlayback();
    }

    private void playImage(String path){
        binding.imageView.setVisibility(View.VISIBLE);
        Glide.with(this).asBitmap().load(path).into(binding.imageView);
    }

    private void stopImage(){
        binding.imageView.setImageBitmap(null);
        binding.imageView.setVisibility(View.GONE);
    }

    private void clearPlay(){
        stopVideo();
        stopImage();
        this.currPlayStationPicture = null;
        disposeDelay();
    }

    private Disposable disposable;
    private void delayRun(long seconds,Runnable runnable){
        disposable = Observable
                .timer(seconds, TimeUnit.SECONDS)
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> {
                    runnable.run();
                });
    }

    private void disposeDelay(){
        if(disposable != null && !disposable.isDisposed()){
            disposable.dispose();
            disposable = null;
        }
    }

    private BroadSiteFragment broadSiteFragment;
    private void showBroadSite(String site,String nextSite,String isResponsiveStr){
        if(broadSiteFragment != null && broadSiteFragment.isAdded()){
            broadSiteFragment.update(site,nextSite,isResponsiveStr);
        } else {
            broadSiteFragment = BroadSiteFragment.newInstance(site,nextSite,isResponsiveStr);
            if(broadSiteFragment != null){
                FragmentUtils.add(getChildFragmentManager(),broadSiteFragment,binding.flBroadContainer.getId(),R.anim.anim_site_enter,R.anim.anim_site_exit);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        disposeDelay();
    }
}
