package com.janev.chongqing_bus_app.tcp.task.params;

import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.blankj.utilcode.util.FileIOUtils;
import com.janev.chongqing_bus_app.system.Cache;
import com.janev.chongqing_bus_app.tcp.message.MessageUtils;
import com.janev.chongqing_bus_app.tcp.task.resource.ResourceDataResolver;

import java.io.File;

import io.reactivex.functions.Function;

public class ParamsDataResolver implements Function<File, String> {
    private static final String TAG = "ParamsDataResolver";

    @Override
    public String apply(File file) throws Exception {
        try {
            String string = FileIOUtils.readFile2String(file);
            Log.d(TAG, "解析：" + string);

            JSONArray jsonArray = JSONObject.parseArray(string);
            if(jsonArray != null && !jsonArray.isEmpty()){
                for (int i = 0; i < jsonArray.size(); i++) {
                    try {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        Log.d(TAG,jsonObject.toJSONString());
                        int id = jsonObject.getIntValue("id");
                        String value = jsonObject.getString("value");
                        if(id < 1 || TextUtils.isEmpty(value)){
                            continue;
                        }
                        String paramKey = getParamKey(id);
                        if(TextUtils.isEmpty(paramKey)){
                            continue;
                        }
                        String paramType = getParamType(paramKey);
                        if(TextUtils.isEmpty(paramType)){
                            continue;
                        }

                        switch (paramType) {
                            case "String":
                                MessageUtils.setParams(paramKey,value);
                                Log.d(TAG,"setString: " + paramKey + " --- " + value);
                                break;
                            case "boolean":
                                MessageUtils.setParams(paramKey,Boolean.parseBoolean(value));
                                Log.d(TAG,"setBoolean: " + paramKey + " --- " + value);
                                break;
                            case "int":
                                MessageUtils.setParams(paramKey,Integer.parseInt(value));
                                Log.d(TAG,"setInt: " + paramKey + " --- " + value);
                                break;
                        }
                    } catch ( Exception e){
                        Log.d(TAG,"读取数据异常：" + e.getMessage());
                    }
                }
            }
            return string;
        } catch (Exception e){
            Log.e(TAG, "apply: ", e);
            throw e;
        }
    }

    private static String getParamType(String key){
        if(TextUtils.isEmpty(key)){
            return "";
        }
        switch (key) {
            case Cache.Key.VOLUME_PERCENT:
            case Cache.Key.PULSE_INTERVAL:
            case Cache.Key.SPARE_SERVER_PORT:
            case Cache.Key.MAIN_SERVER_PORT:
            case Cache.Key.MESSAGE_RESEND_TIMES:
                return int.class.getSimpleName();
            case Cache.Key.DEBUG:
                return boolean.class.getSimpleName();
            default:
                return String.class.getSimpleName();
        }
    }

    //根据变量值获得参数ID
    private static String getParamKey(int id){
        switch (id) {
            case 1:
                return Cache.Key.VOLUME_PERCENT;
            case 2:
                return Cache.Key.DEVICE_NUMBER;
            case 3:
                return Cache.Key.TERMINAL_NUMBER;
            case 4:
                return Cache.Key.CAR_NUMBER;
//            case 5:
//                return Cache.Key.CAR_LICENSE_NUMBER;
            case 6:
                return Cache.Key.PULSE_INTERVAL;
            case 7:
                return Cache.Key.MAIN_SERVER_ADDRESS;
            case 8:
                return Cache.Key.MAIN_SERVER_PORT;
            case 9:
                return Cache.Key.SPARE_SERVER_ADDRESS;
            case 10:
                return Cache.Key.SPARE_SERVER_PORT;
            case 11:
                return Cache.Key.DEVICE_ADDRESS;
            case 12:
                return Cache.Key.PRODUCT_NUMBER;
            case 13:
                return Cache.Key.AUTH_NUMBER;
            case 14:
                return Cache.Key.MESSAGE_RESEND_TIMES;
            case 15:
                return Cache.Key.PRODUCT_DATE;
            case 16:
                return Cache.Key.DEBUG;
            default:
                return "";
        }
    }

}
