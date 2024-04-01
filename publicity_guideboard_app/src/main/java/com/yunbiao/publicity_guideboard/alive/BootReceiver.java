package com.yunbiao.publicity_guideboard.alive;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {
    private static final String ACTION_BOOT_COMPLETED="android.intent.action.BOOT_COMPLETED";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("KeepAlive", "onReceive: 开机启动111");
        /*if(context != null && intent != null && TextUtils.equals(ACTION_BOOT_COMPLETED,intent.getAction())){
//            Intent myIntent = new Intent(context, MainActivity.class);
//            myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            context.startActivity(myIntent);

            if(!ServiceUtils.isServiceRunning(KeepAlive.class)){
                context.startService(new Intent(context,KeepAlive.class));
            }
        }*/
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            Log.e("KeepAlive", "onReceive: 开机启动222");
            intent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }
}
