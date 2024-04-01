package com.janev.chongqing_bus_app.ui;

import android.content.Intent;
import android.media.MediaPlayer;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import androidx.annotation.NonNull;

import com.blankj.utilcode.constant.PermissionConstants;
import com.blankj.utilcode.util.FileIOUtils;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.PathUtils;
import com.blankj.utilcode.util.PermissionUtils;
import com.bumptech.glide.Glide;
import com.janev.chongqing_bus_app.App;
import com.janev.chongqing_bus_app.R;
import com.janev.chongqing_bus_app.databinding.ActivitySplashBinding;
import com.janev.chongqing_bus_app.db.DaoManager;
import com.janev.chongqing_bus_app.db.StartUpLogo;
import com.janev.chongqing_bus_app.system.Cache;
import com.janev.chongqing_bus_app.system.Path;
import com.janev.chongqing_bus_app.tcp.message.MessageUtils;
import com.janev.chongqing_bus_app.tcp.task.appResource.AppResourceManager2;
import com.janev.chongqing_bus_app.utils.L;
import com.janev.chongqing_bus_app.utils.MediaFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class SplashActivity extends BaseActivity<ActivitySplashBinding> {
    private final String[] PERMISSIONS = {
//            PermissionConstants.LOCATION,
            PermissionConstants.PHONE,
            PermissionConstants.STORAGE,
            PermissionConstants.MICROPHONE
    };

    @Override
    protected int getLayoutId() {
        return R.layout.activity_splash;
    }

    @Override
    protected void initView() {
        PermissionUtils.permissionGroup(PERMISSIONS).callback(new PermissionUtils.FullCallback() {
            @Override
            public void onGranted(@NonNull List<String> granted) {
                //初始化路径
                Path.init();

                //初始化缓存
                Cache.init();

                //初始化数据库
                DaoManager.get().initDB(App.getContext(),Path.getDatabasePath());

                L.setLogSwitch(true);
                L.clean();

                FileUtils.deleteFilesInDir(Path.getTempDir());//清除临时文件夹
                FileUtils.deleteFilesInDir(Path.getScreenShotPath());//清除截屏文件夹

                MessageUtils.getProductDate();

                StartUpLogo startUpLogo = AppResourceManager2.getInstance().getStartUpLogo();
                if(startUpLogo == null){
                    String defaultStartup = getDefaultStartup();
                    if(!TextUtils.isEmpty(defaultStartup)){
                        playVideo(defaultStartup);
                    } else {
                        jump();
                    }
                } else {
                    String startUpLogoPath = Path.getStartUpLogoPath(startUpLogo);
                    if(!FileUtils.isFileExists(startUpLogoPath)){
                        String defaultStartup = getDefaultStartup();
                        if(!TextUtils.isEmpty(defaultStartup)){
                            playVideo(defaultStartup);
                        } else {
                            jump();
                        }
                    } else {
                        MediaFile.MediaFileType fileType = MediaFile.getFileType(startUpLogoPath);
                        if(fileType != null && MediaFile.isVideoFileType(fileType.fileType)){
                            playVideo(startUpLogoPath);
                        } else if(fileType != null && MediaFile.isImageFileType(fileType.fileType)){
                            playImage(startUpLogoPath);
                        } else {
                            jump();
                        }
                    }
                }
            }

            @Override
            public void onDenied(@NonNull List<String> deniedForever, @NonNull List<String> denied) {

            }
        }).request();
    }

    @Override
    protected void initData() {

    }

    private void playVideo(String path){
        binding.videoView.setVisibility(View.VISIBLE);
        binding.videoView.setVideoPath(path);
        binding.videoView.setOnPreparedListener(MediaPlayer::start);
        binding.videoView.setOnErrorListener((mediaPlayer, i, i1) -> {
            jump();
            return true;
        });
        binding.videoView.setOnCompletionListener(mediaPlayer -> jump());
        binding.videoView.start();
    }

    private void playImage(String path){
        Disposable subscribe = Observable
                .timer(5, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(disposable -> {
                    binding.imageView.setVisibility(View.VISIBLE);
                    Glide.with(SplashActivity.this).asBitmap().load(path).into(binding.imageView);
                })
                .subscribe(aLong -> jump());
    }

    private void jump(){
        startActivity(new Intent(this,MainActivity.class));
        finish();
    }

    private String getDefaultStartup(){
        File file = new File(PathUtils.getExternalAppCachePath(), "start_video.mp4");
        if (FileUtils.isFileExists(file)) {
            return file.getPath();
        }
        try {
            if (FileIOUtils.writeFileFromIS(file, getAssets().open("start_video.mp4"))) {
                return file.getPath();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return true;
    }
}