package com.yunbiao.publicity_guideboard.system;

import android.util.Log;

import com.tencent.mmkv.MMKV;

/***
 * 缓存基于设备本身
 */
public class Cache {
    private static final String TAG = "Cache";
    private static MMKV mmkv;

    private static final String CACHE_ID = "cache";

    public static void init(){
        String initialize = MMKV.initialize(Path.getCachePath());
        Log.e(TAG, "init: " + initialize);
        mmkv = MMKV.mmkvWithID(CACHE_ID);
    }

    public interface Key{
        String LINE_NAME = "keyLineName";
        String SITE_VERSION = "keySiteVersion";
        String SITE_LIST_UP = "keySiteListUp";
        String SITE_LIST_DOWN = "keySiteListDown";
        String DEBUG = "keyDebug";

        String DRIVER_HEAD = "keyDriverHead";
        String DRIVER_NAME = "keyDriverName";
        String DRIVER_CODE = "keyDriverCode";
        String DRIVER_STAR = "keyDriverStar";
        String COMPLAIN_MOBILE = "keyComplainMobile";

        String USER_NAME = "keyUserName";
        String PASSWORD = "keyPassword";
        String BUS_CODE = "keyBusCode";
        String IMAGE_TIME_INDEX = "keyImageTimeIndex";
    }

    public interface Default{
        int IMAGE_TIME_INDEX = 2;

        String USER_NAME = "100001";
        String PASSWORD = "rbttaattaattbv";
        String BUS_CODE = "";
    }

    public static void remove(String key){
        mmkv.removeValueForKey(key);
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

}
