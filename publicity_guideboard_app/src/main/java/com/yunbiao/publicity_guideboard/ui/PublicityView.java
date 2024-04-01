package com.yunbiao.publicity_guideboard.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.LogUtils;
import com.bumptech.glide.Glide;
import com.sprylab.android.widget.TextureVideoView;
import com.yunbiao.publicity_guideboard.R;
import com.yunbiao.publicity_guideboard.db.Advert;
import com.yunbiao.publicity_guideboard.system.Cache;
import com.yunbiao.publicity_guideboard.utils.MediaFile;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class PublicityView extends FrameLayout {

    private ImageView imageView;
    private TextureVideoView videoView;
    private final Queue<Advert> playQueue = new LinkedList<>();
    private final int[] IMAGE_TIME_ARRAY;

    public PublicityView(@NonNull Context context) {
        this(context,null);
    }

    public PublicityView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,-1);
    }

    public PublicityView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        IMAGE_TIME_ARRAY = getResources().getIntArray(R.array.image_time);

        initView();
    }

    private void initView(){
        imageView = new ImageView(getContext());
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        addView(imageView,new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT, Gravity.CENTER));

        videoView = new TextureVideoView(getContext());
        addView(videoView,new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT, Gravity.CENTER));
    }

    public void reset(){
        stopPlay();
        playQueue.clear();
        d("重置播放列表");
    }

    public void addAdvert(Advert advert){
        playQueue.add(advert);
        if(!isPlaying()){
            startPlay();
        }
        d("addAdvert,播放列表：" + playQueue.size());
    }

    public void removeAdvert(String number){
        if(TextUtils.isEmpty(number)){
            return;
        }
        d("removeAdvert,Before,播放列表：" + playQueue.size());
        Iterator<Advert> iterator = playQueue.iterator();
        while (iterator.hasNext()) {
            Advert next = iterator.next();
            if(TextUtils.equals(next.getNumber(),number)){
                iterator.remove();
            }
        }
        d("removeAdvert,After,播放列表：" + playQueue.size());
    }

//    public void setAdvertList(List<Advert> list){
//        reset();
//        if(list != null && !list.isEmpty()){
//            d( "播放列表：" + list);
//            playQueue.addAll(list);
//            startPlay();
//        } else {
//            d( "播放列表为空");
//        }
//    }

    private void stopPlay(){
        if(playDisposable != null && !playDisposable.isDisposed()){
            playDisposable.dispose();
            playDisposable = null;
        }
        imageView.setVisibility(View.GONE);
        imageView.setImageBitmap(null);

        videoView.setVisibility(View.GONE);
        videoView.stopPlayback();
    }

    private boolean isPlaying(){
        return playDisposable != null && !playDisposable.isDisposed();
    }

    private static final String TAG = "PublicityView";
    private Disposable playDisposable;
    private void startPlay(){
        if(playDisposable != null && !playDisposable.isDisposed()){
            playDisposable.dispose();
            playDisposable = null;
        }
        playDisposable = Observable
                .create((ObservableOnSubscribe<Integer>) emitter -> {
                    Advert poll = playQueue.poll();
                    playQueue.offer(poll);
                    d( "播放检查：" + poll);
                    //文件路径为空
                    if(poll == null || TextUtils.isEmpty(poll.getLocalPath())){
                        Log.e(TAG, "路径为空");
                        emitter.onNext(-1);
                        emitter.onComplete();
                        return;
                    }
                    //文件不存在
                    if(!FileUtils.isFileExists(poll.getLocalPath())){
                        Log.e(TAG, "文件不存在");
                        emitter.onNext(-2);
                        emitter.onComplete();
                        return;
                    }
                    //检查媒体类型失败
                    MediaFile.MediaFileType fileType = MediaFile.getFileType(poll.getLocalPath());
                    if(fileType == null){
                        Log.e(TAG, "媒体类型检查失败");
                        emitter.onNext(-3);
                        emitter.onComplete();
                        return;
                    }

                    d( "开始播放：" + poll);
                    //视频播放
                    if (MediaFile.isVideoFileType(fileType.fileType)) {
                        d( "播放视频");
                        videoView.setVisibility(View.VISIBLE);
                        videoView.setVideoPath(poll.getLocalPath());
                        videoView.setOnCompletionListener(mp -> {
                            emitter.onNext(1);
                            emitter.onComplete();
                        });
                        videoView.setOnErrorListener((mp, what, extra) -> {
                            d("播放失败：" + what + "," + extra);
                            emitter.onNext(2);
                            emitter.onComplete();
                            return true;
                        });
                        videoView.setOnPreparedListener(MediaPlayer::start);
                        videoView.start();
                        imageView.setVisibility(View.GONE);
                        imageView.setImageBitmap(null);
                    }
                    //图片播放
                    else if(MediaFile.isImageFileType(fileType.fileType)){
                        long imageTime = getImageTime();
                        d( "播放图片:" + imageTime + "毫秒");
                        imageView.setVisibility(View.VISIBLE);
                        Glide.with(PublicityView.this).asBitmap().load(poll.getLocalPath()).into(imageView);
                        imageView.postDelayed(() -> {
                            emitter.onNext(1);
                            emitter.onComplete();
                        },imageTime);
                        videoView.stopPlayback();
                        videoView.setVisibility(View.GONE);
                    }
                    //未知的文件类型
                    else {
                        d( "未知类型");
                        emitter.onNext(0);
                        emitter.onComplete();
                    }
                })
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .repeatWhen(objectObservable -> getRepeatObservable(objectObservable,playQueue))
                .subscribe(o -> {}, t -> {}, () -> {});
    }

    private ObservableSource<?> getRepeatObservable(Observable<Object> objectObservable,Queue<Advert> playQueue){
        return objectObservable.flatMap(o -> {
            d( "检查播放队列");
            if(playQueue.isEmpty()){
                d( "testRepeat: 播放队列为空，播放任务结束");
                return Observable.error(new Exception("播放队列为空"));
            }
            d( "播放列表数量：" + playQueue.size());
            if(playQueue.size() == 1){
                Advert first = playQueue.peek();
                //如果文件存在则直接发送
                if (!TextUtils.isEmpty(first.getLocalPath()) && FileUtils.isFileExists(first.getLocalPath())) {
                    d( "文件存在：立即发送");
                    return Observable.just(o);
                }
                d( "文件不存在：延迟3秒发送");
                //如果文件不存在就延迟3秒发送
                return Observable.timer(3,TimeUnit.SECONDS);
            }

            //使用临时队列检查已存在文件的位置
            Queue<Advert> tempQueue = new LinkedList<>(playQueue);
            while (!tempQueue.isEmpty()) {
                Advert peek = tempQueue.peek();
                if(FileUtils.isFileExists(peek.getLocalPath())){
                    break;
                }
                //如果不存在则出栈
                tempQueue.poll();
            }
            //如果临时队列为空说明一个文件都没有
            if(tempQueue.isEmpty()){
                d( "一个文件都没有：延迟3秒发送");
                return Observable.timer(3,TimeUnit.SECONDS);
            }
            //得到存在的文件的索引，将其移动到队列首位并立即发送
            int notExistsCount = playQueue.size() - tempQueue.size();
            for (int i = 0; i < notExistsCount; i++) {
                playQueue.offer(playQueue.poll());
            }
            d( "文件位置：" + notExistsCount + "，立即发送");
            return Observable.just(o);
        });
    }

    private long getImageTime(){
        int imageTimeIndex = Cache.getInt(Cache.Key.IMAGE_TIME_INDEX, Cache.Default.IMAGE_TIME_INDEX);
        int i = IMAGE_TIME_ARRAY[imageTimeIndex];
        return i * 1000L;
    }

    private void d(String log){
//        LogUtils.d(TAG,TAG + ":" + log);
        Log.d(TAG, log);
    }
}
