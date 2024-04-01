package com.yunbiao.publicity_guideboard.serial;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.blankj.utilcode.util.LogUtils;
import com.yunbiao.publicity_guideboard.system.Cache;
import com.yunbiao.publicity_guideboard.utils.BytesUtils;

import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;

import tp.xmaihh.serialport.SerialHelper;
import tp.xmaihh.serialport.bean.ComBean;

public class SerialPortService extends Service {
    public static final String TAG = "SerialPortService";

    private final MyBinder myBinder = new MyBinder();

    private SerialHelper serialHelper;
    private DataHandler dataHandler;

    @Override
    public void onCreate() {
        super.onCreate();
        d("onCreate: ");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        d("onStartCommand: ");
        return super.onStartCommand(intent, flags, startId);
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        d("onBind: ");
        initSerial();
        open();
        return myBinder;
    }

    private void initSerial(){
        serialHelper = new SerialHelper("/dev/ttyS2",9600) {
            @Override
            public void open() throws SecurityException, IOException, InvalidParameterException {
                try {
                    super.open();
                    d("open: 开启串口");
                    if(dataHandler == null){
//                        dataHandler = new TMDataHandler();
                        dataHandler = new HYDataHandler();
                    }
                } catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void close() {
                super.close();
                d("close: 关闭串口");
                if(dataHandler != null){
                    dataHandler = null;
                }
            }

            @Override
            protected void onDataReceived(ComBean paramComBean) {
                byte[] bRec = paramComBean.bRec;
                if(bRec != null && bRec.length > 0){
                    String hex = BytesUtils.bytesToHex(bRec);
                    d("<---" + hex);
                }

                if(dataHandler != null){
                    dataHandler.check(serialHelper,paramComBean);
                }
            }

            @Override
            public void sendHex(String sHex) {
                super.sendHex(sHex);
                d("--->" + sHex);
            }
        };
        serialHelper.setStickPackageHelper(is -> {
            byte[] byteArray = new byte[512];
            if(is != null){
                try {
                    int size = is.read(byteArray);
                    if(size > 0){
                        return BytesUtils.SubByte(byteArray,0,size);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return byteArray;
        });
        serialHelper.setDataBits(8);
        serialHelper.setStopBits(1);
    }

    private void open(){
        if (serialHelper != null && !serialHelper.isOpen()) {
            try {
                serialHelper.open();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void close(){
        if(serialHelper != null && serialHelper.isOpen()){
            serialHelper.close();
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        d("onUnbind: ");
        close();
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        d("onDestroy: ");
    }

    public class MyBinder extends Binder {
        SerialPortService getService(){
            return SerialPortService.this;
        }
    }

    private void d(String log){
        LogUtils.d(TAG,log);
    }
}
