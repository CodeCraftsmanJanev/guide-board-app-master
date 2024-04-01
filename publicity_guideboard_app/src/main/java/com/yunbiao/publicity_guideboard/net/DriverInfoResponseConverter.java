package com.yunbiao.publicity_guideboard.net;

import android.text.TextUtils;

import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.LogUtils;
import com.lzy.okgo.convert.Converter;

import okhttp3.Response;
import okhttp3.ResponseBody;

public class DriverInfoResponseConverter implements Converter<DriverInfoResponse> {
    private final String TAG;
    private final String URL;

    public DriverInfoResponseConverter(String TAG, String URL) {
        this.TAG = TAG;
        this.URL = URL;
    }

    @Override
    public DriverInfoResponse convertResponse(Response response) throws Throwable {
        ResponseBody body = response.body();
        if (body == null) {
            throw new Exception("request failed");
        }

        String string = body.string();
        d("请求响应：" + string);
        if(TextUtils.isEmpty(string)){
            throw new Exception("request empty");
        }

        DriverInfoResponse driverInfoResponse = GsonUtils.fromJson(string, DriverInfoResponse.class);
        if(driverInfoResponse == null){
            throw new Exception("format to PublicityResponse failed");
        }

        return driverInfoResponse;
    }

    private void d(String log){
        LogUtils.d(TAG,TAG + ":" + log);
    }
}
