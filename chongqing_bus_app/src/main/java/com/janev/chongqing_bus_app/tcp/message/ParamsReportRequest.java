package com.janev.chongqing_bus_app.tcp.message;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.BrightnessUtils;
import com.janev.chongqing_bus_app.utils.BytesUtils;

import java.util.List;

public class ParamsReportRequest extends IRequest{
    private final List<Byte> paramIDList;
    private final String msgSerial;
    public ParamsReportRequest(String msgSerial,@NonNull List<Byte> paramIDList) {
        super("04");
        this.msgSerial = msgSerial;
        this.paramIDList = paramIDList;
    }

    @Override
    protected String getHexData() {
        StringBuilder stringBuilder = new StringBuilder();
        //应答流水号
        stringBuilder.append(msgSerial);
        //参数个数
        stringBuilder.append(MessageUtils.addZero(Integer.toHexString(paramIDList.size()),2));
        //具体参数
        for (Byte aByte : paramIDList) {
            String param = "";
            switch (aByte) {
                case (byte)0x01://0x01 硬件序列号
                    param = MessageUtils.getHardwareSerialHex();
                    d("硬件序列号：" + param);
                    break;
                case (byte)0x02://0x02 硬件版本号
                    param = MessageUtils.getHardwareVersionHex();
                    d("硬件版本号：" + param);
                    break;
                case (byte)0x03://0x03 固件版本号
                    param = MessageUtils.getSystemVersionHex();
                    d("固件版本号：" + param);
                    break;
                case (byte)0x04://0x04 应用版本号
                    param = MessageUtils.getAppVersionHex();
                    d("应用版本号：" + param);
                    break;
                case (byte)0x05://0x05 设备出场日期
                    param = MessageUtils.getProductDate();
                    d("设备出场日期：" + param);
                    break;
                case (byte)0x06://0x06 设备自编号，设备唯一标识
                    param = MessageUtils.getDeviceNumber();
                    d("设备自编号，设备唯一标识：" + param);
                    break;
                case (byte)0x07://0x07 厂商编码
                    param = MessageUtils.getProductNumberHex();
                    d("厂商编码：" + param);
                    break;
                case (byte)0x08://0x08 设备地址
                    param = MessageUtils.getDeviceAddressHex();
                    d("设备地址：" + param);
                    break;
                case (byte)0x09://0x09 LED屏滚动速度
                    param = "00";
                    d("LED屏滚动速度：" + param);
                    break;
                case (byte)0x22://0x0A LCD屏幕参数
                case (byte)0x0A://0x0A LCD屏幕参数
                    param = MessageUtils.getScreenParamsHex();
                    d("LCD屏幕参数：" + param);
                    break;
                case (byte)0x23://0x0B 屏幕显示亮度
                case (byte)0x0B://0x0B 屏幕显示亮度
                    param = MessageUtils.getBrightnessHex();
                    d("屏幕显示亮度：" + param);
                    break;
                case (byte)0x24://0x0C 播放音量
                case (byte)0x0C://0x0C 播放音量
                    param = MessageUtils.getVolumePercentHex();
                    d("播放音量：" + param);
                    break;
                case (byte)0x20://0x06 设备自编号，设备唯一标识
                case (byte)0x41://0x41 当前车辆自编号
                    param = MessageUtils.getCarNumberHex();
                    d("当前车辆自编号：" + param);
                    break;
                case (byte)0x21://0x42 当前车辆车牌号
                case (byte)0x42://0x42 当前车辆车牌号
                    param = MessageUtils.getCarLicenseNumberHex();
                    d("当前车辆车牌号：" + param);
                    break;
                case (byte)0x25://0x25 心跳间隔
                case (byte)0x43://0x43 心跳间隔，秒
                    param = MessageUtils.getPulseIntervalHex();
                    d("心跳间隔，秒：" + param);
                    break;
                case (byte)0x26://0x44 消息重发次数
                case (byte)0x44://0x44 消息重发次数
                    param = MessageUtils.getMessageResendTimeHex();
                    d("消息重发次数：" + param);
                    break;
                case (byte)0x27://0x45 主服务器地址
                case (byte)0x45://0x45 主服务器地址
                    param = MessageUtils.getMainServerAddressHex();
                    d("主服务器地址：" + param);
                    break;
                case (byte)0x28://0x46 主服务器端口
                case (byte)0x46://0x46 主服务器端口
                    param = MessageUtils.getMainServerPortHex();
                    d("主服务器端口：" + param);
                    break;
                case (byte)0x29://0x47 备用服务器地址
                case (byte)0x47://0x47 备用服务器地址
                    param = MessageUtils.getSpareServerAddressHex();
                    d("备用服务器地址：" + param);
                    break;
                case (byte)0x2A://0x48 备用服务器端口
                case (byte)0x48://0x48 备用服务器端口
                    param = MessageUtils.getSpareServerPortHex();
                    d("备用服务器端口：" + param);
                    break;
                default:
                    param = "00";
                    break;
            }

            stringBuilder
                    //参数ID
                    .append(BytesUtils.byteToHex(aByte))
                    //参数长度
                    .append(MessageUtils.getLength(param))
                    //参数内容
                    .append(param);
        }

        return stringBuilder.toString();
    }
}
