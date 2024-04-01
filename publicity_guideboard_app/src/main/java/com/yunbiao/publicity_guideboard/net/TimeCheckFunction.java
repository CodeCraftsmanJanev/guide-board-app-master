package com.yunbiao.publicity_guideboard.net;

import android.text.TextUtils;

import com.lzy.okgo.model.Response;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Function;

public class TimeCheckFunction implements Function<Response<TimeResponse>, ObservableSource<TimeResponse>> {
    private static final String TAG = "PublicityCheckFunction";

    @Override
    public ObservableSource<TimeResponse> apply(Response<TimeResponse> TimeResponseResponse) throws Exception {
        if (!TimeResponseResponse.isSuccessful()) {
            return Observable.error(new Exception("request failed"));
        }

        TimeResponse body = TimeResponseResponse.body();

        if(body.isSuccess()){
            //成功直接放行
        } else {
            return Observable.error(new Exception("get time failed"));
        }

        return Observable.just(body);
    }
}
