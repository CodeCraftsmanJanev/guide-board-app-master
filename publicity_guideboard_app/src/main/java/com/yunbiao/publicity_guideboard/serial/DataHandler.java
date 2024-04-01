package com.yunbiao.publicity_guideboard.serial;

import android.text.TextUtils;
import android.util.Log;

import com.yunbiao.publicity_guideboard.utils.BytesUtils;

import tp.xmaihh.serialport.SerialHelper;
import tp.xmaihh.serialport.bean.ComBean;

public abstract class DataHandler {
    protected final String TAG = getClass().getSimpleName();

    private final Constraint constraint;

    public DataHandler(Constraint constraint) {
        this.constraint = constraint;

        loadCache();
    }

    public void check(SerialHelper serialHelper,ComBean comBean){
        if(comBean == null){
            return;
        }
        byte[] bRec = comBean.bRec;
        if(bRec == null || bRec.length <= 0){
            Log.d(TAG, "空消息");
            return;
        }

        byte[] bytes = bRec;
        if(constraint != null){
            if(!TextUtils.isEmpty(constraint.startHex)){
                byte start = BytesUtils.hexToByte(constraint.startHex);
                if(bRec[0] != start){
                    Log.d(TAG, "未知的起始位：" + BytesUtils.byteToHex(start));
                    return;
                }
            }

            if(!TextUtils.isEmpty(constraint.endHex)){
                byte end = BytesUtils.hexToByte(constraint.endHex);
                if(bRec[0] != end){
                    Log.d(TAG, "未知的结束位：" + BytesUtils.byteToHex(end));
                    return;
                }
            }

            if(constraint.lengthPosition > 0){
                if (bRec.length <= constraint.lengthPosition) {
                    Log.d(TAG, "长度不正确：" + bRec.length);
                    return;
                }
                int length = BytesUtils.hex16to10(BytesUtils.byteToHex(bRec[constraint.lengthPosition]));
                if(bRec.length < length){
                    Log.d(TAG, "长度不正确：" + bRec.length + " != " + length);
                    return;
                }
                bytes = BytesUtils.SubByte(bRec, 0, length);
            }
        }
        handle(serialHelper,bytes);
    }

    protected abstract void loadCache();

    protected abstract void handle(SerialHelper serialHelper,byte[] bytes);

    public static class Constraint{
        private String startHex = "";

        private String endHex = "";

        private int lengthPosition = -1;

        public Constraint(String startByte, String endByte, int lengthPosition) {
            this.startHex = startByte;
            this.endHex = endByte;
            this.lengthPosition = lengthPosition;
        }
    }

}
