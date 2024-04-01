package com.janev.chongqing_bus_app.tcp.message;

import android.util.Log;

import com.blankj.utilcode.util.TimeUtils;
import com.janev.chongqing_bus_app.easysocket.EasySocket;
import com.janev.chongqing_bus_app.tcp.DataLog;
import com.janev.chongqing_bus_app.tcp.TCPLog;
import com.janev.chongqing_bus_app.utils.BytesUtils;
import com.janev.chongqing_bus_app.utils.L;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public abstract class IRequest {
    private final String TAG = getClass().getSimpleName();

    protected final String START = "2828";
    protected final String END = "0C";

    protected final long OUT_TIME = 60 * 1000;
    protected long lastTime = 0;

    protected final String ORDER;
    protected final String DEVICE_ADDRESS;
    protected int result = -1;

    protected String lastSendTime;
    protected String lastResultTime;

    public int getResult() {
        return result;
    }

    public String getLastSendTime() {
        return lastSendTime;
    }

    public void setLastSendTime() {
        this.lastSendTime = TimeUtils.date2String(new Date(),"HH:mm:ss");
    }

    public String getLastResultTime() {
        return lastResultTime;
    }

    public void setLastResultTime() {
        this.lastResultTime = TimeUtils.date2String(new Date(),"HH:mm:ss");
    }

    public IRequest(String order) {
        this(order,MessageUtils.getDeviceAddressHex());
    }

    public IRequest(String order, String deviceAddress) {
        this.ORDER = order;
        this.DEVICE_ADDRESS = deviceAddress;
    }

    public boolean isOutTime(){
        if (System.currentTimeMillis() - this.lastTime < OUT_TIME) {
            return false;
        }
        this.lastTime = System.currentTimeMillis();
        return true;
    }

    public boolean isSuccess(){
        if(result != 0){
            return false;
        }
        return true;
    }

    private Disposable disposable;
    public void send(){
        int anInt = MessageUtils.getMessageResendTime();
        if(anInt == 0){
            EasySocket.getInstance().upMessage(byteArray());
            setLastSendTime();
        } else {
            disposable = Observable
                    .intervalRange(0,anInt,0,3, TimeUnit.SECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .subscribe(aLong -> {
                        EasySocket.getInstance().upMessage(byteArray());
                        setLastSendTime();
                    });
        }
    }

    public void dispose(){
        if(disposable != null && !disposable.isDisposed()){
            disposable.dispose();
            disposable = null;
        }
    }

    public byte[] byteArray(){
        Log.d(TAG, "头部标识: " + START + " --- " + START.length());

        Log.d(TAG, "消息ID: " + ORDER + " --- " + ORDER.length());

        Log.d(TAG, "设备地址: " + DEVICE_ADDRESS + " --- " + DEVICE_ADDRESS.length());

        String DEVICE_NUMBER = MessageUtils.getDeviceNumber();
        Log.d(TAG, "设备编号: " + DEVICE_NUMBER + " --- " + DEVICE_NUMBER.length());

        String TERMINAL_NUMBER = MessageUtils.getTerminalNumber();
        Log.d(TAG, "终端编号: " + TERMINAL_NUMBER + " --- " + TERMINAL_NUMBER.length());

        String SERIAL_HEX = MessageUtils.getMessageSerialHex();
        Log.d(TAG, "消息流水号: " + SERIAL_HEX + " --- " + SERIAL_HEX.length());

        String CONTENT_HEX = getHexData();

        String CONTENT_LENGTH = MessageUtils.getLength(CONTENT_HEX, 4);
        Log.d(TAG, "消息内容长度: " + CONTENT_LENGTH + " --- " + CONTENT_LENGTH.length());

        Log.d(TAG, "消息内容: " + CONTENT_HEX + " --- " + CONTENT_HEX.length());

        String HEX_DATA =
                START // 2828 头部标识
                + ORDER // 02 消息ID
                + DEVICE_ADDRESS // 06 设备地址
                + DEVICE_NUMBER // 220120099999 设备编号
                + TERMINAL_NUMBER // 003133393230 终端编号
                + SERIAL_HEX // 3137 消息流水号
                + CONTENT_LENGTH // 3030 //长度
                + CONTENT_HEX;
        String BCC_HEX = MessageUtils.getBCC(HEX_DATA);
        Log.d(TAG, "校验码: " + BCC_HEX + " --- " + BCC_HEX.length());

        Log.d(TAG, "尾部标识: " + END + " --- " + END.length());

        String hexString = HEX_DATA + BCC_HEX + END;
        Log.d(TAG, "完整数据: " + hexString + " --- " + hexString.length());

        byte[] bytes = BytesUtils.hexToByteArray(hexString);
        TCPLog.getInstance().inputData(bytes);

        return bytes;
    }

    protected String getTag(){
        return "";
    }

    protected abstract String getHexData();

    public void setResult(int result) {
        this.result = result;
        setLastResultTime();
    }

    protected void d(String log){
        L.tcp(TAG,log);
    }
}
