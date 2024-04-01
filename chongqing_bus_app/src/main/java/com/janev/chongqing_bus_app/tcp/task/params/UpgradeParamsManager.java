package com.janev.chongqing_bus_app.tcp.task.params;

import android.text.TextUtils;
import android.util.Log;

import com.janev.chongqing_bus_app.db.DaoManager;
import com.janev.chongqing_bus_app.system.Cache;
import com.janev.chongqing_bus_app.tcp.message.MessageUtils;
import com.janev.chongqing_bus_app.tcp.task.downloader.FtpDownloader;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class UpgradeParamsManager {
    private static final String TAG = "UpgradeParamsManager";

    private static final class Holder {
        public static final UpgradeParamsManager INSTANCE = new UpgradeParamsManager();
    }

    public static UpgradeParamsManager getInstance(){
        return UpgradeParamsManager.Holder.INSTANCE;
    }

    public void check(String msgSerial, boolean forceUpgrade, String newResourceId, String newVersionName, String ftpAddress, String ftpUserName, String ftpPassword) {
        if(isZero(newResourceId)){
            d("无资源文件");
            clearCache();
            return;
        }

        String cacheParamsId = MessageUtils.getParamsId();
        String cacheParamsVersion = MessageUtils.getParamsVersion();
        d("消息序列：" + msgSerial);
        d("强制更新：" + forceUpgrade);
        d("远程资源Id：" + newResourceId);
        d("远程资源版本：" + newVersionName);
        d("本地资源Id：" + cacheParamsId);
        d("本地资源版本：" + cacheParamsVersion);
        d("ftp地址：" + ftpAddress);
        d("ftp用户名：" + ftpUserName);
        d("ftp密码：" + ftpPassword);

        //强制更新/Id不同/版本号不同，则直接清库下载
        if(!TextUtils.equals(newResourceId,cacheParamsId) || !TextUtils.equals(newVersionName,cacheParamsVersion) || forceUpgrade){
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
    private void download(String msgSerial, String newResourceId, String newVersionName, String ftpAddress, String ftpUserName, String ftpPassword) {
        if(disposable != null && !disposable.isDisposed()){
            disposable.dispose();
            disposable = null;
        }
        disposable = new FtpDownloader(ftpAddress,ftpUserName,ftpPassword,"")
                .setListener(new ParamsDownloadListener(msgSerial,newResourceId,ftpAddress))
                .download()
                .map(new ParamsDataResolver())
                .map(s -> {
                    setCache(newResourceId,newVersionName,ftpAddress,ftpUserName,ftpPassword,s);
                    return true;
                })
                .subscribeOn(Schedulers.io())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) throws Exception {
                        Log.d(TAG, "accept: 设置完成");
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Log.e(TAG, "accept: 设置失败：", throwable);
                    }
                });

    }

    private static final String KEY_RESOURCE_CONTENT = "KEY_PARAMS_FILE_CONTENT";
    private static final String KEY_FTP_ADDRESS = "KEY_PARAMS_FTP_ADDRESS";
    private static final String KEY_FTP_USER = "KEY_PARAMS_FTP_USER";
    private static final String KEY_FTP_PASSWORD = "KEY_FTP_PASSWORD";
    private static void setCache(String newResourceId,String newVersion,String ftpAddress,String ftpUser,String ftpPassword,String string){
        MessageUtils.setParamsId(newResourceId);
        MessageUtils.setParamsVersion(newVersion);
        Cache.setString(KEY_RESOURCE_CONTENT,string);
        Cache.setString(KEY_FTP_ADDRESS,ftpAddress);
        Cache.setString(KEY_FTP_USER,ftpUser);
        Cache.setString(KEY_FTP_PASSWORD,ftpPassword);
    }
    private static void clearCache(){
        MessageUtils.removeParamsId();
        MessageUtils.removeParamsVersion();
        Cache.remove(KEY_RESOURCE_CONTENT);
        Cache.remove(KEY_FTP_ADDRESS);
        Cache.remove(KEY_FTP_USER);
        Cache.remove(KEY_FTP_PASSWORD);
    }

    public static boolean isZero(String resourceId){
        for (int i = 0; i < resourceId.length(); i++) {
            char c = resourceId.charAt(i);
            if(c != '0'){
                return false;
            }
        }
        return true;
    }

    private static void d(String l){
        Log.d(TAG, l);
    }
}
