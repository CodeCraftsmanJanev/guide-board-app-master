package com.janev.chongqing_bus_app.tcp.task.resource;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.UiMessageUtils;
import com.blankj.utilcode.util.Utils;
import com.bumptech.glide.Glide;
import com.janev.chongqing_bus_app.R;
import com.janev.chongqing_bus_app.db.DaoManager;
import com.janev.chongqing_bus_app.db.Material;
import com.janev.chongqing_bus_app.db.Program;
import com.janev.chongqing_bus_app.db.Time;
import com.janev.chongqing_bus_app.system.Cache;
import com.janev.chongqing_bus_app.system.Path;
import com.janev.chongqing_bus_app.system.UiEvent;
import com.janev.chongqing_bus_app.utils.MediaFile;
import com.janev.chongqing_bus_app.utils.ProgramLoader;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class DatePlayView extends FrameLayout implements UiMessageUtils.UiMessageCallback {

    private static final String KEY_CURR_MATERIAL = "KEY_CURR_MATERIAL";
//    private boolean playCache = false;

    public DatePlayView(@NonNull Context context) {
        this(context, null);
    }

    public DatePlayView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public DatePlayView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        UiMessageUtils.getInstance().addListener(UiEvent.UPDATE_RESOURCE, this);
        showDefaultImage();
    }

    @Override
    public void handleMessage(@NonNull UiMessageUtils.UiMessage localMessage) {
        init();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopPlay();
        UiMessageUtils.getInstance().removeListener(UiEvent.UPDATE_RESOURCE, this);
    }


    public static long getCurrMaterialId(){
        return Cache.getLong(KEY_CURR_MATERIAL,0L);
    }

    private static void setCurrMaterialId(long _id){
        if(_id < 0){
            Cache.remove(KEY_CURR_MATERIAL);
        } else {
            Cache.setLong(KEY_CURR_MATERIAL,_id);
        }
    }

//    public void setPlayCache(boolean playCache) {
//        this.playCache = playCache;
//    }

//    public boolean isPlayCache() {
//        return playCache;
//    }

    public void init() {
        NetworkUtils.isAvailableAsync(aBoolean -> {
//            setPlayCache(!aBoolean);
            startPlay();
        });
    }

    //筛选符合条件的数据
    private final LinkedHashMap<Material, List<Time>> materialMap = new LinkedHashMap<>();
    private static final String TAG = "DatePlayView";
    private Disposable playDisposable;
    private void startPlay() {
        stopPlay();

        materialMap.clear();
//        if (isPlayCache()) {
//            Log.d(TAG, "加载缓存节目单");
//            materialMap.putAll(ProgramLoader.loadCacheProgram());
//        } else {
            Log.d(TAG, "加载播放节目单");
            materialMap.putAll(ProgramLoader.loadPlayProgram());
//        }
        Log.d(TAG, "节目单数量：" + materialMap.size());

        if(materialMap.isEmpty()){
            Log.d(TAG, "没有节目");
            return;
        }

        Log.d(TAG, "开始播放队列");
        playDisposable = Observable
                .fromIterable(materialMap.entrySet())
                .concatMap(materialListEntry -> {
                    Observable<Integer> integerObservable = playProgram(materialListEntry);;
//                    if (playCache) {
//                        integerObservable = playCache(materialListEntry);
//                    } else {
//                        integerObservable = playProgram(materialListEntry);
//                    }
                    return integerObservable
                            .onErrorResumeNext(Observable.just(-1));
                })
                .repeatWhen(objectObservable -> objectObservable.delay(3, TimeUnit.SECONDS))
                .retryWhen(throwableObservable ->
                        throwableObservable.flatMap(throwable ->
                                Observable.timer(3,TimeUnit.SECONDS)
                        )
                )
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        integer -> {
                        },
                        throwable -> {
                            Log.e(TAG, "startPlay: ", throwable);
                        }
                );
    }

    private void stopPlay() {
        setCurrMaterialId(-1);
        if (playDisposable != null && !playDisposable.isDisposed()) {
            Log.d(TAG, "stopPlay: 结束播放");
            playDisposable.dispose();
            playDisposable = null;
        }
        materialMap.clear();
        showDefaultImage();
    }

    //素材播放完成
    private static final int MATERIAL_PLAY_COMPLETE = 0;
    //素材不存在
    private static final int MATERIAL_NOT_EXISTS = -1;
    //不在有效期
    private static final int MATERIAL_NOT_EXPIRE = -2;
    //格式不支持
    private static final int MATERIAL_NOT_SUPPORT = -3;
    //素材已达最大次数
    private static final int MATERIAL_TOTAL_TIMES = -4;
    //素材已达最大时长
    private static final int MATERIAL_TOTAL_DURATION = -5;
    //时段已达最大次数
    private static final int MATERIAL_TIME_TOTAL_TIMES = -4;
    //时段已达最大时长
    private static final int MATERIAL_TIME_TOTAL_DURATION = -5;
    //素材播放错误
    private static final int MATERIAL_PLAY_ERROR = -6;

    private Observable<Integer> playProgram(Map.Entry<Material, List<Time>> entry) {
        return Observable.create(emitter -> {
            Material material = entry.getKey();
            Log.d(TAG, "检查素材：" + material.getId() + " ---------------------------------------- ");

            String materialPath = Path.getMaterialPath(material);
            //素材文件不存在
            if(TextUtils.isEmpty(materialPath) || !FileUtils.isFileExists(materialPath)){
                emitter.onNext(MATERIAL_NOT_EXISTS);
                emitter.onComplete();
                return;
            }

            //未知类型
            MediaFile.MediaFileType fileType = MediaFile.getFileType(materialPath);
            if(fileType == null){
                emitter.onNext(MATERIAL_NOT_SUPPORT);
                emitter.onComplete();
                return;
            }

            //素材最大次数
            if (material.getTotalTimes() > 0 && material.getPlayTotalTimes() >= material.getTotalTimes()) {
                Log.d(TAG, "素材已达最大次数");
                emitter.onNext(MATERIAL_TOTAL_TIMES);
                emitter.onComplete();
                return;
            }

            //素材最大时长
            if (material.getTotalDuration() > 0 && material.getPlayTotalDuration() >= material.getTotalDuration()) {
                Log.d(TAG, "素材已达最大时长");
                emitter.onNext(MATERIAL_TOTAL_DURATION);
                emitter.onComplete();
                return;
            }

            //检查时间段
            Date currTime = ResourceManager2.getCurrTime();

            //筛选当前符合时段的Time
            Time okTime = null;
            for (Time t : entry.getValue()) {
                if (t.getStartTime() <= currTime.getTime() && t.getEndTime() > currTime.getTime()) {
                    okTime = t;
                    break;
                }
            }
            //没有符合要求的时间段
            if (okTime == null) {
                Log.d(TAG, "没有可用时间段");
                emitter.onNext(MATERIAL_NOT_EXPIRE);
                emitter.onComplete();
                return;
            }

            //播放已达最大次数
            if (okTime.getTotalTimes() > 0 && okTime.getPlayTotalTimes() >= okTime.getTotalTimes()) {
                Log.d(TAG, "时段已达最大次数");
                emitter.onNext(MATERIAL_TIME_TOTAL_TIMES);
                emitter.onComplete();
                return;
            }

            //播放已达最大时长
            if (okTime.getTotalDuration() > 0 && okTime.getPlayTotalTimes() >= okTime.getTotalDuration()) {
                Log.d(TAG, "时段已达最大时长");
                emitter.onNext(MATERIAL_TIME_TOTAL_DURATION);
                emitter.onComplete();
                return;
            }

            Time time = okTime;
            Log.d(TAG, "时段：" + ResourceManager2.time2String(time.getStartTime()) + " --- " + ResourceManager2.time2String(time.getEndTime()));

            //素材
            AtomicLong materialPlayTotalTimes = new AtomicLong(material.getPlayTotalTimes());
            AtomicLong materialPlayTotalDuration = new AtomicLong(material.getPlayTotalDuration());

            //时段
            AtomicLong timePlayTotalTimes = new AtomicLong(time.getPlayTotalTimes());
            AtomicLong timePlayTotalDuration = new AtomicLong(time.getPlayTotalDuration());

            //重播次数
            AtomicLong repeatTimes = new AtomicLong(1);

            Log.d(TAG, "开始播放：" + material.getId());

            setCurrMaterialId(material.get_id());

            if(MediaFile.isVideoFileType(fileType.fileType)){
                Log.d(TAG, "视频");
                showVideoView(
                        materialPath,
                        ints -> {
                            emitter.onNext(MATERIAL_PLAY_ERROR);
                            emitter.onComplete();
                        },
                        videoView -> {
                            //累加播放次数
                            material.setPlayTotalTimes(materialPlayTotalTimes.incrementAndGet());
                            time.setPlayTotalTimes(timePlayTotalTimes.incrementAndGet());

                            //累加播放时长
                            int playDuration = videoView.getDuration() / 1000;
                            material.setPlayTotalDuration(materialPlayTotalDuration.addAndGet(playDuration));
                            time.setPlayTotalDuration(timePlayTotalDuration.addAndGet(playDuration));

                            DaoManager.get().update(material);
                            DaoManager.get().update(time);

                            //超出素材总播放次数
                            if (material.getPlayTotalTimes() >= material.getTotalTimes()) {
                                videoView.stopPlayback();

                                emitter.onNext(MATERIAL_PLAY_COMPLETE);
                                emitter.onComplete();
                            }

                            //超出素材总播放时长
                            else if (material.getPlayTotalDuration() >= material.getTotalDuration()) {
                                videoView.stopPlayback();

                                emitter.onNext(MATERIAL_PLAY_COMPLETE);
                                emitter.onComplete();
                            }

                            //超出时段总播放次数
                            else if (time.getPlayTotalTimes() >= time.getTotalTimes()) {
                                videoView.stopPlayback();

                                emitter.onNext(MATERIAL_PLAY_COMPLETE);
                                emitter.onComplete();
                            }

                            //超出时段总播放时长
                            else if (time.getPlayTotalDuration() >= time.getTotalDuration()) {
                                videoView.stopPlayback();

                                emitter.onNext(MATERIAL_PLAY_COMPLETE);
                                emitter.onComplete();
                            }

                            //如果循环次数为0
                            else if (time.getRepeatTimes() == 0) {
                                videoView.stopPlayback();

                                emitter.onNext(MATERIAL_PLAY_COMPLETE);
                                emitter.onComplete();
                            }

                            //超出循环次数则跳过
                            else if (repeatTimes.incrementAndGet() >= time.getRepeatTimes()) {
                                videoView.stopPlayback();

                                emitter.onNext(MATERIAL_PLAY_COMPLETE);
                                emitter.onComplete();
                            }
                        });
            } else if(MediaFile.isImageFileType(fileType.fileType)){
                Log.d(TAG, "图片");
                showImage(materialPath,time.getDuration() == 0 ? 10 : time.getDuration(),aLong -> {
                    //累加播放次数
                    material.setPlayTotalTimes(materialPlayTotalTimes.incrementAndGet());
                    time.setPlayTotalTimes(timePlayTotalTimes.incrementAndGet());

                    //累加播放时长
                    material.setPlayTotalDuration(materialPlayTotalDuration.addAndGet(aLong));
                    time.setPlayTotalDuration(timePlayTotalDuration.addAndGet(aLong));

                    DaoManager.get().update(material);
                    DaoManager.get().update(time);

                    emitter.onNext(MATERIAL_PLAY_COMPLETE);
                    emitter.onComplete();
                });
            } else {
                Log.d(TAG, "不支持");
                emitter.onNext(MATERIAL_NOT_SUPPORT);
                emitter.onComplete();
            }
        });
    }

//    private Observable<Integer> playCache(Map.Entry<Material, List<Time>> entry) {
//        return Observable
//                .create(emitter -> {
//                    Material material = entry.getKey();
//                    String materialPath = Path.getMaterialPath(material);
//                    //素材文件不存在
//                    if (TextUtils.isEmpty(materialPath) || !FileUtils.isFileExists(materialPath)) {
//                        emitter.onNext(MATERIAL_NOT_EXISTS);
//                        emitter.onComplete();
//                        return;
//                    }
//
//                    //未知类型
//                    MediaFile.MediaFileType fileType = MediaFile.getFileType(materialPath);
//                    if (fileType == null) {
//                        emitter.onNext(MATERIAL_NOT_SUPPORT);
//                        emitter.onComplete();
//                        return;
//                    }
//
//                    if (MediaFile.isVideoFileType(fileType.fileType)) {
//                        showVideoView(materialPath, ints -> {
//                            emitter.onNext(MATERIAL_PLAY_ERROR);
//                            emitter.onComplete();
//                        }, videoView -> {
//                            emitter.onNext(MATERIAL_PLAY_COMPLETE);
//                            emitter.onComplete();
//                        });
//                    } else if (MediaFile.isImageFileType(fileType.fileType)) {
//                        showImage(materialPath,10,aLong -> {
//                            emitter.onNext(MATERIAL_PLAY_COMPLETE);
//                            emitter.onComplete();
//                        });
//                    }
//                });
//    }

    private void showDefaultImage() {
        post(() -> {
            setBackgroundColor(Color.WHITE);
            ImageView imageView = getImageView();
            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            imageView.setImageResource(R.mipmap.icon_left_default);
        });
    }

    private void showImage(String materialPath,long seconds,Consumer<Long> consumer) {
        post(() -> {
            setBackgroundColor(Color.BLACK);
            ImageView imageView = getImageView();
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            Glide.with(imageView.getContext()).load(materialPath).into(imageView);
            timer(seconds,consumer);
        });
    }

    private void showVideoView(String materialPath, Utils.Consumer<int[]> onErrorListener, Utils.Consumer<VideoView> onCompletionListener){
        post(() -> {
            setBackgroundColor(Color.BLACK);
            VideoView videoView = getVideoView();
            videoView.setOnPreparedListener(mp -> {
                mp.setLooping(true);
                mp.start();
            });
            videoView.setOnErrorListener((mp, what, extra) -> {
                if(onErrorListener != null){
                    onErrorListener.accept(new int[]{what,extra});
                }
                return true;
            });
            videoView.setOnCompletionListener(mp -> {
                if(onCompletionListener != null){
                    onCompletionListener.accept(videoView);
                }
            });
            videoView.setVideoPath(materialPath);
            videoView.start();
        });
    }

    private ImageView getImageView(){
        ImageView imageView = null;

        for (int i = 0; i < getChildCount(); i++) {
            View childAt = getChildAt(i);
            if (childAt instanceof ImageView) {
                imageView = (ImageView) childAt;
            } else {
                if (childAt instanceof VideoView) {
                    VideoView videoView = (VideoView) childAt;
                    videoView.stopPlayback();
                }
                removeView(childAt);
            }
        }

        if (imageView == null) {
            imageView = new ImageView(getContext());

            LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, Gravity.CENTER);
            addView(imageView, layoutParams);
        }
        return imageView;
    }

    private VideoView getVideoView() {
        VideoView videoView = null;

        for (int i = 0; i < getChildCount(); i++) {
            View childAt = getChildAt(i);
            if (childAt instanceof VideoView) {
                videoView = (VideoView) childAt;
            } else {
                removeView(childAt);
            }
        }

        if (videoView == null) {
            videoView = new VideoView(getContext());
            LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, Gravity.CENTER);
            addView(videoView, layoutParams);
        }

        return videoView;
    }

    private Disposable timerDisposable;

    private void timer(long seconds, Consumer<Long> consumer) {
        if (timerDisposable != null && !timerDisposable.isDisposed()) {
            timerDisposable.dispose();
            timerDisposable = null;
        }
        timerDisposable = Observable
                .timer(seconds, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.trampoline())
                .observeOn(Schedulers.trampoline())
                .subscribe(consumer);
    }

}
