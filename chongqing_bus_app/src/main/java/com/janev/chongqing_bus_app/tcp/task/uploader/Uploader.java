package com.janev.chongqing_bus_app.tcp.task.uploader;

import com.janev.chongqing_bus_app.tcp.task.downloader.Downloader;
import com.janev.chongqing_bus_app.tcp.task.downloader.OnDownloadListener;

import java.io.File;

import io.reactivex.Observable;

public abstract class Uploader {

    private OnUploadListener onUploadListener;

    public Uploader setListener(OnUploadListener onUploadListener) {
        this.onUploadListener = onUploadListener;
        return this;
    }

    private void onStart(){
        if(onUploadListener != null){
            onUploadListener.onStart();
        }
    }

    protected void onProgress(int percent){
        if(onUploadListener != null){
            onUploadListener.onProgress(percent);
        }
    }

    private void onComplete(Boolean b){
        if(onUploadListener != null){
            onUploadListener.onComplete(b,true);
        }
    }

    private void onError(Throwable throwable){
        if(onUploadListener != null){
            onUploadListener.onError(throwable);
        }
    }

    public Observable<Boolean> upload(){
        return implUpload()
                .doOnSubscribe(disposable -> onStart())
                .doOnError(this::onError)
                .doOnNext(this::onComplete);
    }

    protected abstract Observable<Boolean> implUpload();

}
