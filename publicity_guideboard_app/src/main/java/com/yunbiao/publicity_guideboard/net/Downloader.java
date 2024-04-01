package com.yunbiao.publicity_guideboard.net;

import android.os.Environment;
import android.util.Log;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.Utils;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.FileCallback;
import com.lzy.okgo.convert.FileConvert;
import com.lzy.okgo.model.Progress;
import com.lzy.okgo.model.Response;
import com.lzy.okrx2.adapter.ObservableResponse;
import com.yunbiao.publicity_guideboard.db.Advert;
import com.yunbiao.publicity_guideboard.db.DaoManager;

import java.io.File;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class Downloader {
    private static final String TAG = "Downloader";

    private final String advertNumber;

    private final String url;
    private final String name;
    private final String path;
    private OnDownloadListener onDownloadListener;
    private Utils.Consumer<Progress> progressConsumer;

    private final String cachePath;

    private final int RETRY_COUNT = 20;

    private Disposable disposable;

    public Downloader(Advert advert) {
        this.advertNumber = advert.getNumber();

        this.url = advert.getFilePath();
        this.name = advert.getFileName();
        this.path = advert.getLocalPath();

        this.cachePath = new File(new File(this.path).getParent(), "cache_" + this.name).getPath();

        d("\n编号：" + advertNumber + "\n地址：" + url + "\n名称：" + name + "\n路径：" + path + "\n缓存路径：" + cachePath);
    }

    public boolean isDownloading(){
        return disposable != null && !disposable.isDisposed();
    }

    public boolean isComplete(){
        return FileUtils.isFileExists(path);
    }

    public void setOnDownloadListener(OnDownloadListener onDownloadListener) {
        this.onDownloadListener = onDownloadListener;
    }

    public void setProgressConsumer(Utils.Consumer<Progress> progressConsumer) {
        this.progressConsumer = progressConsumer;
    }

    public String getAdvertNumber() {
        return advertNumber;
    }

    public String getName() {
        return name;
    }

    public void clear(){
        if (isDownloading()) {
            d("清除下载：" + name);
            disposable.dispose();
            disposable = null;
        }
        deleteOriginCacheFile();
        deleteDestCacheFile();
    }

    public void start(){
        //如果已完成则直接回调成功
        if (isComplete()) {
            d("该文件已下载完成");
            if(onDownloadListener != null){
                onDownloadListener.onComplete(advertNumber,FileUtils.getFileByPath(path),false);
            }
            return;
        }

        if(isDownloading()){
            d("正在下载中...");
            return;
        }
        FileConvert fileConvert = new FileConvert();
        fileConvert.setCallback(new FileCallback() {
            @Override
            public void onSuccess(Response<File> response) {
                if(Downloader.this.progressConsumer != null){
                    progressConsumer.accept(null);
                }
            }

            @Override
            public void downloadProgress(Progress progress) {
                if(Downloader.this.progressConsumer != null){
                    progressConsumer.accept(progress);
                }
            }
        });

        //开始下载
        disposable = OkGo.<File>get(url)
                .tag(url)
                .converter(fileConvert)
                .adapt(new ObservableResponse<>())
                .flatMap((Function<Response<File>, ObservableSource<File>>) fileResponse -> {
                    if(!fileResponse.isSuccessful()){
                        return Observable.error(new Exception("download failed"));
                    }

                    //检查文件是否存在
                    File originCacheFile = fileResponse.body();
                    if(!FileUtils.isFileExists(originCacheFile)){
                        return Observable.error(new Exception("cacheFile not exists,unknown error"));
                    }

                    //检查移动结果
                    File destCacheFile = new File(cachePath);
                    if (!FileUtils.move(originCacheFile,destCacheFile)) {
                        return Observable.error(new Exception("move failed,unknown error"));
                    }

                    //检查重命名结果
                    File destFile = new File(path);
                    if (!FileUtils.rename(destCacheFile,destFile.getName())) {
                        return Observable.error(new Exception("rename failed,unknown error"));
                    }

                    return Observable.just(destFile);
                })
                .retry((integer, throwable) -> integer.compareTo(RETRY_COUNT) <= 0)
                .subscribeOn(Schedulers.io())
                .doOnSubscribe(disposable -> {
                    d("开始下载：" + name);
                    //删除原始cache文件
                    deleteOriginCacheFile();
                    //删除目标cache文件
                    deleteDestCacheFile();

                    if(onDownloadListener != null){
                        onDownloadListener.onStart(url);
                    }
                })
                .subscribe(
                        file -> {
                            d("下载成功:" + name + "," + file.getPath());

                            //下载完成更新数据库
                            Advert advert = DaoManager.get().queryAdvertByNumber(advertNumber);
                            advert.setResult(1);
                            advert.setMessage("");
                            DaoManager.get().update(advert);

                            if (onDownloadListener != null) {
                                onDownloadListener.onComplete(advertNumber, file,true);
                            }
                        },
                        throwable -> {
                            d("下载错误：" + name + "," + throwable.getMessage());
                            //删除cache文件
                            if(FileUtils.isFileExists(cachePath)){
                                boolean delete = FileUtils.delete(cachePath);
                                d("删除cache文件：" + delete);
                            }

                            //设置错误信息并更新数据库，之后回调结果
                            Advert advert = DaoManager.get().queryAdvertByNumber(advertNumber);
                            advert.setResult(2);
                            advert.setMessage(throwable.getMessage());
                            DaoManager.get().update(advert);

                            if(onDownloadListener != null){
                                onDownloadListener.onError(throwable);
                            }
                        },
                        () -> {}
                );
    }

    private void deleteOriginCacheFile(){
        File systemDownloadDir = new File(Environment.getExternalStorageDirectory(),"download");
        boolean b = FileUtils.deleteFilesInDirWithFilter(systemDownloadDir, pathname -> pathname.getName().contains(name));
        d("删除原始cache文件：" + b + "," + systemDownloadDir.getPath());
    }

    private void deleteDestCacheFile(){
        boolean delete = FileUtils.delete(cachePath);
        d("删除目标cache文件：" + delete + "," + cachePath);
    }

    private void d(String log){
        Log.d(TAG,name + ":" + log);
//        Log.d(TAG, log);
    }


    public interface OnDownloadListener{
        void onStart(String url);
        void onComplete(String advertNumber, File file,boolean isDownload);
        void onError(Throwable throwable);
    }
}
