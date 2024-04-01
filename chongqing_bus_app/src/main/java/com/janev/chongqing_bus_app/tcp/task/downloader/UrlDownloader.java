package com.janev.chongqing_bus_app.tcp.task.downloader;

import android.util.Log;

import com.blankj.utilcode.util.CloseUtils;
import com.blankj.utilcode.util.FileUtils;
import com.janev.chongqing_bus_app.db.Material;
import com.janev.chongqing_bus_app.db.StationPicture;
import com.janev.chongqing_bus_app.system.Path;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.utils.HttpUtils;
import com.lzy.okrx2.adapter.ObservableResponse;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;

import io.reactivex.Observable;
import okhttp3.ResponseBody;

public class UrlDownloader extends Downloader {
    private static final String TAG = "UrlDownloader";
    String url;

    public UrlDownloader(Material material){
        this(material.getUrl(), Path.getMaterialPath(material));
    }
    public UrlDownloader(StationPicture stationPicture){
        this(stationPicture.getUrl(), Path.getStationPicturePath(stationPicture));
    }
    public UrlDownloader(String urlAddress,String destFilePath) {
        super(destFilePath);
        url = urlAddress;
    }

    @Override
    protected Observable<File> implDownload() {
        return OkGo.<File>get(url)
                .tag(url)
                .converter(response -> {
                    String url = response.request().url().toString();
                    String fileName = HttpUtils.getNetFileName(response, url);
                    Log.d(TAG, "convertResponse: 服务器文件名称：" + fileName);

                    ResponseBody body = response.body();
                    if(body == null){
                        throw new Exception("request file body is null");
                    }

                    long serverFileSize = body.contentLength();
                    Log.d(TAG, "convertResponse: 服务器文件大小：" + serverFileSize);

                    File tempFile = new File(tempDir,fileName);

                    long localFileSize = 0;
                    if(FileUtils.isFileExists(tempFile)){
                        localFileSize = tempFile.length();
                        if(localFileSize >= serverFileSize){
                            FileUtils.delete(tempFile);
                            localFileSize = 0;
                        }
                    }
                    Log.d(TAG, "convertResponse: 本地文件大小：" + localFileSize);

                    long currentSize = localFileSize;
                    int percent = 0;
                    BufferedInputStream bis = null;
                    BufferedOutputStream bos = null;
                    try {
                        InputStream inputStream = body.byteStream();
                        bis = new BufferedInputStream(inputStream);
                        bos = new BufferedOutputStream(new FileOutputStream(tempFile,true));

                        if(currentSize > 0){
                            long skip = bis.skip(currentSize);
                            Log.d(TAG, "convertResponse: 跳过字节：" + skip);
                        }

                        byte[] buffer = new byte[1024 * 2];
                        int bytesRead;
                        while ((bytesRead = bis.read(buffer, 0, 1024)) != -1) {
                            bos.write(buffer, 0, bytesRead);
                            currentSize += bytesRead;
//                    Log.d(TAG, "convertResponse: " + currentSize);
                            int p = BigDecimal.valueOf(currentSize)
                                    .divide(BigDecimal.valueOf(serverFileSize), 2, RoundingMode.HALF_UP)
                                    .multiply(BigDecimal.valueOf(100)).intValue();
                            if(p - percent >= 10){
                                percent = p;
                                Log.d(TAG, "impDownload: 下载进度：" + percent);
                                onProgress(percent);
                            }
                        }
                        bos.flush();
                        Log.d(TAG, "convertResponse: " + tempFile.getPath());
                        return tempFile;
                    } catch (Exception e){
                        Log.e(TAG, "convertResponse: " + e.getMessage());
                        throw new Exception("download failed: " + e.getMessage());
                    } finally {
                        CloseUtils.closeIOQuietly(bis);
                        CloseUtils.closeIOQuietly(bos);
                        CloseUtils.closeIOQuietly(body);
                    }
                })
                .adapt(new ObservableResponse<>())
                .map(fileResponse -> {
                    if(!fileResponse.isSuccessful()){
                        throw new Exception("download failed");
                    }
                    File body = fileResponse.body();
                    if(body == null){
                        throw new Exception("download file is null");
                    }
                    return body;
                });
    }
}
