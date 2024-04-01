package com.yunbiao.guideboard;

import android.app.Application;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.blankj.utilcode.util.CrashUtils;
import com.blankj.utilcode.util.Utils;

import java.io.File;

public class App extends Application {
    private static final String TAG = "App";
    private static Context context;
    @Override
    public void onCreate() {
        super.onCreate();
        context = this;

        Utils.init(this);

        CrashUtils.init(new File(Environment.getExternalStorageDirectory(), "bus_log"), crashInfo -> {
            Log.e(TAG, "onCrash: ", crashInfo.getThrowable());
            crashInfo.getThrowable().printStackTrace();
        });
    }

    public static Context getContext(){
        return context;
    }
}
