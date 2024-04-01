package com.janev.chongqing_bus_app.tcp.task.appResource;

import com.blankj.utilcode.util.FileUtils;
import com.janev.chongqing_bus_app.db.DaoManager;
import com.janev.chongqing_bus_app.db.StartUpLogo;
import com.janev.chongqing_bus_app.db.StationPicture;
import com.janev.chongqing_bus_app.system.Path;
import com.janev.chongqing_bus_app.tcp.task.downloader.Downloader;
import com.janev.chongqing_bus_app.tcp.task.downloader.FtpDownloader;
import com.janev.chongqing_bus_app.tcp.task.downloader.UrlDownloader;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class AppResourceDownloader {

    private final String msgSerial;
    private final String resourceId;
    private final List<StationPicture> stationPictureList;

    public AppResourceDownloader(String msgSerial, String resourceId) {
        this.msgSerial = msgSerial;
        this.resourceId = resourceId;
        List<StartUpLogo> startUpLogoList = DaoManager.get().query(StartUpLogo.class);
        stationPictureList = DaoManager.get().query(StationPicture.class);
        for (StartUpLogo startUpLogo : startUpLogoList) {
            StationPicture stationPicture = new StationPicture();
            stationPicture.setUrl(startUpLogo.getUrl());
            stationPicture.setName(startUpLogo.getName());
            stationPictureList.add(stationPicture);
        }
    }

    private Disposable disposable;
    public void start() {
        if(stationPictureList.isEmpty()){
            return;
        }
        if(disposable != null && !disposable.isDisposed()){
            return;
        }
        disposable = Observable.fromIterable(stationPictureList)
                .concatMap(stationPicture -> {
                    Downloader downloader;
                    //如果是FTP
                    if (Downloader.isFtp(stationPicture.getUrl())) {
                        downloader = new FtpDownloader(stationPicture,
                                AppResourceManager2.getFtpUserName(),
                                AppResourceManager2.getFtpPassword());
                    } else {
                        downloader = new UrlDownloader(stationPicture);
                    }
                    return downloader.setListener(new StationPictureDownloadListener(stationPicture,msgSerial,resourceId){
                        @Override
                        public boolean isAllComplete() {
                            return allComplete();
                        }
                    }).download();
                })
                .subscribeOn(Schedulers.io())
                .repeat()
                .subscribe(
                        file -> {
                            if(allComplete()){
                                clear();
                            }
                        },
                        throwable -> {

                        }
                );
    }

    private boolean allComplete(){
        int i = 0;
        for (StationPicture stationPicture : stationPictureList) {
            String stationPicturePath = Path.getStationPicturePath(stationPicture);
            if(FileUtils.isFileExists(stationPicturePath)){
                i += 1;
            }
        }
        return i == stationPictureList.size();
    }

    public void clear() {
        if(disposable != null && !disposable.isDisposed()){
            disposable.dispose();
            disposable = null;
        }
    }
}
