package com.janev.chongqing_bus_app.tcp.task.resource;

import android.text.TextUtils;
import android.util.Log;

import com.blankj.utilcode.util.FileUtils;
import com.janev.chongqing_bus_app.db.DaoManager;
import com.janev.chongqing_bus_app.db.Material;
import com.janev.chongqing_bus_app.system.Path;
import com.janev.chongqing_bus_app.tcp.StorageChecker;
import com.janev.chongqing_bus_app.tcp.task.downloader.Downloader;
import com.janev.chongqing_bus_app.tcp.task.downloader.FtpDownloader;
import com.janev.chongqing_bus_app.tcp.task.downloader.UrlDownloader;
import com.janev.chongqing_bus_app.utils.ProgramLoader;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class ResourceDownloader {

    private final String msgSerial;
    private final String resourceId;
    private final List<Material> materialList;

    public ResourceDownloader(String msgSerial,String resourceId) {
        this.msgSerial = msgSerial;
        this.resourceId = resourceId;
        this.materialList = ProgramLoader.loadDownloadList();
    }

    private static final String TAG = "ResourceDownloader";
    private Disposable disposable;
    public void start(){
        if(materialList.isEmpty()){
            Log.d(TAG, "start: 下载列表为空");
            return;
        }
        if(disposable != null && !disposable.isDisposed()){
            Log.d(TAG, "start: 下载正在进行");
            return;
        }
        disposable = Observable.fromIterable(materialList)
                .map(material -> {
                    StorageChecker.checkAndDelete(material.getSize());
                    return material;
                })
                .concatMap(material -> {
                    Log.d(TAG, "开始下载：" + material.getUrl());
                    Downloader downloader;
                    //如果是FTP
                    if (Downloader.isFtp(material.getUrl())) {
                        downloader = new FtpDownloader(material,
                                ResourceManager2.getFtpUserName(),
                                ResourceManager2.getFtpPassword());
                    } else {
                        downloader = new UrlDownloader(material);
                    }
                    return downloader.setListener(new MaterialDownloadListener(material,msgSerial,resourceId){
                                @Override
                                public boolean isAllComplete() {
                                    return allComplete();
                                }
                            })
                            .download()
                            .onErrorResumeNext(Observable.just(new File("")));
                })
                .subscribeOn(Schedulers.io())
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
//                .repeatWhen(objectObservable -> objectObservable.delay(3, TimeUnit.SECONDS))//三秒无限重试
                .subscribe(
                        file -> {
                            if(TextUtils.isEmpty(file.getPath())){
                                Log.d(TAG, "下载失败: ");
                            } else {
                                Log.d(TAG, "下载完成: " + file.getPath());
                                if(allComplete()){
                                    clear();
                                }
                            }
                        },
                        throwable -> {
                            Log.e(TAG, "下载失败: ", throwable);
                        }
                );
    }

    private boolean allComplete(){
        int i = 0;
        for (Material material : materialList) {
            String materialPath = Path.getMaterialPath(material);
            if(FileUtils.isFileExists(materialPath)){
                i += 1;
            }
        }
        Log.d(TAG, "allComplete: " + i +  " --- " + materialList.size());
        return i == materialList.size();
    }

    public void clear(){
        if(disposable != null && !disposable.isDisposed()){
            disposable.dispose();
            disposable = null;
        }
    }
}
