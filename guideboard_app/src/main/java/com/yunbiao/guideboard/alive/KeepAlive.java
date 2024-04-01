package com.yunbiao.guideboard.alive;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;

import com.blankj.utilcode.util.AppUtils;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class KeepAlive extends Service {

    private ScheduledExecutorService singleScheduled;
    private KeepAliveRunnable keepAliveRunnable;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startKeepAlive();
        return super.onStartCommand(intent, flags, startId);
    }

    private void startKeepAlive(){
        if(singleScheduled != null && !singleScheduled.isShutdown()){
            singleScheduled.shutdownNow();
            singleScheduled = null;
        }
        singleScheduled = Executors.newSingleThreadScheduledExecutor();
        if(keepAliveRunnable == null){
            String appPackageName = AppUtils.getAppPackageName();
            Log.e("KeepAlive", "startKeepAlive: " + appPackageName);
            keepAliveRunnable = new KeepAliveRunnable(this,appPackageName,singleScheduled);
        };
        singleScheduled.submit(keepAliveRunnable);
    }

    static class KeepAliveRunnable implements Runnable{
        private static final String TAG = "KeepAliveRunnable";

        private Context context;
        private String packageName;
        private ScheduledExecutorService singleScheduled;
        private int waiting = 0;

        public KeepAliveRunnable(Context context, String packageName, ScheduledExecutorService singleScheduled) {
            this.context = context;
            this.packageName = packageName;
            this.singleScheduled = singleScheduled;
        }

        @Override
        public void run() {
            Log.e(TAG, "run: 1111111111111111111111");
            if(TextUtils.isEmpty(packageName)){
                return;
            }
            // 检测应用是否在最上层，没有则重新打开至上层
            if (!AppUtils.isAppRunning(packageName) || !AppUtils.isAppForeground(packageName)) {
                AppUtils.launchApp(packageName);
            }

            if(singleScheduled != null && !singleScheduled.isShutdown()){
                singleScheduled.schedule(this,10000,TimeUnit.MILLISECONDS);
            }
            waiting ++;
        }

        private int waiting(){
           return waiting > 12 ? 15 :
                    waiting > 7  ? 10 :
                    waiting <= 3 ? 3 : 5;
        }
    }

    @Override
    public void onDestroy() {
        if(singleScheduled != null && !singleScheduled.isShutdown()){
            singleScheduled.shutdownNow();
            singleScheduled = null;
        }
        keepAliveRunnable = null;
        super.onDestroy();
    }
}
