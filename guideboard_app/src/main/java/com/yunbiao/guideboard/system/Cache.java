package com.yunbiao.guideboard.system;

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
        String PRICE_POSITION = "key_Price_position";
        String SITE_LIST = "key_Site_list";
        String END_SITE = "key_End_site";
        String LINE_NUMBER = "key_Line_number";
        String DEBUG = "keyDebug";
    }

    public interface Default{
        int PRICE = 1;
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
