package com.janev.chongqing_bus_app.utils;

import android.media.AudioManager;
import android.util.Log;

import com.blankj.utilcode.util.UiMessageUtils;
import com.blankj.utilcode.util.VolumeUtils;
import com.janev.chongqing_bus_app.system.UiEvent;
import com.janev.chongqing_bus_app.tcp.message.MessageUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class VolumeManager {
    private static final String TAG = "VolumeManager";

    public static int getRealVolume(){
        return com.blankj.utilcode.util.VolumeUtils.getVolume(AudioManager.STREAM_MUSIC);
    }

    public static void setVolumePercent(int percent){

        int maxVolume = VolumeUtils.getMaxVolume(AudioManager.STREAM_MUSIC);
        //音量百分比 / 100 * 最大音量 = 实际音量
        int volume = BigDecimal.valueOf(percent)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.UP)
                .multiply(BigDecimal.valueOf(maxVolume)).intValue();
        Log.d(TAG, "222setVolumePercent: " + volume + " --- " + maxVolume);
        VolumeUtils.setVolume(AudioManager.STREAM_MUSIC,volume,AudioManager.FLAG_SHOW_UI);

        UiMessageUtils.getInstance().send(UiEvent.EVENT_UPDATE_VOLUME,new int[]{percent,volume});

        int volumePercent = MessageUtils.getVolumePercent();
        Log.d(TAG, "333setVolumePercent: " + volumePercent);
    }

    public static void setCacheVolume(){
        int volumePercent = MessageUtils.getVolumePercent();
        setVolumePercent(volumePercent);
    }
}
