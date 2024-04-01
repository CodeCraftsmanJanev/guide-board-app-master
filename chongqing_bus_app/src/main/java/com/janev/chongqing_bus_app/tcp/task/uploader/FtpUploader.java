package com.janev.chongqing_bus_app.tcp.task.uploader;

import android.util.Log;

import com.blankj.utilcode.util.NetworkUtils;
import com.janev.chongqing_bus_app.utils.L;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import io.reactivex.Observable;

public class FtpUploader extends Uploader{
    private static final String TAG = "FtpUploader";
    private final File file;
    private String ftpHost;
    private final List<String> ftpDirList;
    private final String userName;
    private final String password;

    public FtpUploader(File file, String ftpAddress, String userName, String password) {
        this.file = file;
        this.userName = userName;
        this.password = password;
        String[] split = ftpAddress.replace("ftp://", "").split("/");
        ftpDirList = new LinkedList<>();
        for (int i = 0; i < split.length; i++) {
            String string = split[i];
            if(i == 0){
                ftpHost = string;
            } else if(i == split.length - 1) {
                if(!string.contains(".")){
                    ftpDirList.add(string);
                }
            } else {
                ftpDirList.add(string);
            }
        }
    }

    @Override
    protected Observable<Boolean> implUpload() {
        return Observable
                .fromCallable(() -> {
                    if(!NetworkUtils.isAvailable()){
                        throw new Exception("network is unavailable");
                    }
                    return new Object();
                })
                .map(o -> {
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

                    boolean login = ftpClient.login(userName, password);
                    Log.d(TAG,"登录：" + login);
                    int replyCode = ftpClient.getReplyCode();
                    if (!FTPReply.isPositiveCompletion(replyCode)) {
                        ftpClient.disconnect();
                        throw new Exception("login ftp failed, " + replyCode);
                    }

                    //ftp client告诉ftp server开通一个端口来传输数据
                    ftpClient.enterLocalPassiveMode();
                    ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);

                    for (String dir : ftpDirList) {
                        if(ftpClient.changeWorkingDirectory(dir)){
                            Log.d(TAG,"转移到指定目录:" + dir);
                        } else if(ftpClient.makeDirectory(dir)){
                            Log.d(TAG, "创建目录：" + dir);
                            if(ftpClient.changeWorkingDirectory(dir)){
                                Log.d(TAG,"转移到指定目录:" + dir);
                            } else {
                                Log.d(TAG, "转移到指定目录失败：" + dir);
                                throw new Exception("change working directory failed");
                            }
                        } else {
                            Log.d(TAG, "创建目录失败：" + dir);
                            throw new Exception("make directory failed");
                        }
                    }

                    try(FileInputStream fileInputStream = new FileInputStream(file)){
                        try(InputStream is = new BufferedInputStream(fileInputStream)){
                            return ftpClient.storeFile(file.getName(), is);
                        }
                    }
                });
    }

    private void d(String log){
        L.tcp(TAG,log);
    }
}
