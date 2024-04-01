package com.yunbiao.publicity_guideboard.net;

import android.util.Log;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Function;

public class RetryWithDelay implements Function<Observable<Throwable>, ObservableSource<?>> {
    private final String TAG,URL;
    private static final int DEFAULT_DELAY = 30;
    private final int delay;

    public RetryWithDelay(int delay,String TAG,String url) {
        this.delay = delay;
        this.TAG = TAG;
        this.URL = url;
    }

    public static RetryWithDelay defaultDelay(String TAG,String url){
        return new RetryWithDelay(DEFAULT_DELAY,TAG,url);
    }

    @Override
    public ObservableSource<?> apply(Observable<Throwable> throwableObservable) throws Exception {
        return throwableObservable.flatMap(new Function<Throwable, ObservableSource<?>>() {
            @Override
            public ObservableSource<?> apply(Throwable throwable) throws Exception {
                e("retryWhen: 收到错误：" + throwable.getMessage());
                return Observable.timer(delay, TimeUnit.SECONDS);
            }
        });
    }

    private void e(String log){
        Log.e(TAG,TAG + ":" + URL + ":" + log);
    }
}
