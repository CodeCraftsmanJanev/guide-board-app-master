package com.janev.chongqing_bus_app.tcp.message;

import android.util.Log;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;

public class LoginRequest extends IRequest {

    private Runnable successRunnable;

    public LoginRequest(Runnable successRunnable) {
        super("02");
        this.successRunnable = successRunnable;
    }

    @Override
    protected String getTag() {
        return "发送登录";
    }

    // 2828 【头部标识】
    // 02 【消息 ID】
    // 06 【设备地址】
    // 220120000001 【设备编号】
    // 013920170007 【终端编号】
    // 02aa 【消息流水号】
    // 0056 【长度】
    // 0f 【硬件序列号长度】
    // 333538323430303531313131313130【硬件序列号】
    // 15 【硬件版本号长度】
    // 416e64726f696453444b6275696c74666f72783836 【硬件版本号】
    // 05 【固件版本号长度】
    // 372e312e31 【固件版本号】
    // 03 【应用版本号长度】
    // 312e31 【应用版本号】
    // 10 【ICCID长度】
    // 454d554c41544f523330583758345830 【ICCID】
    // 0100202201 【厂商编码】
    // 2825fe2cb75648278e47aa6b0c75e569 【厂商授权码】
    // 6b0c

    // 2828
    // 02
    // 06
    // 220120000001
    // 013920170007
    // 060c
    // 0056
    // 0f
    // 333538323430303531313131313130
    // 15
    // 416e64726f696453444b6275696c74666f72783836
    // 05
    // 372e312e31
    // 03
    // 312e31
    // 10
    // 454d554c41544f523330583758345830
    // 0100202201
    // 2825FE2CB75648278E47AA6B0C75E569
    // C90C

    // 2828
    // 81
    // 00
    // 220120000001
    // 000000000000
    // 4a27
    // 0004
    // 03071c00f20c2828810022012000000100000000000038bd000402071d001a0c

    private static final String TAG = "LoginRequest";
    @Override
    protected String getHexData() {
        //硬件序列号
        String hardwareSerialHex = MessageUtils.getHardwareSerialHex();
        String hardwareSerialHexLength = MessageUtils.getLength(hardwareSerialHex);
        Log.d(TAG, "硬件序列号: " + hardwareSerialHex + " --- " + hardwareSerialHexLength);

        //硬件版本号
        String hardwareVersionHex = MessageUtils.getHardwareVersionHex();
        String hardwareVersionHexLength = MessageUtils.getLength(hardwareVersionHex);
        Log.d(TAG, "硬件版本号: " + hardwareVersionHex + " --- " + hardwareVersionHexLength);

        //固件版本号
        String systemVersionHex = MessageUtils.getSystemVersionHex();
        String systemVersionHexLength = MessageUtils.getLength(systemVersionHex);
        Log.d(TAG, "固件版本号: " + systemVersionHex + " --- " + systemVersionHexLength);

        //应用版本号
        String appVersionHex = MessageUtils.getAppVersionHex();
        String appVersionHexLength = MessageUtils.getLength(appVersionHex);
        Log.d(TAG, "应用版本号: " + appVersionHex + " --- " + appVersionHexLength);

        //ICCID
        String iccidHex = MessageUtils.getICCIDHex();
        String iccidHexLength = MessageUtils.getLength(iccidHex);
        Log.d(TAG, "ICCID: " + iccidHex + " --- " + iccidHexLength);

        //厂商编码
        String productNumberHex = MessageUtils.getProductNumberHex();
        Log.d(TAG, "厂商编码: " + productNumberHex);

        //厂商授权码
        String authNumberHex = MessageUtils.getAuthNumberHex();
        Log.d(TAG, "厂商授权码: " + authNumberHex);

        return hardwareSerialHexLength
                + hardwareSerialHex
                + hardwareVersionHexLength
                + hardwareVersionHex
                + systemVersionHexLength
                + systemVersionHex
                + appVersionHexLength
                + appVersionHex
                + iccidHexLength
                + iccidHex
                + productNumberHex
                + authNumberHex;
    }

    @Override
    public void setResult(int result) {
        super.setResult(result);

        if(isSuccess()){
            dispose();
            if(this.successRunnable != null){
                this.successRunnable.run();
            }
        }
    }

    public void dispose(){
        super.dispose();
        if(disposable != null && !disposable.isDisposed()){
            disposable.dispose();
            disposable = null;
        }
    }

    private Disposable disposable;
    @Override
    public void send() {
        dispose();
        disposable = Observable
                .interval(0,10,TimeUnit.SECONDS)
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> super.send());
    }
}
