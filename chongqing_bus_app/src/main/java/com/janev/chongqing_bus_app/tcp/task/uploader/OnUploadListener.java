package com.janev.chongqing_bus_app.tcp.task.uploader;

import java.io.File;

public interface OnUploadListener {
    void onStart();
    void onProgress(int percent);
    void onComplete(Boolean b, boolean isDownload);
    void onError(Throwable throwable);
}