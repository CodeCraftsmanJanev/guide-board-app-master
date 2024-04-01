package com.yunbiao.publicity_guideboard.net;

import android.text.TextUtils;

import com.lzy.okgo.model.Response;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Function;

public class PublicityCheckFunction implements Function<Response<PublicityResponse>, ObservableSource<PublicityResponse>> {
    private static final String TAG = "PublicityCheckFunction";

    @Override
    public ObservableSource<PublicityResponse> apply(Response<PublicityResponse> publicityResponseResponse) throws Exception {
        if (!publicityResponseResponse.isSuccessful()) {
            return Observable.error(new Exception("request failed"));
        }

        PublicityResponse body = publicityResponseResponse.body();
        int code = body.getCode();
        String message = body.getMessage();

        if(body.isSuccess()){
            //成功直接放行
        } else if(!TextUtils.isEmpty(message) && message.contains("未发布")){
            //未发布直接放行
        } else if(code != 200){
            return Observable.error(new Exception("response failed[" + code + "][" + message + "]"));
        } else {
            return Observable.error(new Exception("request failed, success is false[" + code + "][" + message + "]"));
        }

        return Observable.just(body);
    }
}
