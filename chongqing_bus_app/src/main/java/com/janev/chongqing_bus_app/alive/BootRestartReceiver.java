package com.janev.chongqing_bus_app.alive;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.ServiceUtils;

public class BootRestartReceiver extends BroadcastReceiver {
    private static final String TAG = "BootRestartReceiver";
    private static final String ACTION_BOOT_COMPLETED="android.intent.action.BOOT_COMPLETED";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e(TAG, "onReceive: 开机启动111");
//        if(ACTION_BOOT_COMPLETED.equals(intent.getAction())){
//            Log.e("KeepAlive", "onReceive: 开机启动222");
//            AppUtils.launchApp(AppUtils.getAppPackageName());
//        }
//        if (intent.getAction().equals(ACTION_BOOT_COMPLETED)) {
//            Log.e("KeepAlive", "onReceive: 开机启动222");
//            intent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
//            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//            context.startActivity(intent);
//        }

        //启动保活服务
        if(!ServiceUtils.isServiceRunning(KeepAlive.class)){
            ServiceUtils.startService(KeepAlive.class);
        }

    }
}
