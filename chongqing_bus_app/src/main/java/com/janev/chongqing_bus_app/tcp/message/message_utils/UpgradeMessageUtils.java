package com.janev.chongqing_bus_app.tcp.message.message_utils;

import android.util.Log;

import com.janev.chongqing_bus_app.tcp.message.MessageUtils;
import com.janev.chongqing_bus_app.tcp.message.ReplyRequest;
import com.janev.chongqing_bus_app.tcp.message.ResourceInfoReportRequest;
import com.janev.chongqing_bus_app.tcp.task.app.UpgradeAppManager2;
import com.janev.chongqing_bus_app.tcp.task.appResource.AppResourceManager2;
import com.janev.chongqing_bus_app.tcp.task.params.UpgradeParamsManager;
import com.janev.chongqing_bus_app.tcp.task.resource.ResourceManager2;
import com.janev.chongqing_bus_app.utils.BytesUtils;
import com.janev.chongqing_bus_app.utils.L;
import com.janev.chongqing_bus_app.utils.StringUtils;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.Queue;

public class UpgradeMessageUtils {
    private static final String TAG = "UpgradeMessageUtils";

    public static void deviceUpgrade(byte order, String msgSerial, ByteBuffer byteBuffer){
        byte upgradeType = byteBuffer.get();
        Log.d(TAG, "更新类型：" + upgradeType);

        byte upgradeFlag = byteBuffer.get();
        Log.d(TAG, "更新标识：" + upgradeFlag);
        boolean forceUpgrade = upgradeFlag == (byte) 0x01;

        String resourceId = MessageUtils.getBytesHex(byteBuffer, 8);
        Log.d(TAG, "资源ID：" + resourceId);

        int versionLength = MessageUtils.getByteInt(byteBuffer);
        Log.d(TAG, "资源版本号长度：" + versionLength);

        String resourceVersion = MessageUtils.getBytesHex(byteBuffer, versionLength);
        Log.d(TAG, "资源版本号：" + resourceVersion);

        int fileAddressLength = MessageUtils.getByteInt(byteBuffer);
        Log.d(TAG, "FTP地址长度：" + fileAddressLength);

        String fileAddress = MessageUtils.getBytesHex(byteBuffer, fileAddressLength);
        if(fileAddressLength > 0){
            fileAddress = StringUtils.hexStringToString(fileAddress);
        }
        Log.d(TAG, "FTP地址：" + fileAddress);

        int ftpUserLength = MessageUtils.getByteInt(byteBuffer);
        Log.d(TAG, "FTP用户名长度：" + ftpUserLength);

        String ftpUser = MessageUtils.getBytesHex(byteBuffer, ftpUserLength);
        if(ftpUserLength > 0){
            ftpUser = StringUtils.hexStringToString(ftpUser);
        }
        Log.d(TAG, "FTP用户名：" + ftpUser);

        int ftpPasswordLength = MessageUtils.getByteInt(byteBuffer);
        Log.d(TAG, "FTP密码长度：" + ftpPasswordLength);

        String ftpPassword = MessageUtils.getBytesHex(byteBuffer, ftpPasswordLength);
        if(ftpPasswordLength > 0){
            ftpPassword = StringUtils.hexStringToString(ftpPassword);
        }
        Log.d(TAG, "FTP密码：" + ftpPassword);

        if(upgradeType == (byte)0x01){//固件程序（不支持）
            Log.d(TAG, "固件程序（不支持）");
            new ReplyRequest(BytesUtils.byteToHex(order), msgSerial,ReplyRequest.NOT_SUPPORT).send();
        } else {
            new ReplyRequest(BytesUtils.byteToHex(order), msgSerial,ReplyRequest.SUCCESS).send();
            if(upgradeType == (byte)0x05){
                new ResourceInfoReportRequest(msgSerial,null).send();
            }

            switch (upgradeType) {
                case (byte) 0x02://应用程序
                    Log.d(TAG, "应用程序");
                    UpgradeAppManager2.getInstance().check(msgSerial,forceUpgrade,resourceId,resourceVersion,fileAddress,ftpUser,ftpPassword);
                    break;
                case (byte) 0x03://应用程序资源包
                    Log.d(TAG, "应用程序资源包");
                    AppResourceManager2.getInstance().check(msgSerial,forceUpgrade,resourceId,resourceVersion,fileAddress,ftpUser,ftpPassword);
                    break;
                case (byte) 0x04://设备参数配置
                    Log.d(TAG, "设备参数配置");
                    UpgradeParamsManager.getInstance().check(msgSerial,forceUpgrade,resourceId,resourceVersion,fileAddress,ftpUser,ftpPassword);
                    break;
                case (byte) 0x05://广告资源文件
                    Log.d(TAG, "广告资源文件");
                    ResourceManager2.getInstance().checkResource(msgSerial,forceUpgrade,resourceId,resourceVersion,fileAddress,ftpUser,ftpPassword);
                    break;
            }
        }
    }

    public static void activeUpgrade(byte order,String msgSerial,ByteBuffer byteBuffer){
        String responseMsgSerial = MessageUtils.getBytesHex(byteBuffer, 2);
        Log.d(TAG, "应答流水号：" + responseMsgSerial);

        byte queryType = byteBuffer.get();
        Log.d(TAG, "查询类型：" + queryType);

        String resourceId = MessageUtils.getBytesHex(byteBuffer, 8);
        Log.d(TAG, "资源ID：" + resourceId);

        int versionLength = MessageUtils.getByteInt(byteBuffer);
        Log.d(TAG, "资源版本号长度：" + versionLength);

        String resourceVersion = MessageUtils.getBytesHex(byteBuffer, versionLength);
        Log.d(TAG, "资源版本号：" + resourceVersion);

        int fileAddressLength = MessageUtils.getByteInt(byteBuffer);
        Log.d(TAG, "FTP地址长度：" + fileAddressLength);

        String fileAddress = MessageUtils.getBytesHex(byteBuffer, fileAddressLength);
        if(fileAddressLength  > 0){
            fileAddress = StringUtils.hexStringToString(fileAddress);
        }
        Log.d(TAG, "FTP地址：" + fileAddress);

        int ftpUserLength = MessageUtils.getByteInt(byteBuffer);
        Log.d(TAG, "FTP用户名长度：" + ftpUserLength);

        String ftpUser = MessageUtils.getBytesHex(byteBuffer, ftpUserLength);
        if(ftpUserLength > 0){
            ftpUser = StringUtils.hexStringToString(ftpUser);
        }
        Log.d(TAG, "FTP用户名：" + ftpUser);

        int ftpPasswordLength = MessageUtils.getByteInt(byteBuffer);
        Log.d(TAG, "FTP密码长度：" + ftpPasswordLength);

        String ftpPassword = MessageUtils.getBytesHex(byteBuffer, ftpPasswordLength);
        if(ftpPasswordLength > 0){
            ftpPassword = StringUtils.hexStringToString(ftpPassword);
        }
        Log.d(TAG, "FTP密码：" + ftpPassword);


        if(queryType == (byte)0x01){//固件程序（不支持）
            Log.d(TAG, "固件程序（不支持）");
            new ReplyRequest(BytesUtils.byteToHex(order), msgSerial,ReplyRequest.NOT_SUPPORT).send();
        } else {
            new ReplyRequest(BytesUtils.byteToHex(order), msgSerial,ReplyRequest.SUCCESS).send();

            switch (queryType) {
                case (byte) 0x02://应用程序
                    Log.d(TAG, "应用程序");
                    UpgradeAppManager2.getInstance().check(msgSerial,false,resourceId,resourceVersion,fileAddress,ftpUser,ftpPassword);
                    break;
                case (byte) 0x03://应用程序资源包
                    Log.d(TAG, "应用程序资源包");
                    AppResourceManager2.getInstance().check(msgSerial,false,resourceId,resourceVersion,fileAddress,ftpUser,ftpPassword);
                    break;
                case (byte) 0x04://设备参数配置
                    Log.d(TAG, "设备参数配置");
                    UpgradeParamsManager.getInstance().check(msgSerial,false,resourceId,resourceVersion,fileAddress,ftpUser,ftpPassword);
                    break;
                case (byte) 0x05://广告资源文件
                    Log.d(TAG, "广告资源文件");
                    ResourceManager2.getInstance().checkResource(msgSerial,false,resourceId,resourceVersion,fileAddress,ftpUser,ftpPassword);
                    break;
            }
        }
    }

    public static void destroy(){
//        ResourceManager2.getInstance().destroy();
//        AppResourceManager2.getInstance().destroy();
//        UpgradeParamsFileManager2.getInstance().destroy();
//        UpgradeAppManager2.getInstance().destroy();
    }

    private static void d(String log){
        L.tcp(TAG,log);
    }
}
