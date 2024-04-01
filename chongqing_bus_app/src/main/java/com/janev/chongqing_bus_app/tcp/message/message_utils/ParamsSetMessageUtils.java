package com.janev.chongqing_bus_app.tcp.message.message_utils;

import com.blankj.utilcode.util.BrightnessUtils;
import com.blankj.utilcode.util.UiMessageUtils;
import com.janev.chongqing_bus_app.system.UiEvent;
import com.janev.chongqing_bus_app.tcp.message.MessageUtils;
import com.janev.chongqing_bus_app.tcp.message.ReplyRequest;
import com.janev.chongqing_bus_app.utils.BytesUtils;
import com.janev.chongqing_bus_app.utils.L;
import com.janev.chongqing_bus_app.utils.StringUtils;
import com.janev.chongqing_bus_app.utils.VolumeManager;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class ParamsSetMessageUtils {
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
    public static void set(byte order, String msgSerial, ByteBuffer byteBuffer){

        int paramNumber = MessageUtils.getByteInt(byteBuffer);
        for (int i1 = 0; i1 < paramNumber; i1++) {
            byte paramId = byteBuffer.get();

            int paramLength = MessageUtils.getByteInt(byteBuffer);

            String hex = MessageUtils.getBytesHex(byteBuffer, paramLength);

            switch (paramId) {
                case (byte) 0x01://0x01 硬件序列号
                    String hardwareSerial = StringUtils.hexStringToString(hex);
                    d("设置硬件序列号：" + hardwareSerial);
                    break;
                case (byte) 0x02://0x02 硬件版本号
                    String hardwareVersion = StringUtils.hexStringToString(hex);
                    d("硬件版本号：" + hardwareVersion);
                    break;
                case (byte) 0x03://0x03 固件版本号
                    String systemVersion = StringUtils.hexStringToString(hex);
                    d("固件版本号：" + systemVersion);
                    break;
                case (byte) 0x04://0x04 应用版本号
                    String appVersion = StringUtils.hexStringToString(hex);
                    d("应用版本号：" + appVersion);
                    break;
                case (byte) 0x05://0x05 设备出场日期
                    String productDate = StringUtils.hexStringToString(hex);
                    d("设备出场日期：" + productDate);
                    break;
                case (byte) 0x09://0x09 LED屏滚动速度
                    String rollSpeed = StringUtils.hexStringToString(hex);
                    d("LED屏滚动速度：" + rollSpeed);
                    break;
                case (byte) 0x0A://0x0A LCD屏幕参数
                    String lcdParams = StringUtils.hexStringToString(hex);
                    d("LCD屏幕参数：" + lcdParams);
                    break;
                case (byte) 0x06://0x06 设备自编号，设备唯一标识
                    String s = StringUtils.hexStringToString(hex);
//                        Cache.setString(Cache.Key.DEVICE_NUMBER,s);
                    d("设置设备自编号：" + s);
                    break;
                case (byte) 0x07://0x07 厂商编码
                    d("厂商编码：" + hex);
                    break;
                case (byte) 0x08://0x08 设备地址
                    MessageUtils.setDeviceAddressHex(hex);
                    d("设置设备地址：" + hex);
                    break;
                case (byte) 0x0B://0x0B 屏幕显示亮度
                    int brigness = BytesUtils.hex16to10(hex);
                    MessageUtils.setBrightness(brigness);
                    d("设置亮度：" + brigness);
                    break;
                case (byte) 0x0C://0x0C 播放音量
                    int setVolume = BytesUtils.hex16to10(hex);
                    MessageUtils.setVolumePercent(setVolume);
                    d("设置音量：" + setVolume);
                    break;
                case (byte) 0x41://0x41 当前车辆自编号
                    String carNumber = StringUtils.hexStringToString(hex);
                    MessageUtils.setCarNumber(carNumber);
                    d("设置车辆自编号：" + carNumber);
                    break;
                case (byte) 0x42://0x42 当前车辆车牌号
                    String carLicenseNumber = StringUtils.hexStringToString(hex);
                    MessageUtils.setCarLicenseNumber(carLicenseNumber);
                    d("设置车辆车牌号：" + carLicenseNumber);
                    break;
                case (byte) 0x43://0x43 心跳间隔，秒
                    int seconds = BytesUtils.hex16to10(hex);
                    MessageUtils.setPulseInterval(seconds);
                    UiMessageUtils.getInstance().send(UiEvent.EVENT_SET_PULSE_FREQUENCY,seconds);
                    d("设置心跳间隔：" + seconds);
                    break;
                case (byte) 0x44://0x44 消息重发次数
                    int i = BytesUtils.hex16to10(hex);
                    MessageUtils.setMessageResendTime(i);
                    d("设置消息重发次数：" + i);
                    break;
                case (byte) 0x45://0x45 主服务器地址
                    String mainServerAddress = StringUtils.hexStringToString(hex);
                    MessageUtils.setMainServerAddress(mainServerAddress);
                    d("设置主服务器地址：" + mainServerAddress);
                    break;
                case (byte) 0x46://0x46 主服务器端口
                    int mainServerPort = BytesUtils.hex16to10(hex);
                    MessageUtils.setMainServerPort(mainServerPort);
                    d("设置主服务器端口：" + mainServerPort);
                    break;
                case (byte) 0x47://0x47 备用服务器地址
                    String spareServerAddress = StringUtils.hexStringToString(hex);
                    MessageUtils.setSpareServerAddress(spareServerAddress);
                    d("设置备用服务器地址：" + spareServerAddress);
                    break;
                case (byte) 0x48://0x48 备用服务器端口
                    int spareServerPort = BytesUtils.hex16to10(hex);
                    MessageUtils.setSpareServerPort(spareServerPort);
                    d("设置备用服务器端口：" + spareServerPort);
                    break;
                default:
                    new ReplyRequest(BytesUtils.byteToHex(order),msgSerial,ReplyRequest.SUCCESS).send();
                    break;
            }

            switch (paramId) {
                case (byte) 0x01://0x01 硬件序列号
                case (byte) 0x02://0x02 硬件版本号
                case (byte) 0x03://0x03 固件版本号
                case (byte) 0x04://0x04 应用版本号
                case (byte) 0x05://0x05 设备出场日期
                case (byte) 0x09://0x09 LED屏滚动速度
                case (byte) 0x0A://0x0A LCD屏幕参数
                    new ReplyRequest(BytesUtils.byteToHex(order),msgSerial,ReplyRequest.NOT_SUPPORT).send();
                    break;
            }
        }
    }

    private static final String TAG = "ParamsSetMessageUtils";
    private static void d(String log){
        L.tcp(TAG,log);
    }
}
