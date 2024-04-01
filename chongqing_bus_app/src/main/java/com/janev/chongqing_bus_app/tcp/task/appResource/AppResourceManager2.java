package com.janev.chongqing_bus_app.tcp.task.appResource;

import android.text.TextUtils;
import android.util.Log;

import com.blankj.utilcode.util.FileUtils;
import com.janev.chongqing_bus_app.db.DaoManager;
import com.janev.chongqing_bus_app.db.StartUpLogo;
import com.janev.chongqing_bus_app.db.StationPicture;
import com.janev.chongqing_bus_app.system.Cache;
import com.janev.chongqing_bus_app.system.Path;
import com.janev.chongqing_bus_app.tcp.message.MessageUtils;
import com.janev.chongqing_bus_app.tcp.task.resource.ResourceManager2;
import com.janev.chongqing_bus_app.tcp.task.downloader.FtpDownloader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class AppResourceManager2 {

    private static final String KEY_FTP_ADDRESS = "KEY_APP_RES_FTP_ADDRESS";
    private static final String KEY_FTP_USER_NAME = "KEY_APP_RES_FTP_USER_NAME";
    private static final String KEY_FTP_PASSWORD = "KEY_APP_RES_FTP_PASSWORD";
    private static final class Holder {
        public static final AppResourceManager2 INSTANCE = new AppResourceManager2();
    }

    private AppResourceManager2() {
    }

    public static AppResourceManager2 getInstance() {
        return AppResourceManager2.Holder.INSTANCE;
    }


    public void check(String msgSerial, boolean forceUpgrade, String newResourceId, String newVersionName, String ftpAddress, String ftpUserName, String ftpPassword) {
        if(ResourceManager2.isZero(newResourceId)){
            d("无资源文件");
            clearCache();
            return;
        }

        String cacheResourceId = MessageUtils.getAppResourceId();
        String cacheVersionName = MessageUtils.getAppResourceVersion();
        d("消息序列：" + msgSerial);
        d("强制更新：" + forceUpgrade);
        d("远程资源Id：" + newResourceId);
        d("远程资源版本：" + newVersionName);
        d("本地资源Id：" + cacheResourceId);
        d("本地资源版本：" + cacheVersionName);
        d("ftp地址：" + ftpAddress);
        d("ftp用户名：" + ftpUserName);
        d("ftp密码：" + ftpPassword);

        //强制更新/Id不同/版本号不同，则直接清库下载
        if(!TextUtils.equals(newResourceId,cacheResourceId) || !TextUtils.equals(newVersionName,cacheVersionName) || forceUpgrade){
            d("更新节目单：(强制更新/Id不同/版本号不同)");
            download(msgSerial,newResourceId,newVersionName,ftpAddress,ftpUserName,ftpPassword);
        }
        //都相同则检查数据库是否为空,为空则检查缓存的资源文件
        else if(DaoManager.get().queryUpdateStationPictures() <= 0){
            d("本地无节目单");
            download(msgSerial,newResourceId,newVersionName,ftpAddress,ftpUserName,ftpPassword);
        } else {
            d("节目单未更新，发送播放通知");
        }
    }

    private Disposable disposable;
    private void download(String msgSerial, String resourceId, String resourceVersion, String ftpAddress, String ftpUserName, String ftpPassword) {
        if(disposable != null && !disposable.isDisposed()){
            disposable.dispose();
            disposable = null;
        }
        disposable = new FtpDownloader(ftpAddress,ftpUserName,ftpPassword,"")
                .setListener(new AppResourceDownloadListener(msgSerial,resourceId,ftpAddress))
                .download()
                .map(new AppResourceDataResolver())
                .map(data -> {
                    setCache(resourceId, resourceVersion,ftpAddress,ftpUserName,ftpPassword);

                    saveDatabase(data.getStartUpLogo(),data.getStationPictures());

                    return data.getFile();
                })
                .map(file -> {
                    List<String> pathList = new ArrayList<>();
                    List<StartUpLogo> startUpLogoList = DaoManager.get().query(StartUpLogo.class);
                    for (StartUpLogo startUpLogo : startUpLogoList) {
                        String startUpLogoPath = Path.getStartUpLogoPath(startUpLogo);
                        pathList.add(startUpLogoPath);
                    }
                    List<StationPicture> stationPictureList = DaoManager.get().query(StationPicture.class);
                    for (StationPicture stationPicture : stationPictureList) {
                        String stationPicturePath = Path.getStationPicturePath(stationPicture);
                        pathList.add(stationPicturePath);
                    }

                    //检查文件夹，不存在的资源则删除
                    List<File> files = FileUtils.listFilesInDir(Path.getAppResourceDir());
                    ListIterator<File> fileListIterator = files.listIterator();
                    while (fileListIterator.hasNext()) {
                        File next = fileListIterator.next();
                        if(!pathList.contains(next.getPath())){
                            fileListIterator.remove();
                            FileUtils.delete(next);
                        }
                    }

                    Log.d(TAG, "download: " + FileUtils.listFilesInDir(Path.getAppResourceDir()).size());

                    return file;
                })
                .subscribeOn(Schedulers.io())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        file -> {
                            Log.d(TAG, "处理完成：" + file.getPath());
                            startDownload(msgSerial,resourceId);
                        },
                        throwable -> Log.e(TAG, "下载异常：", throwable)
                );
    }

    private AppResourceDownloader resourceDownloader;
    private void startDownload(String msgSerial,String resourceId){
        clearDownload();
        resourceDownloader = new AppResourceDownloader(msgSerial,resourceId);
        resourceDownloader.start();
    }
    private void clearDownload(){
        if(resourceDownloader != null){
            resourceDownloader.clear();
        }
    }

    private void setCache(String resourceId,String resourceVersion,final String ftpAddress, final String ftpUserName, final String ftpPassword){
        MessageUtils.setAppResourceId(resourceId);
        MessageUtils.setAppResourceVersion(resourceVersion);
        Cache.setString(KEY_FTP_ADDRESS,ftpAddress);
        Cache.setString(KEY_FTP_USER_NAME,ftpUserName);
        Cache.setString(KEY_FTP_PASSWORD,ftpPassword);
    }
    private void clearCache(){
        Cache.remove(KEY_FTP_ADDRESS);
        Cache.remove(KEY_FTP_USER_NAME);
        Cache.remove(KEY_FTP_PASSWORD);
        MessageUtils.removeAppResourceId();
        MessageUtils.removeAppResourceVersion();
    }

    private void saveDatabase(StartUpLogo startUpLogo,List<StationPicture> stationPictures){
        clearDownload();

        DaoManager.get().deleteAll(StartUpLogo.class);
        DaoManager.get().deleteAll(StationPicture.class);

        if(startUpLogo != null) DaoManager.get().addOrUpdate(startUpLogo);
        if(!stationPictures.isEmpty()) DaoManager.get().addOrUpdateStationPictures(stationPictures);
    }

    public StartUpLogo getStartUpLogo(){
        List<StartUpLogo> query = DaoManager.get().query(StartUpLogo.class);
        if(!query.isEmpty()){
            return query.get(0);
        }
        return null;
    }

    public StationPicture getStationPicture(String lineName,int upDown,int index){
        return DaoManager.get().queryStationPicture(lineName,upDown,index);
    }

    public static String getFtpAddress(){
        return Cache.getString(KEY_FTP_ADDRESS);
    }

    public static String getFtpUserName(){
        return Cache.getString(KEY_FTP_USER_NAME);
    }

    public static String getFtpPassword(){
        return Cache.getString(KEY_FTP_PASSWORD);
    }

    private static final String TAG = "AppResourceManager2";
    private void d(String l){
        Log.d(TAG, l);
    }
}
