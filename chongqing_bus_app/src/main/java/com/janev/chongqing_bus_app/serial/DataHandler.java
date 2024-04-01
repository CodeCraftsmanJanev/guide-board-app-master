package com.janev.chongqing_bus_app.serial;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.janev.chongqing_bus_app.utils.BytesUtils;
import com.janev.chongqing_bus_app.utils.L;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import tp.xmaihh.serialport.SerialHelper;
import tp.xmaihh.serialport.bean.ComBean;

public abstract class DataHandler implements Handler.Callback {
    protected final String TAG = getClass().getSimpleName();
    private final Map<String,StringBuilder> dataBuilderMap;
    private final Constraint constraint;
    protected final SerialHelper mSerialHelper;

    private final HandlerThread handlerThread;
    private final Handler handler;

    public DataHandler(SerialHelper serialHelper) {
        this.mSerialHelper = serialHelper;

        handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper(),this);

        this.constraint = initConstraint();
        this.dataBuilderMap = new HashMap<>();
        Set<String> bytes = this.constraint.startEndMap.keySet();
        for (String aByte : bytes) {
            dataBuilderMap.put(aByte,new StringBuilder());
        }

        loadCache();
    }

    protected abstract Constraint initConstraint();

    @Override
    public boolean handleMessage(@NonNull Message msg) {

        try {
            Object obj = msg.obj;
            if(obj instanceof ComBean){
                ComBean comBean = (ComBean) obj;

                if(comBean.bRec != null && comBean.bRec.length > 0){
                    byte[] bRec = comBean.bRec;

                    String startHex,endHex;
                    String unknownHex = BytesUtils.bytesToHex(bRec);
                    Log.d(TAG, "指令：" + unknownHex);

                    boolean onlyEnd = constraint.startEndMap.containsValue(unknownHex);
                    //仅包含指令尾的数据
                    if(onlyEnd){
                        startHex = unknownHex;
                        endHex = unknownHex;
                        Log.d(TAG, "check: 仅包含指令尾");
                    }
                    //正常数据
                    else if(bRec.length >= 2){
                        startHex = BytesUtils.bytesToHex(BytesUtils.SubByte(bRec, 0, 2));
                        endHex = BytesUtils.bytesToHex(BytesUtils.SubByte(bRec, bRec.length - 1, 1));
                        Log.d(TAG, "指令头尾：" + startHex + " , " + endHex);
                    }
                    //未知的超短数据
                    else {
                        return true;
                    }

                    Set<Map.Entry<String, String>> entries = constraint.startEndMap.entrySet();
                    for (Map.Entry<String, String> entry : entries) {
                        String startH = entry.getKey();
                        String endH = entry.getValue();
                        //数据组装
                        StringBuilder dataBuilder = dataBuilderMap.get(startH);
                        //判断是否需要组合未知数据
                        boolean aBoolean = constraint.compositeMap.get(startH);

                        Log.e(TAG, "判断：" + startH + " , " + endH + " --- " + aBoolean);
                        //如果起始位相同，说明是开头，清空数据后添加
                        if(TextUtils.equals(startHex,startH)){
                            dataBuilder.setLength(0);
                            dataBuilder.append(BytesUtils.bytesToHex(bRec));
                            Log.d(TAG, "指令头相同，添加指令");
                        }
                        //如果起始位不同，判断是否包含该起始位，如果包含则不处理
                        else if(constraint.startEndMap.containsKey(startHex)){
                            Log.d(TAG, "有该指令，不处理");
                            continue;
                        }
                        else if(TextUtils.equals(startHex,endHex)){
                            dataBuilder.append(BytesUtils.bytesToHex(bRec));
                            Log.d(TAG, "组合指令尾");
                        }
                        //如果不包含则判断是否需要组合数据
                        else if(aBoolean){
                            dataBuilder.append(BytesUtils.bytesToHex(bRec));
                            Log.d(TAG, "未知指令头，组合数据");
                        } else {
                            Log.d(TAG, "未知指令头，放弃数据");
                        }

                        //如果包含结束位，则发送数据
                        if(TextUtils.equals(endHex,endH)){
                            if(this instanceof ChongqingV2Handler){
                                Log.e(TAG, "检查数据：" + dataBuilder.toString());
                                String lengthHex = dataBuilder.substring(8,12);
                                Log.e(TAG, "检查长度：" + lengthHex);
                                int length = BytesUtils.hex16to10(lengthHex) * 2;
                                Log.e(TAG, "数据长度：" + length);
                                if(length != 0 && dataBuilder.length() < length){
                                    Log.d(TAG, "长度不合理，不发送指令");
                                    return true;
                                }
                            }

                            Log.d(TAG, "发现指令尾，发送指令");
                            byte[] bytes = BytesUtils.hexToByteArray(dataBuilder.toString());
//                            ChongqingV2SerialLog.getInstance().resolve(bRec);
                            handle(mSerialHelper,bytes);
                            dataBuilder.setLength(0);
                        }

                    }
                    Log.d(TAG, "-------------------------------------------------------------- ");


                } else {
                    d("数据为空");
                }
            } else {
                d("数据类型未知：" + (obj == null ? "NULL" : obj.getClass().getSimpleName()));
            }
        } catch (Exception e){
            e.printStackTrace();
            L.serialE(TAG,"解析失败：" + e);
        }

        return true;
    }

    public void check(ComBean comBean){
        Message message = handler.obtainMessage(1);
        message.what = 1;
        message.obj = comBean;
        handler.sendMessage(message);
    }

    protected abstract void loadCache();

    protected abstract void handle(SerialHelper serialHelper,byte[] bytes);

    public void onOpen() {

    }

    public void onClose() {

    }

    public static class Constraint{
        private final Map<String,String> startEndMap;
        private final Map<String,Boolean> compositeMap;
        public Constraint() {
            this.startEndMap = new HashMap<>();
            this.compositeMap = new HashMap<>();
        }

        public void addStartEnd(String startBytes, String endByte,boolean composite){
            this.startEndMap.put(startBytes,endByte);
            this.compositeMap.put(startBytes,composite);
        }
    }

    protected void d(String log){
        L.serialD(TAG,log);
    }

    protected void e(Throwable log){
        L.serialE(TAG,log);
    }

    protected void e(String log){
        L.serialE(TAG,log);
    }
}
