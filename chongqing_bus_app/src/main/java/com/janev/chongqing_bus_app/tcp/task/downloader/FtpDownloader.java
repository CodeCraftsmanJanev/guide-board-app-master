package com.janev.chongqing_bus_app.tcp.task.downloader;

import android.text.TextUtils;
import android.util.Log;

import com.blankj.utilcode.util.NetworkUtils;
import com.janev.chongqing_bus_app.db.Material;
import com.janev.chongqing_bus_app.db.StationPicture;
import com.janev.chongqing_bus_app.system.Path;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedList;

import io.reactivex.Observable;

public class FtpDownloader extends Downloader {
    private static final String TAG = "FtpDownloader";
    String ftpHost;
    LinkedList<String> ftpDirList;
    String ftpFileName;
    String ftpUserName;
    String ftpPassword;
    String destFilePath;

    public FtpDownloader(Material material,String ftpUserName, String ftpPassword){
        this(material.getUrl(),ftpUserName,ftpPassword, Path.getMaterialPath(material));
    }

    public FtpDownloader(StationPicture stationPicture, String ftpUserName, String ftpPassword){
        this(stationPicture.getUrl(),ftpUserName,ftpPassword, Path.getStationPicturePath(stationPicture));
    }

    public FtpDownloader(String ftpAddress,String ftpUserName, String ftpPassword, String destFilePath) {
        super(destFilePath);
        String[] split = ftpAddress.replace("ftp://", "").split("/");
        ftpDirList = new LinkedList<>();
        for (int i = 0; i < split.length; i++) {
            String string = split[i];
            if(i == 0){
                ftpHost = string;
            } else if(i == split.length - 1){
                ftpFileName = string;
            } else {
                ftpDirList.add(string);
            }
        }
        this.ftpUserName = ftpUserName;
        this.ftpPassword = ftpPassword;
        this.destFilePath = destFilePath;
        Log.d(TAG, "host: " + ftpHost);
        Log.d(TAG, "dirs: " + ftpDirList);
        Log.d(TAG, "ftpFileName: " + ftpFileName);
    }

    @Override
    protected Observable<File> implDownload(){
        return Observable
                .fromCallable(() -> {
                    if(!NetworkUtils.isAvailable()){
                        throw new Exception("network is unavailable");
                    }
                    return new Object();
                })
                .map(object -> {
                    final FTPClient ftpClient = new FTPClient();
                    ftpClient.setControlEncoding("GBK");

                    //如果采用默认端口，可以使用ftp.connect(url)的方式直接连接FTP服务器
                    if(ftpHost.contains(":")){
                        String[] split = ftpHost.split(":");
                        Log.d(TAG, "apply: 连接地址：" + split[0] + " ,端口：" + split[1]);
                        ftpClient.connect(split[0],Integer.parseInt(split[1]));
                    } else {
                        Log.e(TAG, "apply: 连接地址" + ftpHost);
                        ftpClient.connect(ftpHost);
                    }

                    boolean login = ftpClient.login(ftpUserName, ftpPassword);
                    Log.d(TAG,"登录：" + login);
                    int replyCode = ftpClient.getReplyCode();
                    if (!FTPReply.isPositiveCompletion(replyCode)) {
                        ftpClient.disconnect();
                        throw new Exception("login ftp failed, " + replyCode);
                    }

                    //ftp client告诉ftp server开通一个端口来传输数据
                    ftpClient.enterLocalPassiveMode();

                    for (String dir : ftpDirList) {
                        boolean result = ftpClient.changeWorkingDirectory(dir);
                        Log.d(TAG,"转移到指定目录:" + result);
                        if (!result) {
                            throw new Exception("change working directory failed");
                        }
                    }

                    //检索文件
                    FTPFile ftpFile = null;
                    FTPFile[] ftpFiles = ftpClient.listFiles();
                    for (FTPFile ff : ftpFiles) {
                        if (TextUtils.equals(ff.getName(), ftpFileName)) {
                            ftpFile = ff;
                            break;
                        }
                    }
                    if (ftpFile == null) {
                        Log.d(TAG,"未找到该文件");
                        throw new Exception("don't have this file:" + ftpFileName);
                    }

                    ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

                    File tempFile = new File(tempDir,ftpFile.getName());

                    //判断是否可以断点下载
                    long serverFileSize = ftpFile.getSize();
                    Log.d(TAG, "impDownload: 服务器文件大小：" + serverFileSize);
                    long localFileSize = 0;
                    if (tempFile.exists()) {
                        localFileSize = tempFile.length();
                        if (localFileSize >= serverFileSize) {
                            tempFile.delete();
                            localFileSize = 0;
                        }
                    }
                    Log.d(TAG, "impDownload: 本地文件大小：" + localFileSize);

                    //设置断点下载
                    if(localFileSize > 0){
                        ftpClient.setRestartOffset(localFileSize);
                    }

                    long currentSize = localFileSize;
                    int percent = 0;
                    BufferedInputStream bis = null;
                    BufferedOutputStream bos = null;
                    try {
                        InputStream inputStream = ftpClient.retrieveFileStream(ftpFile.getName());
                        bis = new BufferedInputStream(inputStream);
                        bos = new BufferedOutputStream(new FileOutputStream(tempFile,true));
                        byte[] buffer = new byte[1024 * 8];
                        int bytesRead;
                        while ((bytesRead = bis.read(buffer, 0, 1024)) != -1) {
                            bos.write(buffer, 0, bytesRead);
                            currentSize += bytesRead;
                            int p = BigDecimal.valueOf(currentSize)
                                    .divide(BigDecimal.valueOf(serverFileSize), 2, RoundingMode.HALF_UP)
                                    .multiply(BigDecimal.valueOf(100)).intValue();
                            if(p - percent >= 10){
                                percent = p;
                                Log.d(TAG, "impDownload: 下载进度：" + percent);
                                onProgress(percent);
                            }
                        }
                        boolean downloadSuccess = ftpClient.completePendingCommand();
                        Log.d(TAG, "impDownload: 下载结果：" + downloadSuccess);
                        if(downloadSuccess){
                            Log.d(TAG, "impDownload: " + tempFile.getPath() + " --- " + tempFile.exists());
                            return  tempFile;
                        } else {
                            throw new Exception("ftpClient completePendingCommand is false");
                        }
                    } catch (Exception e){
                        Log.e(TAG, "impDownload: " + e.getMessage());
                        throw new Exception("download failed: " + e.getMessage());
                    } finally {
                        if(bis != null){
                            bis.close();
                        }
                        if(bos != null){
                            bos.close();
                        }
                        if(ftpClient.isConnected()){
                            ftpClient.logout();
                            ftpClient.disconnect();
                        }
                    }
                });
    }
}
