package com.janev.chongqing_bus_app.serial.request_v2;

import androidx.annotation.NonNull;

import com.janev.chongqing_bus_app.tcp.message.MessageUtils;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;


public class QueryLineRequest extends ISerialRequest{

    public QueryLineRequest(@NonNull GetHelperListener getHelperListener) {
        super("82", getHelperListener);
    }

    @Override
    protected String getContentHex() {
        return MessageUtils.getDeviceAddressHex();
    }

    private Disposable disposable;

    public void send(){
        stop();
        disposable = Observable
                .intervalRange(1,3,1,15, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.trampoline())
                .observeOn(Schedulers.trampoline())
                .repeatWhen(objectObservable ->
                        objectObservable.flatMap((Function<Object, ObservableSource<?>>) o ->
                                Observable.timer(5,TimeUnit.MINUTES)
                        )
                )
                .subscribe(aLong -> super.send());
    }

    public void stop(){
        if(disposable != null && !disposable.isDisposed()){
            disposable.dispose();
            disposable = null;
        }
    }

}
