package com.janev.chongqing_bus_app.tcp.task.downloader;


import android.text.TextUtils;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.ZipUtils;
import com.janev.chongqing_bus_app.system.Path;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.functions.Function;

public abstract class Downloader {
    private final String destFilePath;
    protected final String tempDir;

    private OnDownloadListener onDownloadListener;

    public Downloader setListener(OnDownloadListener onDownloadListener) {
        this.onDownloadListener = onDownloadListener;
        return this;
    }

    private void onStart(){
        if(onDownloadListener != null){
            onDownloadListener.onStart();
        }
    }

    protected void onProgress(int percent){
        if(onDownloadListener != null){
            onDownloadListener.onProgress(percent);
        }
    }

    private void onComplete(File file){
        if(onDownloadListener != null){
            onDownloadListener.onComplete(file,true);
        }
    }

    private void onError(Throwable throwable){
        if(onDownloadListener != null){
            onDownloadListener.onError(throwable);
        }
    }

    public Downloader(String destFilePath) {
        this.destFilePath = destFilePath;
        this.tempDir = Path.getTempDir();
    }

    public Observable<File> download(){
        Observable<File> observable;

        if(!TextUtils.isEmpty(destFilePath) && FileUtils.isFileExists(destFilePath)){
            observable = Observable.just(new File(destFilePath));
        } else {
            observable = implDownload().map(handleFile());
        }

        return observable
                .retryWhen(errors -> errors
                        .zipWith(Observable.range(1, 3), (throwable, integer) -> new Object[]{integer,throwable})
                        .flatMap(objects -> {
                            int times = (int) objects[0];
                            Throwable throwable = (Throwable) objects[1];
                            if(times >= 3){
                                return Observable.error(throwable);
                            } else {
                                return Observable.timer(30, TimeUnit.SECONDS);
                            }
                        }))
                .doOnSubscribe(disposable -> onStart())
                .doOnError(this::onError)
                .doOnNext(this::onComplete);
    }

    protected abstract Observable<File> implDownload();

    private Function<File,File> handleFile(){
        return tempFile -> {
            File destTempFile = tempFile;
            if(tempFile.getName().endsWith(".zip")){
                //解压到目标缓存路径
                List<File> files = ZipUtils.unzipFile(tempFile, tempFile.getParentFile());
                FileUtils.delete(tempFile);
                if(files == null || files.isEmpty()){
                    throw new Exception("unzip failed,unknown error");
                }
                //取第一个文件
                destTempFile = files.get(0);
            }

            if(!TextUtils.isEmpty(destFilePath)){
                String fileExtension = FileUtils.getFileExtension(destTempFile);
                if(!TextUtils.isEmpty(fileExtension)){
                    fileExtension = "." + fileExtension;
                }
                //移动目标文件
                File destFile = new File(destFilePath + fileExtension);
                if (!FileUtils.move(destTempFile,destFile)) {
                    throw new Exception("destCacheFile move to destFile failed,unknown error");
                }
                return destFile;
            }
            return destTempFile;
        };
    }

    public static boolean isFtp(String url){
        return url.startsWith("ftp://");
    }

}
