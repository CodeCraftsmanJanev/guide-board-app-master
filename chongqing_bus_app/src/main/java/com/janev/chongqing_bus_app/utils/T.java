package com.janev.chongqing_bus_app.utils;

import android.graphics.Color;
import android.view.Gravity;

import com.blankj.utilcode.util.ToastUtils;

public class T {

    public static void l(String toast){
        ToastUtils.getDefaultMaker()
                .setGravity(Gravity.CENTER,0,0)
                .setTextSize(40)
                .setBgColor(Color.WHITE)
                .setDurationIsLong(true)
                .show(toast);
    }

}
