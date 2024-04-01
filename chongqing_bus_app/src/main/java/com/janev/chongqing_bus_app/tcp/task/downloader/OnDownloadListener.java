package com.janev.chongqing_bus_app.tcp.task.downloader;

import com.janev.chongqing_bus_app.db.Material;

import java.io.File;

public interface OnDownloadListener{
    void onStart();
    void onProgress(int percent);
    void onComplete(File file, boolean isDownload);
    void onError(Throwable throwable);
}