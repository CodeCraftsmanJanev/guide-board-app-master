package com.janev.chongqing_bus_app.tcp;

import com.janev.chongqing_bus_app.system.Path;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Function;

public class RunLog extends DataLog{

    private static final class Holder {
        public static final RunLog INSTANCE = new RunLog();
    }

    public static RunLog getInstance(){
        return RunLog.Holder.INSTANCE;
    }

    private RunLog() {
        super(Path.getRunLogPath());
    }

    @Override
    protected Function<byte[], ObservableSource<?>> resolve() {
        return bytes -> Observable.just(bytes).map(bytes1 -> {
            d(new String(bytes1));
            return new Object();
        });
    }

    public void inputData(String log){
        inputData(log.getBytes());
    }

    public void inputThrowable(Throwable throwable){
        String stackTrace = getStackTrace(throwable);
        inputData(stackTrace);
    }

    @Override
    public void inputData(byte[] bytes) {
        super.inputData(bytes);
    }
}
