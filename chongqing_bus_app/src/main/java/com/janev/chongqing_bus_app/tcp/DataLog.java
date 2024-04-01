package com.janev.chongqing_bus_app.tcp;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.janev.chongqing_bus_app.tcp.message.MessageUtils;
import com.janev.chongqing_bus_app.utils.L;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.ByteBuffer;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

public abstract class DataLog implements Handler.Callback {

    private final String TAG;

    private final String logPath;

    private final Handler handler;
    private final HandlerThread handlerThread;

    public DataLog(String logPath) {
        this.logPath = logPath;
        this.TAG = getClass().getSimpleName();

        handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper(),this);
    }

    public void init(){

    }

    @Override
    public boolean handleMessage(@NonNull Message msg) {
        try {
            Object obj = msg.obj;
            if(obj instanceof byte[]){
                resolve().apply((byte[]) obj);
            }
        } catch (Exception e){
            e(e.getMessage());
        }
        return true;
    }

    public void clear(){
        if(handlerThread.isAlive()){
            handler.getLooper().quitSafely();
            handlerThread.quitSafely();
        }
        handler.removeCallbacksAndMessages(null);
    }

    public void inputData(byte[] bytes){
        Message message = handler.obtainMessage(1);
        message.what = 1;
        message.obj = bytes;
        handler.sendMessage(message);
    }

    public String getStackTrace(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }

    protected abstract Function<byte[], ObservableSource<?>> resolve();

    protected int getByteInt(ByteBuffer byteBuffer){
        return MessageUtils.getByteInt(byteBuffer);
    }
    protected String getByteHex(ByteBuffer byteBuffer){
        return MessageUtils.getByteHex(byteBuffer);
    }
    protected int getBytesInt(ByteBuffer byteBuffer,int length){
        return MessageUtils.getBytesInt(byteBuffer,length);
    }
    protected String getBytesHex(ByteBuffer byteBuffer,int length){
        return MessageUtils.getBytesHex(byteBuffer,length);
    }

    protected void d(String log){
        L.d(TAG,log,logPath);
    }
    protected void e(String log){
        L.e(TAG,log,logPath);
    }
    protected void w(String log){
        L.w(TAG,log,logPath);
    }
}
