package com.janev.chongqing_bus_app.tcp.task.resource;

import android.app.usage.StorageStatsManager;
import android.content.Context;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.text.TextUtils;
import android.util.Log;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.blankj.utilcode.util.UiMessageUtils;
import com.janev.chongqing_bus_app.App;
import com.janev.chongqing_bus_app.db.DaoManager;
import com.janev.chongqing_bus_app.db.Material;
import com.janev.chongqing_bus_app.db.Program;
import com.janev.chongqing_bus_app.db.Time;
import com.janev.chongqing_bus_app.system.Cache;
import com.janev.chongqing_bus_app.system.Path;
import com.janev.chongqing_bus_app.system.UiEvent;
import com.janev.chongqing_bus_app.tcp.message.MessageUtils;
import com.janev.chongqing_bus_app.tcp.task.downloader.FtpDownloader;
import com.janev.chongqing_bus_app.utils.L;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class ResourceManager2 {
    private static final String TAG = "ResourceManager2";

    private static final String DATE_PATTERN = "yyyy-MM-dd";
    private static final String TIME_PATTERN = "HH:mm:ss";
    public static Date string2Date(String string){
        return TimeUtils.string2Date(string, DATE_PATTERN);
    }
    public static String date2String(Date date){
        return TimeUtils.date2String(date,DATE_PATTERN);
    }
    public static String date2String(long mills){
        return TimeUtils.millis2String(mills,DATE_PATTERN);
    }
    public static Date string2Time(String string){
        return TimeUtils.string2Date(string,TIME_PATTERN);
    }
    public static String time2String(Date date){
        return TimeUtils.date2String(date,TIME_PATTERN);
    }
    public static String time2String(long mills){
        return TimeUtils.millis2String(mills,TIME_PATTERN);
    }
    public static Date getCurrDate(){
        return string2Date(date2String(new Date()));
//        return string2Date("2022-11-25");
    }
    public static Date getCurrTime(){
        return string2Time(time2String(new Date()));
//        return string2Time("14:00:00");
    }


    private static final String KEY_RESOURCE_SERIAL = "KEY_RESOURCE_SERIAL";
    private static final String KEY_RES_FILE_ID = "KEY_RES_FILE_ID";
    private static final String KEY_RES_FILE_VERSION = "KEY_RES_FILE_VERSION";
    private static final String KEY_RES_CONTENT = "KEY_RES_CONTENT";
    private static final String KEY_FTP_ADDRESS = "KEY_RES_FTP_ADDRESS";
    private static final String KEY_FTP_USER_NAME = "KEY_RES_FTP_USER_NAME";
    private static final String KEY_FTP_PASSWORD = "KEY_RES_FTP_PASSWORD";
    private static final class Holder {
        public static final ResourceManager2 INSTANCE = new ResourceManager2();
    }

    private ResourceManager2() {
    }

    public static ResourceManager2 getInstance() {
        return ResourceManager2.Holder.INSTANCE;
    }

    public void checkResource(String msgSerial, boolean forceUpgrade, String newResourceId, String newVersionName, String ftpAddress, String ftpUserName, String ftpPassword) {
        if(isZero(newResourceId)){
            d("无资源文件");
            clearCache();
            ResourceLocal.readFileFromStorage();
            return;
        }

        String cacheResourceId = MessageUtils.getResourceID();
        String cacheVersionName = MessageUtils.getResourceVersion();
        d("消息序列：" + msgSerial);
        d("强制更新：" + forceUpgrade);
        d("远程资源Id：" + newResourceId);
        d("远程资源版本：" + newVersionName);
        d("本地资源Id：" + cacheResourceId);
        d("本地资源版本：" + cacheVersionName);
        d("ftp地址：" + ftpAddress);
        d("ftp用户名：" + ftpUserName);
        d("ftp密码：" + ftpPassword);

        if(TextUtils.isEmpty(newVersionName) || TextUtils.isEmpty(ftpAddress)){
            d("下载地址为空");
            ResourceLocal.readFileFromStorage();
            return;
        }

        //强制更新/Id不同/版本号不同，则直接清库下载
        if(!TextUtils.equals(newResourceId,cacheResourceId) || !TextUtils.equals(newVersionName,cacheVersionName) || forceUpgrade){
            d("更新节目单：(强制更新/Id不同/版本号不同)");
            download(msgSerial,newResourceId,newVersionName,ftpAddress,ftpUserName,ftpPassword);
        }
        //都相同则检查数据库是否为空,为空则检查缓存的资源文件
        else if(DaoManager.get().queryProgramCount(cacheResourceId) <= 0){
            d("本地无节目单");
            download(msgSerial,newResourceId,newVersionName,ftpAddress,ftpUserName,ftpPassword);
        } else {
            d("节目单未更新，发送播放通知");
            String resourceID = MessageUtils.getResourceID();
            String resMsgSerial = getResMsgSerial();
            startDownload(resMsgSerial,resourceID);
        }
    }

    private Disposable disposable;
    private void download(final String msgSerial,String resourceId,String resourceVersion, final String ftpAddress, final String ftpUserName, final String ftpPassword){
        if(disposable != null && !disposable.isDisposed()){
            disposable.dispose();
            disposable = null;
        }
        disposable = new FtpDownloader(ftpAddress, ftpUserName, ftpPassword, "")
                .setListener(new ResourceDownloadListener(msgSerial, resourceId,ftpAddress))
                .download()
                .map(new ResourceDataResolver())
                .map(data -> {
                    Log.d(TAG, "解析后: " + data.toString());

                    //加缓存
                    setCache(msgSerial,resourceId, resourceVersion,
                            ftpAddress,ftpUserName,ftpPassword,
                            data.getResFileId(),data.getResFileVersion(),data.getString());

                    //加库
                    saveDatabase(data.getPrograms(),data.getMaterials(),data.getTimes());
                    return data;
                })
                .map(ResourceDataResolver.Data::getFile)
//                .map(file -> {
//                    List<String> pathList = new ArrayList<>();
//                    List<Material> materialList = DaoManager.get().query(Material.class);
//                    for (Material material : materialList) {
//                        String materialPath = Path.getMaterialPath(material);
//                        pathList.add(materialPath);
//                    }
//
//                    //检查文件夹，不存在的资源则删除
//                    List<File> files = FileUtils.listFilesInDir(Path.getMaterialDir());
//                    ListIterator<File> fileListIterator = files.listIterator();
//                    while (fileListIterator.hasNext()) {
//                        File next = fileListIterator.next();
//                        if(!pathList.contains(next.getPath())){
//                            fileListIterator.remove();
//                            FileUtils.delete(next);
//                        }
//                    }
//
//                    Log.d(TAG, "download: " + FileUtils.listFilesInDir(Path.getMaterialDir()).size());
//
//                    return file;
//                })
                .subscribeOn(Schedulers.io())
                .subscribeOn(AndroidSchedulers.mainThread())
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
                .subscribe(
                        file -> {
                            Log.d(TAG, "处理完成：" + file.getPath());
                            startDownload(msgSerial,resourceId);
                        },
                        throwable -> Log.e(TAG, "下载异常：", throwable)
                );
    }

    private ResourceDownloader resourceDownloader;
    private void startDownload(String msgSerial,String resourceId){
        clearDownload();
        resourceDownloader = new ResourceDownloader(msgSerial,resourceId);
        resourceDownloader.start();
    }
    private void clearDownload(){
        if(resourceDownloader != null){
            resourceDownloader.clear();
        }
    }

    private void setCache(String msgSerial,
                          String resourceId,String resourceVersion,
                          final String ftpAddress, final String ftpUserName, final String ftpPassword,
                          String resFileId,String resFileVersion,String string){
        MessageUtils.setResourceID(resourceId);
        MessageUtils.setResourceVersion(resourceVersion);

        Cache.setString(KEY_RESOURCE_SERIAL,msgSerial);
        Cache.setString(KEY_RES_FILE_ID,resFileId);
        Cache.setString(KEY_RES_FILE_VERSION,resFileVersion);
        Cache.setString(KEY_RES_CONTENT,string);
        Cache.setString(KEY_FTP_ADDRESS,ftpAddress);
        Cache.setString(KEY_FTP_USER_NAME,ftpUserName);
        Cache.setString(KEY_FTP_PASSWORD,ftpPassword);
    }

    private void clearCache(){
        Cache.remove(KEY_RES_FILE_ID);
        Cache.remove(KEY_RES_FILE_VERSION);
        Cache.remove(KEY_FTP_ADDRESS);
        Cache.remove(KEY_FTP_USER_NAME);
        Cache.remove(KEY_FTP_PASSWORD);
        MessageUtils.removeResourceId();
        MessageUtils.removeResourceVersion();
    }

    private void saveDatabase(List<Program> programs,List<Material> materials,List<Time> times){
        clearDownload();

        DaoManager.get().deleteAll(Program.class);
        DaoManager.get().deleteAll(Material.class);
        DaoManager.get().deleteAll(Time.class);

        if(!programs.isEmpty()) DaoManager.get().addOrUpdatePrograms(programs);
        if(!materials.isEmpty()) DaoManager.get().addOrUpdateMaterials(materials);
        if(!times.isEmpty()) DaoManager.get().addOrUpdateTimes(times);

        UiMessageUtils.getInstance().send(UiEvent.UPDATE_RESOURCE);
    }

    public static String getResMsgSerial(){
        return Cache.getString(KEY_RESOURCE_SERIAL,"0000");
    }
    public static String getResFileId(){
        return Cache.getString(KEY_RES_FILE_ID);
    }
    public static String getResFileVersion(){
        return Cache.getString(KEY_RES_FILE_VERSION);
    }
    public static String getFtpContent(){
        return Cache.getString(KEY_RES_CONTENT);
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

    public static boolean isZero(String resourceId){
        for (int i = 0; i < resourceId.length(); i++) {
            char c = resourceId.charAt(i);
            if(c != '0'){
                return false;
            }
        }
        return true;
    }

    public static int compareVersion(String version1, String version2) {
        if(TextUtils.isEmpty(version1) && TextUtils.isEmpty(version2)){
            return 0;
        } else if(TextUtils.isEmpty(version1)){
            return -1;
        } else if(TextUtils.isEmpty(version2)){
            return 1;
        }

        int size1 = version1.length(), size2 = version2.length();
        int size = Math.max(size1, size2);//选择两者中长的那个来做为停止条件
        for(int idx1 = 0,idx2 = 0;idx1<size || idx2<size;++idx1,++idx2)
        {//每次开始新的循环的时候，两者都会清0，因为我们只要当前位置的数字，
            //以前的没必要，反正比较过了
            int num1 = 0, num2 = 0;
            //下面两个while就用来转换成数字，而且就算段数不够，也会因为上面的赋值而补成0
            while(idx1<size1 && version1.charAt(idx1) != '.') num1 = num1*10+version1.charAt(idx1++)-'0';
            while(idx2<size2 && version2.charAt(idx2) != '.') num2 = num2*10+version2.charAt(idx2++)-'0';
            if(num1 > num2)return 1;//只要有一段不相等，就可以return了
            else if(num1 < num2)return -1;
        }
        return 0;//只有全部段都相等，才是真正的相等，所以得在转换完后才能知道
    }

    private void d(String l){
        L.tcp(TAG,l);
    }
}