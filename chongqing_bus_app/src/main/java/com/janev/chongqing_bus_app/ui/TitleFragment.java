package com.janev.chongqing_bus_app.ui;

import android.content.Context;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.UiMessageUtils;
import com.blankj.utilcode.util.Utils;
import com.blankj.utilcode.util.VolumeUtils;
import com.janev.chongqing_bus_app.R;
import com.janev.chongqing_bus_app.databinding.FragmentTitleBinding;
import com.janev.chongqing_bus_app.system.Cache;
import com.janev.chongqing_bus_app.system.NetworkReceiver;
import com.janev.chongqing_bus_app.system.UiEvent;
import com.janev.chongqing_bus_app.utils.VolumeManager;

public class TitleFragment extends BaseFragment<FragmentTitleBinding> implements UiMessageUtils.UiMessageCallback {

    private int maxVolume;
    private NetworkReceiver networkReceiver;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_title;
    }

    @Override
    protected void initView() {
        UiMessageUtils.getInstance().addListener(UiEvent.EVENT_UPDATE_VOLUME,this);
        UiMessageUtils.getInstance().addListener(UiEvent.EVENT_LINE_STAR,this);
        UiMessageUtils.getInstance().addListener(UiEvent.EVENT_WORKER_ID,this);
        UiMessageUtils.getInstance().addListener(UiEvent.EVENT_POLITIC,this);
    }

    @Override
    protected void initData() {
        initVolume();
        initSignal();
    }

    private void initVolume(){
        int volume = VolumeManager.getRealVolume();
        maxVolume = VolumeUtils.getMaxVolume(AudioManager.STREAM_MUSIC);
        if(volume == 0){
            binding.ivVolume.setImageResource(R.mipmap.icon_volume_no);
        } else if(volume <= maxVolume / 2){
            binding.ivVolume.setImageResource(R.mipmap.icon_volume_low);
        } else {
            binding.ivVolume.setImageResource(R.mipmap.icon_volume_high);
        }
        binding.tvVolume.setText(String.valueOf(volume));
    }

    private void initSignal(){
        networkReceiver = new NetworkReceiver(requireContext(), integer -> {
            binding.ivSignal.setImageResource(integer);
        });
    }

    @Override
    public void handleMessage(@NonNull UiMessageUtils.UiMessage localMessage) {
        int id = localMessage.getId();
        if(id == UiEvent.EVENT_UPDATE_VOLUME){
            int[] volumeArray = (int[]) localMessage.getObject();
            int percent = volumeArray[0];
            int volume = volumeArray[1];
            if(volume == 0){
                binding.ivVolume.setImageResource(R.mipmap.icon_volume_no);
            } else if(volume <= maxVolume / 2){
                binding.ivVolume.setImageResource(R.mipmap.icon_volume_low);
            } else {
                binding.ivVolume.setImageResource(R.mipmap.icon_volume_high);
            }
            binding.tvVolume.setText(String.valueOf(percent));
        } else {
            if(id== UiEvent.EVENT_LINE_STAR){
                int starNumber = (int) localMessage.getObject();
                Log.d(TAG, "handleMessage: 线路星级" + starNumber);
                if(starNumber > 0){
                    binding.llStar.setVisibility(View.VISIBLE);
                    binding.tvStar.setText(getStartNumberStr(starNumber) + "星级线路  ");
                    binding.rbStar.setNumStars(starNumber);
                    binding.rbStar.setRating(starNumber);
                } else {
                    binding.llStar.setVisibility(View.GONE);
                }
            } else if(id == UiEvent.EVENT_WORKER_ID){
                String workerId = localMessage.getObject().toString();
                Log.d(TAG, "handleMessage: 车长工号" + workerId);
                if(!TextUtils.isEmpty(workerId)){
                    binding.llWorkerId.setVisibility(View.VISIBLE);
                    binding.tvWorkerId.setText(workerId);
                } else {
                    binding.llWorkerId.setVisibility(View.GONE);
                }
            } else if(id == UiEvent.EVENT_POLITIC){
                boolean isParty = (boolean) localMessage.getObject();
                Log.d(TAG, "handleMessage: 政治面貌" + isParty);
                binding.ivPolitic.setVisibility(isParty ? View.VISIBLE : View.GONE);
            }

            //如果星级或车长工号在显示，则显示背景色，隐藏标题图片
            if(binding.llStar.isShown() || binding.llWorkerId.isShown() || binding.ivPolitic.isShown()){
                binding.vBackground.setVisibility(View.VISIBLE);
                binding.ivTitle.setVisibility(View.GONE);
            } else {
                binding.vBackground.setVisibility(View.GONE);
                binding.ivTitle.setVisibility(View.VISIBLE);
            }
        }
    }

    private String getStartNumberStr(int number){
        switch (number) {
            case 1:
                return "一";
            case 2:
                return "二";
            case 3:
                return "三";
            case 4:
                return "四";
            case 5:
                return "五";
            case 6:
                return "六";
            case 7:
                return "七";
            case 8:
                return "八";
            case 9:
                return "九";
            case 10:
                return "十";
        }
        return number + "";
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(networkReceiver != null){
            networkReceiver.unregister();
        }
    }
}
