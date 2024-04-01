package com.yunbiao.publicity_guideboard.net;

import android.text.TextUtils;

import com.lzy.okgo.model.Response;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Function;

public class DriverInfoCheckFunction implements Function<Response<DriverInfoResponse>, ObservableSource<DriverInfoResponse>> {
    @Override
    public ObservableSource<DriverInfoResponse> apply(Response<DriverInfoResponse> driverInfoResponseResponse) throws Exception {
        DriverInfoResponse body = driverInfoResponseResponse.body();

        if(!TextUtils.equals("0",body.getRet())){
            return Observable.error(new Exception("response failed,error code[" + body.getRet() + "][" + body.getMsg() + "]"));
        }

        return Observable.just(body);
    }
}
