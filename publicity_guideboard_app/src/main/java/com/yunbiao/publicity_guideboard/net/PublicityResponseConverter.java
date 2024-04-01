package com.yunbiao.publicity_guideboard.net;

import android.text.TextUtils;

import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ResourceUtils;
import com.lzy.okgo.convert.Converter;

import okhttp3.Response;
import okhttp3.ResponseBody;

public class PublicityResponseConverter implements Converter<PublicityResponse> {
    private final String TAG;
    private final String URL;

    public PublicityResponseConverter(String TAG, String URL) {
        this.TAG = TAG;
        this.URL = URL;
    }

//    private boolean test = true;
    @Override
    public PublicityResponse convertResponse(Response response) throws Throwable {
        ResponseBody body = response.body();
        if (body == null) {
            throw new Exception("request failed");
        }

        String string = body.string();
//        if(test){
//            string = ResourceUtils.readAssets2String("test_hy1");
//        } else {
//            string = ResourceUtils.readAssets2String("test_hy2");
//        }
//        test = !test;

        d("请求响应：" + string);
        if(TextUtils.isEmpty(string)){
            throw new Exception("request empty");
        }

        PublicityResponse publicityResponse = GsonUtils.fromJson(string, PublicityResponse.class);
        if(publicityResponse == null){
            throw new Exception("format to PublicityResponse failed");
        }

        return publicityResponse;
    }

    private void d(String log){
        LogUtils.d(TAG,TAG + ":" + log);
    }
}
