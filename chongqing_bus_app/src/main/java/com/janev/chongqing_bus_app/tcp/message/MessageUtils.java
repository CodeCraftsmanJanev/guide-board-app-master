package com.janev.chongqing_bus_app.tcp.message;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.util.Log;

import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.BrightnessUtils;
import com.blankj.utilcode.util.DeviceUtils;
import com.blankj.utilcode.util.PhoneUtils;
import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.janev.chongqing_bus_app.BuildConfig;
import com.janev.chongqing_bus_app.system.Cache;
import com.janev.chongqing_bus_app.utils.BytesUtils;
import com.janev.chongqing_bus_app.utils.L;
import com.janev.chongqing_bus_app.utils.StringUtils;
import com.janev.chongqing_bus_app.utils.VolumeManager;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Date;

public class MessageUtils {
    //        String imsi = PhoneUtils.getIMSI();
//        String meid = PhoneUtils.getMEID();
//        String imei = PhoneUtils.getIMEI();

    public static void setParams(String key,String value){
        Cache.setString(key,value);
        Log.d(TAG, "setParams: " + key + " --- " + value);
    }
    public static void setParams(String key,boolean value){
        Cache.setBoolean(key,value);
        Log.d(TAG, "setParams: " + key + " --- " + value);

    }
    public static void setParams(String key,int value){
        Cache.setInt(key,value);
        Log.d(TAG, "setParams: " + key + " --- " + value);
    }

    public static void setParamsId(String paramsId){
        Cache.setString(Cache.Key.PARAMS_ID,paramsId);
        Log.d(TAG, "setParamsId: " + paramsId);
    }
    public static void removeParamsId(){
        Cache.remove(Cache.Key.PARAMS_ID);
        Log.d(TAG, "removeParamsId: ");
    }
    public static String getParamsId(){
        String localParamsID = Cache.getString(Cache.Key.PARAMS_ID);
        Log.d(TAG, "getParamsId: " + localParamsID);
        return localParamsID;
    }
    public static void setParamsVersion(String paramsVersion){
        Cache.setString(Cache.Key.PARAMS_VERSION,paramsVersion);
        Log.d(TAG, "setParamsVersion: " + paramsVersion);
    }
    public static void removeParamsVersion(){
        Cache.remove(Cache.Key.PARAMS_VERSION);
        Log.d(TAG, "removeParamsVersion: ");
    }
    public static String getParamsVersion(){
        String localVersionName = Cache.getString(Cache.Key.PARAMS_VERSION);
        Log.d(TAG, "getParamsVersion: " + localVersionName);
        return localVersionName;
    }


    /**
     * 获取消息重发次数
     * @return
     */
    public static void setMessageResendTime(int i){
        Log.d(TAG, "setMessageResendTime: " + i);
        Cache.setInt(Cache.Key.MESSAGE_RESEND_TIMES,i);
    }
    public static String getMessageResendTimeHex(){
        int anInt = getMessageResendTime();
        String s = addZero(Integer.toHexString(anInt), 2);
        Log.d(TAG, "getMessageResendTimeHex: " + s);
        return s;
    }
    public static int getMessageResendTime(){
        int anInt = Cache.getInt(Cache.Key.MESSAGE_RESEND_TIMES, Cache.Default.MESSAGE_RESEND_TIMES);
        Log.d(TAG, "getMessageResendTime: " + anInt);
        return anInt;
    }

    
    
    


    public static void setMainServerAddress(String string){
        Log.d(TAG, "setMainServerAddress: " + string);
        Cache.setString(Cache.Key.MAIN_SERVER_ADDRESS,string);
    }

    public static String getMainServerAddressHex(){
        String mainServerAddress = MessageUtils.getMainServerAddress();
        String gbk = BytesUtils.bytesToHex(mainServerAddress.getBytes(Charset.forName("GBK")));
        Log.d(TAG, "getMainServerAddressHex: " + gbk);
        return gbk;
    }

    /**
     * 主服务器地址
     */
    public static String getMainServerAddress(){
        String string = Cache.getString(Cache.Key.MAIN_SERVER_ADDRESS, Cache.Default.MAIN_SERVER_ADDRESS);
        Log.d(TAG, "主服务器地址: " + string);
        return string;
    }





    public static void setMainServerPort(int port){
        Log.d(TAG, "setMainServerPort: " + port);
        Cache.setInt(Cache.Key.MAIN_SERVER_PORT, port);
    }

    public static String getMainServerPortHex(){
        int mainServerPort = getMainServerPort();
        String intStr = addZero(Integer.toHexString(mainServerPort),2);
        Log.d(TAG, "getMainServerPortHex: " + intStr);
        return intStr;
    }

    /**
     * 主服务器端口
     */
    public static int getMainServerPort(){
        int anInt = Cache.getInt(Cache.Key.MAIN_SERVER_PORT, Cache.Default.MAIN_SERVER_PORT);
        Log.d(TAG, "主服务器端口: " + anInt);
        return anInt;
    }





    public static void setSpareServerAddress(String string){
        Log.d(TAG, "setSpareServerAddress: " + string);
        Cache.setString(Cache.Key.SPARE_SERVER_ADDRESS, string);
    }

    public static String getSpareServerAddressHex(){
        String spareServerAddress = getSpareServerAddress();
        String s = BytesUtils.bytesToHex(spareServerAddress.getBytes(Charset.forName("GBK")));
        Log.d(TAG, "getSpareServerAddressHex: " + s);
        return s;
    }

    /**
     * 备用服务器地址
     */
    public static String getSpareServerAddress(){
        String string = Cache.getString(Cache.Key.SPARE_SERVER_ADDRESS, Cache.Default.SPARE_SERVER_ADDRESS);
        Log.d(TAG, "备用服务器地址: " + string);
        return string;
    }






    public static void setSpareServerPort(int port){
        Log.d(TAG, "setSpareServerPort: " + port);
        Cache.setInt(Cache.Key.SPARE_SERVER_PORT, port);
    }

    public static String getSpareServerPortHex(){
        int spareServerPort = getSpareServerPort();
        String intStr = addZero(Integer.toHexString(spareServerPort),4);
        Log.d(TAG, "getMainServerPortHex: " + intStr);
        return intStr;
    }

    /**
     * 备用服务器端口
     */
    public static int getSpareServerPort(){
        int anInt = Cache.getInt(Cache.Key.SPARE_SERVER_PORT, Cache.Default.SPARE_SERVER_PORT);
        Log.d(TAG, "备用服务器端口: " + anInt);
        return anInt;
    }






    public static void setPulseInterval(int i){
        Log.d(TAG, "setPulseInterval: " + i);
        Cache.setInt(Cache.Key.PULSE_INTERVAL, i);
    }

    public static String getPulseIntervalHex(){
        int pulseInterval = getPulseInterval();
        String s = addZero(Integer.toHexString(pulseInterval), 2);
        Log.d(TAG, "getPulseIntervalHex: " + s);
        return s;
    }

    /**
     * 心跳间隔
     */
    public static int getPulseInterval(){
        int anInt = Cache.getInt(Cache.Key.PULSE_INTERVAL, Cache.Default.PULSE_INTERVAL);
        Log.d(TAG, "心跳间隔: " + anInt);
        return anInt;
    }






    public static void setCarNumber(String carNumber){
        Log.d(TAG, "setCarNumber: " + carNumber);
        Cache.setString(Cache.Key.CAR_NUMBER,carNumber);
    }

    public static String getCarNumberHex(){
        String carNumber = getCarNumber();
        String gbk = BytesUtils.bytesToHex(carNumber.getBytes(Charset.forName("GBK")));
        Log.d(TAG, "getCarNumberHex: " + gbk);
        return gbk;
    }

    /**
     * 车辆编号
     * @return
     */
    public static String getCarNumber(){
        String string = Cache.getString(Cache.Key.CAR_NUMBER);
        if(TextUtils.isEmpty(string)){
            string = "000000000000";
        }
        Log.d(TAG, "车辆编号: " + string);
        return string;
    }





    public static void setCarLicenseNumber(String licenseNumber){
        Cache.setString(Cache.Key.CAR_LICENSE_NUMBER,licenseNumber);
    }

    public static String getCarLicenseNumberHex(){
        String carLicenseNumber = getCarLicenseNumber();
        String param = BytesUtils.bytesToHex(carLicenseNumber.getBytes(Charset.forName("GBK")));
        Log.d(TAG, "getCarLicenseNumberHex: " + param);
        return param;
    }

    /**
     * 车牌号
     * @return
     */
    public static String getCarLicenseNumber(){
        String string = Cache.getString(Cache.Key.CAR_LICENSE_NUMBER);
        Log.d(TAG, "车牌号: " + string);
        return string;
    }




    public static String getScreenParamsHex(){
        String s = getScreenParams();
        String gbk = BytesUtils.bytesToHex(s.getBytes(Charset.forName("GBK")));
        Log.d(TAG, "getScreenParamsHex: " + gbk);
        return gbk;
    }
    public static String getScreenParams(){
        int screenDensityDpi = ScreenUtils.getScreenDensityDpi();
        int screenWidth = ScreenUtils.getScreenWidth();
        int screenHeight = ScreenUtils.getScreenHeight();
        String s = screenWidth + "," + screenHeight + "," + screenDensityDpi;
        Log.d(TAG, "getScreenParams: " + s);
        return s;
    }




    public static void setVolumePercent(int volumePercent){
        Cache.setInt(Cache.Key.VOLUME_PERCENT,volumePercent);
        VolumeManager.setVolumePercent(volumePercent);
    }
    public static String getVolumePercentHex(){
        int percent = Cache.getInt(Cache.Key.VOLUME_PERCENT,Cache.Default.VOLUME_PERCENT);
        String percentStr = Integer.toHexString(percent);
        String s = addZero(percentStr, 2);
        Log.d(TAG, "getVolumePercentHex: " + s);
        return s;
    }
    public static int getVolumePercent(){
        int percent = Cache.getInt(Cache.Key.VOLUME_PERCENT,Cache.Default.VOLUME_PERCENT);
        Log.d(TAG, "getVolumePercent: " + percent);
        return percent;
    }





    public static void setBrightness(int brightness){
        BrightnessUtils.setBrightness(brightness);
        Log.d(TAG, "setBrightness: " + brightness);
    }

    public static String getBrightnessHex(){
        int brightness = getBrightness();
        String s = addZero(Integer.toHexString(brightness), 2);
        Log.d(TAG, "getBrightnessHex: " + s);
        return s;
    }
    public static int getBrightness(){
        int brightness = BrightnessUtils.getBrightness();
        Log.d(TAG, "getBrightness: " + brightness);
        return brightness;
    }




    public static void setDeviceAddressHex(String address){
        Log.d(TAG, "setDeviceAddress: " + address);
        Cache.setString(Cache.Key.DEVICE_ADDRESS, address);
    }

    /**
     * 设备地址
     */
    public static String getDeviceAddressHex(){
        String string = Cache.getString(Cache.Key.DEVICE_ADDRESS, Cache.Default.DEVICE_ADDRESS);
        Log.d(TAG, "设备地址: " + string);
        return string;
    }







    /**
     * 设备编号
     * @return
     */
    public static void setDeviceNumber(String deviceNumber){
        Log.d(TAG, "setDeviceNumber: " + deviceNumber);
        Cache.setString(Cache.Key.DEVICE_NUMBER, deviceNumber);
    }
    public static void removeDeviceNumber() {
        Cache.remove(Cache.Key.DEVICE_NUMBER);
    }
    public static String getDeviceNumber(){
        String string = Cache.getString(Cache.Key.DEVICE_NUMBER, Cache.Default.DEVICE_NUMBER);
        Log.d(TAG, "设备编号: " + string);
        return string;
    }



    /**
     * 终端编号
     * @return
     */
    public static void setTerminalNumber(String terminalNumber){
        Log.d(TAG, "setTerminalNumber: " + terminalNumber);
        Cache.setString(Cache.Key.TERMINAL_NUMBER,terminalNumber);
    }
    public static String getTerminalNumber(){
        // 003133393230313730303039
        // 013920170009
        // 003320201109
        String terminalNumber = Cache.getString(Cache.Key.TERMINAL_NUMBER);
        terminalNumber = addZero(terminalNumber,12);
        terminalNumber = terminalNumber.replaceAll("^[^0-9]+", "");
        terminalNumber = MessageUtils.addZero(terminalNumber, 12);
        Log.d(TAG, "终端编号: " + terminalNumber);
        return terminalNumber;
    }






    /**
     * 消息流水
     * @return
     */
    public static String getMessageSerialHex(){
        int messageSerial = Cache.getInt(Cache.Key.MESSAGE_SERIAL, Cache.Default.MESSAGE_SERIAL);
        messageSerial += 1;
        if(messageSerial > 65535){
            messageSerial = 1;
        }
        Cache.setInt(Cache.Key.MESSAGE_SERIAL,messageSerial);
        String s = addZero(Integer.toHexString(messageSerial),4);
        Log.d(TAG, "消息流水: " + s);
        return s;
    }







    /**
     * 硬件序列号
     * @return
     */
    public static String getHardwareSerialHex(){
        String deviceId = getDeviceId();
        if(TextUtils.isEmpty(deviceId)){
            deviceId = "";
        } else {
            deviceId = BytesUtils.bytesToHex(deviceId.getBytes(/*Charset.forName("GBK")*/));
        }
        L.tcp(TAG,"硬件序列号：" + deviceId);
        return deviceId;
    }
    @SuppressLint("MissingPermission")
    public static String getDeviceId(){
        String deviceId = PhoneUtils.getDeviceId();
        Log.d(TAG, "getDeviceId: " + deviceId);
        return deviceId;
    }






    /**
     * 硬件版本号
     * @return
     */
    @SuppressLint("MissingPermission")
    public static String getHardwareVersionHex(){
        String model = getModel();
        if(TextUtils.isEmpty(model)){
            model = "";
        } else {
            model = BytesUtils.bytesToHex(model.getBytes(/*Charset.forName("GBK")*/));
        }
        L.tcp(TAG,"硬件版本号：" + model);
        return model;
    }
    public static String getModel(){
        String model = DeviceUtils.getModel();
        Log.d(TAG, "getModel: " + model);
        return model;
    }

    /**
     * 固件版本号
     * @return
     */
    @SuppressLint("MissingPermission")
    public static String getSystemVersionHex(){
        String sdkVersionName = getSdkVersionName();
        if(TextUtils.isEmpty(sdkVersionName)){
            sdkVersionName = "";
        } else {
            sdkVersionName = BytesUtils.bytesToHex(sdkVersionName.getBytes(/*Charset.forName("GBK")*/));
        }
        L.tcp(TAG,"固件版本号：" + sdkVersionName);
        return sdkVersionName;
    }
    public static String getSdkVersionName(){
        String sdkVersionName = DeviceUtils.getSDKVersionName();
        Log.d(TAG, "getSdkVersionName: " + sdkVersionName);
        return sdkVersionName;
    }

    /**
     * 软件版本号
     * @return
     */
    @SuppressLint("MissingPermission")
    public static String getAppVersionHex(){
        String appVersionName = getAppVersionName();
        if(TextUtils.isEmpty(appVersionName)){
            appVersionName = "";
        } else {
            appVersionName = BytesUtils.bytesToHex(appVersionName.getBytes(/*Charset.forName("GBK")*/));
        }
        L.tcp(TAG,"软件版本号：" + appVersionName);
        return appVersionName;
    }
    public static String getAppVersionName(){
        String appVersionName = AppUtils.getAppVersionName();
        Log.d(TAG, "getAppVersionName: " + appVersionName);
        return appVersionName;
    }

    /**
     * ICCID
     * @return
     */
    @SuppressLint("MissingPermission")
    public static String getICCIDHex(){
        String serial = PhoneUtils.getSerial();
        if(TextUtils.isEmpty(serial)){
            serial = "";
        } else {
            serial = BytesUtils.bytesToHex(serial.getBytes(/*Charset.forName("GBK")*/));
        }
        L.tcp(TAG,"ICCID：" + serial);
        return serial;
    }

    /**
     * 厂商编码
     * @return
     */
    public static void setProductNumber(String inputProductNumber){
        Cache.setString(Cache.Key.PRODUCT_NUMBER,inputProductNumber);
    }
    public static void removeProductNumber(){
        Cache.remove(Cache.Key.PRODUCT_NUMBER);
    }
    public static String getProductNumberHex(){
        String productNumber = getProductNumber();
//        while (productNumber.length() < 20) {
//            productNumber = "00" + productNumber;
//        }
        if(TextUtils.isEmpty(productNumber)){
            productNumber = "0000000000";
        }
        L.tcp(TAG,"厂商编码：" + productNumber);
        return productNumber;
    }
    public static String getProductNumber(){
        String productNumber = Cache.getString(Cache.Key.PRODUCT_NUMBER, Cache.Default.PRODUCT_NUMBER);
        Log.d(TAG, "getProductNumber: " + productNumber);
        return productNumber;
    }

    private static final String TAG = "MessageUtils";
    
    
    
    public static void setAuthNumber(String string){
        Cache.setString(Cache.Key.AUTH_NUMBER,string);
        Log.d(TAG, "setAuthNumber: " + string);
    }
    public static void removeAuthNumber(){
        Cache.remove(Cache.Key.AUTH_NUMBER);
        Log.d(TAG, "removeAuthNumber: ");
    }
    /**
     * 厂商授权码
     * @return
     */
    public static String getAuthNumberHex(){
        String string = Cache.getString(Cache.Key.AUTH_NUMBER, Cache.Default.AUTH_NUMBER);
        if(TextUtils.isEmpty(string)){
            string = "00000000000000000000000000000000";
        }
        L.tcp(TAG,"厂商授权码：" + string);
        return string;
    }






    public static void setLineName(String lineName){
        Cache.setString(Cache.Key.LINE_NAME,lineName);
    }

    public static String getLineName(){
        String lineName = Cache.getString(Cache.Key.LINE_NAME);
//        if(BuildConfig.DEBUG){
//            lineName = "81801";// TODO: 2022/11/22 删除测试内容
//        }
        Log.d(TAG, "线路号: " + lineName);
        return lineName;
    }

    /**
     * 线路号
     * @return
     */
    public static String getLineNameHex(){
        String lineName = getLineName();
        lineName = TextUtils.isEmpty(lineName) ? "" : BytesUtils.bytesToHex(lineName.getBytes(Charset.forName("GBK")));
        Log.d(TAG, "线路号: " + lineName);
        return lineName;
    }


    /**
     * APP资源ID
     */
    public static String getAppResId(){
        String string = Cache.getString(Cache.Key.APP_RES_ID);
        Log.d(TAG, "getAppResId: " + string);
        return string;
    }
    
    public static void setAppResourceVersion(String appResourceVersion){
        Cache.setString(Cache.Key.APP_RESOURCE_VERSION,appResourceVersion);
        Log.d(TAG, "setAppResourceVersion: " + appResourceVersion);
    }
    public static void removeAppResourceVersion(){
        Cache.remove(Cache.Key.APP_RESOURCE_VERSION);
        Log.d(TAG, "removeAppResourceVersion: ");
    }
    public static String getAppResourceVersion(){
        String string = Cache.getString(Cache.Key.APP_RESOURCE_VERSION);
        Log.d(TAG, "getAppResourceVersion: " + string);
        return string;
    }


    /**
     * APP资源ID
     * @param newResourceId
     */
    public static void setAppResourceId(String newResourceId){
        Cache.setString(Cache.Key.APP_RESOURCE_ID,newResourceId);
        Log.d(TAG, "setAppResourceId: " + newResourceId);
    }
    public static void removeAppResourceId(){
        Cache.remove(Cache.Key.APP_RESOURCE_ID);
        Log.d(TAG, "removeAppResourceId: ");
    }
    public static String getAppResourceId(){
        String string = Cache.getString(Cache.Key.APP_RESOURCE_ID);
        Log.d(TAG, "getAppResourceId: " + string);
        return string;
    }
    
    


    /**
     * 资源ID
     */
    public static void setResourceID(String resourceId){
        Cache.setString(Cache.Key.RESOURCE_ID,resourceId);
        Log.d(TAG, "setResourceId: " + resourceId);
    }
    public static void removeResourceId(){
        Cache.remove(Cache.Key.RESOURCE_ID);
        Log.d(TAG, "removeResourceId: ");
    }
    public static String getResourceID(){
        // 0000000000000042
        String string = Cache.getString(Cache.Key.RESOURCE_ID);
        if(TextUtils.isEmpty(string)){
            return "0000000000000000";
        } else {
            string = addZero(string,16);
        }
        Log.d(TAG, "资源ID: " + string);
        return string;
    }




    /**
     * 资源版本
     * @return
     */
    public static void setResourceVersion(String s){
        Cache.setString(Cache.Key.RESOURCE_VERSION,s);
        Log.d(TAG, "setResourceVersion: " + s);
    }
    public static void removeResourceVersion(){
        Cache.remove(Cache.Key.RESOURCE_VERSION);
        Log.d(TAG, "removeResourceVersion: ");
    }
    public static String getResourceVersion(){
        String string = Cache.getString(Cache.Key.RESOURCE_VERSION);
        Log.d(TAG, "资源版本: " + string);
        return string;
    }

    /**
     * BCC异或校验
     * @param hexString
     * @return
     */
    public static String getBCC(String hexString){
        try {
            Log.e(TAG, "getBCC: " + hexString);
            String bcc = getBCC(BytesUtils.hexToByteArray(hexString));
            Log.e(TAG, "getBCC: " + bcc);
            return bcc;
        } catch (Exception e){
            Log.e(TAG, "getBCC: ", e);
            throw e;
        }
    }

    /**
     * BCC异或校验
     * @param data
     * @return
     */
    public static String getBCC(byte[] data) {
        String ret = "";
        byte BCC[] = new byte[1];
        for (int i = 0; i < data.length; i++) {
            BCC[0] = (byte) (BCC[0] ^ data[i]);
        }
        String hex = Integer.toHexString(BCC[0] & 0xFF);
        if (hex.length() == 1) {
            hex = '0' + hex;
        }
        ret += hex.toUpperCase();
        return ret;
    }

    
    
    
    public static void setProductDate(String date){
        Cache.setString(Cache.Key.PRODUCT_DATE,date);
        Log.d(TAG, "setProductDate: " + date);
    }
    /**
     * 生产日期
     * @return
     */
    public static String getProductDate(){
        String string = Cache.getString(Cache.Key.PRODUCT_DATE);
        if(TextUtils.isEmpty(string)){
            string = TimeUtils.date2String(new Date(), "yyyyMMdd");
            Cache.setString(Cache.Key.PRODUCT_DATE,string);
        }
        Log.d(TAG, "生产日期: " + string);
        return string;
    }

    public static String addZero(String s,int limit){
        StringBuilder as = new StringBuilder(s);
        while (as.length() < limit) {
            as.insert(0, "0");
        }
        return as.toString();
    }

    /**
     * 获取长度
     * @param hexStr
     * @return
     */
    public static String getLength(String hexStr){
        return getLength(hexStr,2);
    }

    /**
     * 获取长度
     * @param hexStr
     * @param limitLength
     * @return
     */
    public static String getLength(String hexStr,int limitLength){
        String result;
        if(TextUtils.isEmpty(hexStr) || hexStr.length() == 0){
            result = "00";
        } else {
            result = Integer.toHexString(hexStr.length() / 2);
        }
        return addZero(result,limitLength);
    }

    /**
     * 判断文件的编码格式
     * @param fileName :file
     * @return 文件编码格式
     * @throws Exception
     */
    public static String codeString(String fileName) throws Exception{
        BufferedInputStream bin = new BufferedInputStream(
                new FileInputStream(fileName));
        int p = (bin.read() << 8) + bin.read();
        String code = null;
        switch (p) {
            case 0xefbb:
                code = "UTF-8";
                break;
            case 0xfffe:
                code = "Unicode";
                break;
            case 0xfeff:
                code = "UTF-16BE";
                break;
            default:
                code = "GBK";
        }
        return code;
    }


    public static int getByteInt(ByteBuffer byteBuffer){
        return BytesUtils.hex16to10(BytesUtils.byteToHex(byteBuffer.get()));
    }
    public static String getByteHex(ByteBuffer byteBuffer){
        return BytesUtils.byteToHex(byteBuffer.get());
    }
    public static int getBytesInt(ByteBuffer byteBuffer,int length){
        byte[] bytes = new byte[length];
        byteBuffer.get(bytes);
        return BytesUtils.hex16to10(BytesUtils.bytesToHex(bytes));
    }
    public static String getBytesHex(ByteBuffer byteBuffer,int length){
        if(length > 0){
            byte[] bytes = new byte[length];
            byteBuffer.get(bytes);
            return BytesUtils.bytesToHex(bytes);
        } else {
            return "";
        }
    }
}
