package com.janev.chongqing_bus_app.alive;

import android.app.Service;
import android.app.smdt.SmdtManager;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;

import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.FileIOUtils;
import com.blankj.utilcode.util.FileUtils;
import com.janev.chongqing_bus_app.App;

import java.io.File;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class KeepAlive extends Service {
    private static final String TAG = "KeepAlive";

    private int launchTime = 0;
    private Disposable keepAliveDisposable;
    private final int LAUNCH_TIME = 6;
    public static final String AUTO_LAUNCH_FLAG = new File(Environment.getExternalStorageDirectory(),"chongqingbusautolaunch").getPath();
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(!FileUtils.isFileExists(AUTO_LAUNCH_FLAG)){
            boolean orExistsFile = FileUtils.createOrExistsFile(AUTO_LAUNCH_FLAG);
            if(orExistsFile){
                FileIOUtils.writeFileFromString(AUTO_LAUNCH_FLAG,"1",false);
            }
        }

        Log.e(TAG, "startKeepAlive: 启用看门狗");
        SmdtManager.create(App.getContext()).smdtWatchDogEnable('1');
        startKeepAlive();
        return super.onStartCommand(intent, flags, startId);
    }

    private void startKeepAlive(){
        if (keepAliveDisposable != null && !keepAliveDisposable.isDisposed()) {
            return;
        }
        keepAliveDisposable = Observable
                .interval(3,10,TimeUnit.SECONDS)
                .observeOn(Schedulers.single())
                .subscribeOn(Schedulers.single())
                .doOnSubscribe(disposable -> {
                    boolean autoLaunch = false;
                    if(FileUtils.isFileExists(AUTO_LAUNCH_FLAG)){
                        String s = FileIOUtils.readFile2String(AUTO_LAUNCH_FLAG);
                        autoLaunch = !TextUtils.isEmpty(s) && TextUtils.equals("1",s);
                    }

                    if(autoLaunch){
                        String appPackageName = AppUtils.getAppPackageName();
                        AppUtils.launchApp(appPackageName);
                    }
                })
                .doOnNext(aLong -> {
                    Log.e(TAG, "startKeepAlive: 喂狗");
                    //喂狗
                    SmdtManager.create(App.getContext()).smdtWatchDogFeed();

                    boolean autoLaunch = false;
                    if(FileUtils.isFileExists(AUTO_LAUNCH_FLAG)){
                        String s = FileIOUtils.readFile2String(AUTO_LAUNCH_FLAG);
                        autoLaunch = !TextUtils.isEmpty(s) && TextUtils.equals("1",s);
                    }

                    String appPackageName = AppUtils.getAppPackageName();
                    if(!autoLaunch){
                        Log.d(TAG, "startKeepAlive: 不自启");
                        launchTime = 0;
                    }
                    // 检测应用是否在最上层，没有则重新打开至上层
                    else if(AppUtils.isAppRunning(appPackageName) && AppUtils.isAppForeground(appPackageName)){
                        Log.d(TAG, "startKeepAlive: 已启动");
                        launchTime = 0;
                    }
                    else if(launchTime >= LAUNCH_TIME){
                        Log.e(TAG, "startKeepAlive: 启动应用");
                        AppUtils.launchApp(appPackageName);
                        launchTime = 0;
                    }
                    else {
                        launchTime ++;
                        Log.d(TAG, "startKeepAlive: 累计时长：" + launchTime);
                    }
                })
                .subscribe();
    }

    private void dispose(){
        if(keepAliveDisposable != null && !keepAliveDisposable.isDisposed()){
            keepAliveDisposable.dispose();
            keepAliveDisposable = null;
        }
    }

    @Override
    public void onDestroy() {
        dispose();
        Log.e(TAG, "startKeepAlive: 关闭看门狗");
        SmdtManager.create(App.getContext()).smdtWatchDogEnable('0');
        super.onDestroy();
    }
}
