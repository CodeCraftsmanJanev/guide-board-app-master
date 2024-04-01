package com.janev.chongqing_bus_app.serial;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.janev.chongqing_bus_app.system.Agreement;
import com.janev.chongqing_bus_app.system.Cache;
import com.janev.chongqing_bus_app.utils.BytesUtils;
import com.janev.chongqing_bus_app.utils.L;

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
        int agreementType = Cache.getInt(Cache.Key.AGREEMENT_ORDINAL, Cache.Default.AGREEMENT_ORDINAL);
        Agreement agreement = Agreement.get(agreementType);
        if(agreement == null){
            d("未选择协议类型或协议类型不可用[" + agreementType + "]");
            return;
        }

        serialHelper = new SerialHelper(agreement.getPortPath(),agreement.getBaudRate()) {
            @Override
            public void open() throws SecurityException, IOException, InvalidParameterException {
                try {
                    super.open();
                    d("open: 打开串口");
                    if(dataHandler != null){
                        dataHandler.onOpen();
                    }
                } catch (Exception e){
                    e("open: 打开串口失败:" + e.getMessage());
                }
            }

            @Override
            public void close() {
                super.close();
                d("close: 关闭串口");
                if(dataHandler != null){
                    dataHandler.onClose();
                    dataHandler = null;
                }
            }

            @Override
            protected void onDataReceived(ComBean paramComBean) {
                byte[] bRec = paramComBean.bRec;
                if(bRec != null && bRec.length > 0){
                    String hex = BytesUtils.bytesToHex(bRec);
                    d("<---" + hex);
                } else {
                    d("<---" + "NULL");
                }
                if(dataHandler != null){
                    dataHandler.check(paramComBean);
                }
            }

            @Override
            public void send(byte[] bOutArray) {
                super.send(bOutArray);
//                ChongqingV2SerialLog.getInstance().resolve(bOutArray);
            }

            @Override
            public void sendHex(String sHex) {
                super.sendHex(sHex);
                d("--->" + sHex);
            }
        };
        serialHelper.setStickPackageHelper(new MyStickPackageHelper());
        serialHelper.setDataBits(agreement.getDataBits());
        serialHelper.setStopBits(agreement.getStopBits());

        d("当前协议：" + agreement.getName() + "，串口号：" + agreement.getPortPath() + "，波特率："+ agreement.getBaudRate() + "，数据位：" + agreement.getDataBits() + "，停止位：" + agreement.getStopBits());
        switch (agreement) {
            case CHONGQING_V1:
                dataHandler = new ChongqingV1Handler(serialHelper);
                break;
            case CHONGQING_V2:
                dataHandler = new ChongqingV2Handler(serialHelper);
                break;
            case CHONGQING_V1_1:
                dataHandler = new ChongqingV2Handler(serialHelper);
                break;
        }
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
        L.serialD(TAG,log);
    }

    private void e(String e){
        L.serialE(TAG,e);
    }
}
