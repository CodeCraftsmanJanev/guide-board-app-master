package com.janev.chongqing_bus_app.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.janev.chongqing_bus_app.ui.SplashActivity;

public class SystemEventReceiver extends BroadcastReceiver {

    private static final String TAG = ">>>>>>SystemReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction().toString();
        switch (action) {
            case "wits.action.reboot":
                Log.i(TAG, "wits.action.reboot");
                break;
            case "wits.action.shutdown":
                Log.i(TAG, "wits.action.shutdown");
                break;
            case "android.intent.action.BOOT_COMPLETED":
                Log.i(TAG, "android.intent.action.BOOT_COMPLETED");
                break;
            case "RestartSerivcesForSystemEventReceiver":
                Log.i(TAG, "RestartSerivcesForSystemEventReceiver");
                break;
            case "android.intent.action.MEDIA_MOUNTED":
                Log.i(TAG, "android.intent.action.MEDIA_MOUNTE");
                break;
            case "android.intent.action.MEDIA_UNMOUNTEDD":
                Log.i(TAG, "android.intent.action.MEDIA_UNMOUNTEDD");
                break;
            case "android.intent.action.MEDIA_EJECT":
                Log.i(TAG, "android.intent.action.MEDIA_EJECT");
                break;
            case "android.intent.action.SERVICE_STATE":
                Log.i(TAG, "android.intent.action.SERVICE_STATE");
                break;
        }
        if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            Intent ootStartIntent = new Intent(context, SplashActivity.class);
            ootStartIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(ootStartIntent);
        }

        if (intent.getAction().equals(Intent.ACTION_PACKAGE_REPLACED)) {
            Intent localIntent = new Intent(context, SplashActivity.class);
            localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(localIntent);
        }
    }
}