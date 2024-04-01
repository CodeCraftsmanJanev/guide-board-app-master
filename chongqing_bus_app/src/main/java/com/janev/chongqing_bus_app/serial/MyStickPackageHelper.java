package com.janev.chongqing_bus_app.serial;

import com.janev.chongqing_bus_app.utils.BytesUtils;

import java.io.IOException;
import java.io.InputStream;

import tp.xmaihh.serialport.stick.AbsStickPackageHelper;

public class MyStickPackageHelper implements AbsStickPackageHelper {
    @Override
    public byte[] execute(InputStream is) {
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
    }
}
