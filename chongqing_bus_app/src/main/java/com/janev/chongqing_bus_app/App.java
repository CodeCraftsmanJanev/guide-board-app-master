package com.janev.chongqing_bus_app;

import android.content.Context;

import androidx.multidex.MultiDexApplication;

import com.blankj.utilcode.util.CrashUtils;
import com.blankj.utilcode.util.Utils;
import com.janev.chongqing_bus_app.system.Path;
import com.janev.chongqing_bus_app.tcp.message.CrashReportRequest;
import com.lzy.okgo.OkGo;

public class App extends MultiDexApplication {

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;

        Utils.init(this);

        OkGo.getInstance().init(this);

        CrashUtils.init(Path.getCrashDir(), crashInfo -> {
            crashInfo.getThrowable().printStackTrace();
            new CrashReportRequest(CrashReportRequest.LCD_ERROR,crashInfo.toString(),CrashReportRequest.STATUS_ING).send();
        });
    }

    public static Context getContext(){
        return context;
    }
}
