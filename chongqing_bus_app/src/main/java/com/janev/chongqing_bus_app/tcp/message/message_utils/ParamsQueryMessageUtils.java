package com.janev.chongqing_bus_app.tcp.message.message_utils;

import com.janev.chongqing_bus_app.tcp.message.MessageUtils;
import com.janev.chongqing_bus_app.tcp.message.ParamsReportRequest;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class ParamsQueryMessageUtils {
    //0x01 硬件序列号
    //0x02 硬件版本号
    //0x03 固件版本号
    //0x04 应用版本号
    //0x05 设备出场日期
    //0x06 设备自编号，设备唯一标识
    //0x07 厂商编码
    //0x08 设备地址
    //0x09 LED屏滚动速度
    //0x0A LCD屏幕参数
    //0x0B 屏幕显示亮度
    //0x0C 播放音量
    //0x0D-0x1F 预留
    //0x20-0x40 自定义
    //0x41 当前车辆自编号
    //0x42 当前车辆车牌号
    //0x43 心跳间隔，秒
    //0x44 消息重发次数
    //0x45 主服务器地址
    //0x46 主服务器端口
    //0x47 备用服务器地址
    //0x48 备用服务器端口
    //0x49-0x60 预留
    public static void query(byte order, String msgSerial, ByteBuffer byteBuffer){
        // 2828
        // 84
        // 00
        // 220120000001
        // 000000000000
        // 159c
        // 0001
        // 00
        // 0e0c

        int queryNumber = MessageUtils.getByteInt(byteBuffer);

        List<Byte> paramIDList = new ArrayList<>();
        if(queryNumber == 0){
            paramIDList.add((byte)0x01);
            paramIDList.add((byte)0x02);
            paramIDList.add((byte)0x03);
            paramIDList.add((byte)0x04);
            paramIDList.add((byte)0x05);
            paramIDList.add((byte)0x06);
            paramIDList.add((byte)0x07);
            paramIDList.add((byte)0x08);
            paramIDList.add((byte)0x09);
            paramIDList.add((byte)0x0A);
            paramIDList.add((byte)0x0B);
            paramIDList.add((byte)0x0C);
            paramIDList.add((byte)0x41);
            paramIDList.add((byte)0x42);
            paramIDList.add((byte)0x43);
            paramIDList.add((byte)0x44);
            paramIDList.add((byte)0x45);
            paramIDList.add((byte)0x46);
            paramIDList.add((byte)0x47);
            paramIDList.add((byte)0x48);
        } else {
            for (int i = 0; i < queryNumber; i++) {
                paramIDList.add(byteBuffer.get());
            }
        }

        new ParamsReportRequest(msgSerial,paramIDList).send();
    }
}
