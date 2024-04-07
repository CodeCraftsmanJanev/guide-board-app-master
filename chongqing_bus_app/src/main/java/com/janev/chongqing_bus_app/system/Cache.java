package com.janev.chongqing_bus_app.system;

import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.blankj.utilcode.util.FileUtils;
import com.janev.chongqing_bus_app.utils.L;
import com.tencent.mmkv.MMKV;

import java.io.File;
import java.util.List;

/***
 * 缓存基于设备本身
 */
public class Cache {
    private static final String TAG = "Cache";
    private static MMKV mmkv;

    private static final String CACHE_ID = "cache";

    public static void init(){
        StringBuilder stringBuilder = new StringBuilder();
        try {
            List<File> files = FileUtils.listFilesInDir(Path.getCachePath());
            stringBuilder.append("..... 缓存目录文件数量：").append(files.size()).append("\n");
            if(!files.isEmpty()){
                for (File file : files) {
                    stringBuilder.append("..... 缓存文件：").append(file.getAbsolutePath())
                            .append(";")
                            .append(file.lastModified())
                            .append(";")
                            .append(file.canRead())
                            .append(";")
                            .append(file.canWrite())
                            .append("\n");
                }
            }
            String initialize = MMKV.initialize(Path.getCachePath());
            stringBuilder.append("..... 加载缓存文件：").append(initialize).append("\n");
            mmkv = MMKV.mmkvWithID(CACHE_ID);
            stringBuilder.append("..... 缓存文件key总数：").append(mmkv.count()).append("\n");
        } catch (Exception e){
            stringBuilder.append("..... 加载异常：").append(e.getMessage()).append("\n");
        }
        L.tcp(TAG,stringBuilder.toString());
    }

    public interface Key{
        //协议类型
        String AGREEMENT_ORDINAL = "keyAgreementType";
        //主副屏
        String zhufuping = "zhufuping";

        //收集日志
        String DEBUG = "keyDebug";
        //音量百分比
        String VOLUME_PERCENT = "keyVolumeValue";
        //图片播放时长


        //线路详情
        String LINE_INFO = "keyLineInfo";
        //线路名
        String LINE_NAME = "keyLineName";
        //上行列表
        String SITE_LIST_UP = "keySiteListUp";
        //下行列表
        String SITE_LIST_DOWN = "keySiteListDown";
        //线路星级
        String LINE_STAR = "keyLineStar";
        //司机工号
        String WORKER_ID = "keyWorkerId";
        //政治面貌
        String POLITIC = "keyPolitic";



        //设备编号
        String DEVICE_NUMBER = "keyDeviceNumber";
        //终端编号
        String TERMINAL_NUMBER = "keyTerminalNumber";
        //消息流水
        String MESSAGE_SERIAL = "keyMessageSerial";
        //资源ID
        String RESOURCE_ID = "keyResourceID";
        //APP ID
        String APP_RES_ID = "keyAppResID";
        //资源版本
        String RESOURCE_VERSION = "keyResourceVersion";
        //车辆编号
        String CAR_NUMBER = "keyCarNumber";
        //车牌号
        String CAR_LICENSE_NUMBER = "keyCarLicenseNumber";
        //心跳间隔
        String PULSE_INTERVAL = "keyPulseInterval";
        //主服务器地址
        String MAIN_SERVER_ADDRESS = "keyMainServerAddress";
        //主服务器端口
        String MAIN_SERVER_PORT = "keyMainServerPort";
        //备用服务器地址
        String SPARE_SERVER_ADDRESS = "keySpareServerAddress";
        //备用服务器端口
        String SPARE_SERVER_PORT = "keySpareServerPort";
        //设备地址
        String DEVICE_ADDRESS = "keyDeviceAddress";
        //厂商编码
        String PRODUCT_NUMBER = "keyProductNumber";
        //厂商授权码
        String AUTH_NUMBER = "keyAuthNumber";
        //资源文件ID
        String APP_RESOURCE_ID = "keyAppResourceID";
        //资源版本号
        String APP_RESOURCE_VERSION = "keyAppResourceVersion";
        //消息重发时间
        String MESSAGE_RESEND_TIMES = "keyMessageResendTimes";
        //生产日期
        String PRODUCT_DATE = "keyProductDate";
        //参数文件ID
        String PARAMS_ID = "keyParamsId";
        //参数文件版本号
        String PARAMS_VERSION = "keyParamsVersion";
    }

    public interface Default{
        int MESSAGE_RESEND_TIMES = 0;
        String PRODUCT_NUMBER = "0100202201";

        String AUTH_NUMBER = "2825FE2CB75648278E47AA6B0C75E569";
        //zhang 主服务地址
//        String MAIN_SERVER_ADDRESS = "183.66.65.155";
        //        zhang 测试服务器地址
        String MAIN_SERVER_ADDRESS = "47.108.196.233";
        int MAIN_SERVER_PORT = 8900;

        String SPARE_SERVER_ADDRESS = "";
        int SPARE_SERVER_PORT = 0;

        int PULSE_INTERVAL = 30;

        int MESSAGE_SERIAL = 1;

        //        String DEVICE_NUMBER = "220120000002";
//        String DEVICE_NUMBER = "220120000001";
        String DEVICE_NUMBER = "220120000004";
//        String DEVICE_NUMBER = "220120001234";

        boolean DEBUG = true;

        int AGREEMENT_ORDINAL = Agreement.CHONGQING_V2.ordinal();

        int zhufuping = ZhuFuEnum.zhu.ordinal();

        String DEVICE_ADDRESS = "06";
        int VOLUME_PERCENT = 50;
    }

    public static void remove(String key){
        mmkv.removeValueForKey(key);
    }

    public static boolean contains(String key){
        return mmkv.containsKey(key);
    }

    //================================================================================
    public static boolean setString(String key, String value){
        return mmkv.encode(key,value);
    }

    public static String getString(String key, String defValue){
        return mmkv.decodeString(key,defValue);
    }

    public static String getString(String key){
        return getString(key,"");
    }
    //================================================================================
    public static boolean setInt(String key, int value){
        return mmkv.encode(key,value);
    }

    public static int getInt(String key,int defValue){
        return mmkv.decodeInt(key,defValue);
    }


    public static int getInt(String key){
        return getInt(key,-1);
    }
    //================================================================================

    public static boolean setBoolean(String key, boolean value) {
        return mmkv.encode(key,value);
    }

    public static boolean getBoolean(String key,boolean defValue){
        return mmkv.decodeBool(key, defValue);
    }

    public static boolean getBoolean(String key){
        return getBoolean(key,false);
    }
    //================================================================================

    public static boolean setLong(String key, long value) {
        return mmkv.encode(key,value);
    }

    public static long getLong(String key,long defValue){
        return mmkv.decodeLong(key,defValue);
    }

    public static long getLong(String key){
        return getLong(key,0l);
    }

    //================================================================================
    public static <T> boolean setObj(String key, T o){
        String s = JSONObject.toJSONString(o);
        Log.e(TAG, "setObj: " + s);
        return mmkv.encode(key,s);
    }

    public static <T> T getObj(String key, Class<T> clazz){
        String string = mmkv.decodeString(key);
        Log.e(TAG, "getObj: " + string);
        if(!TextUtils.isEmpty(string)){
            try {
                return JSONObject.parseObject(string, clazz);
            } catch (Exception e){
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }
}
