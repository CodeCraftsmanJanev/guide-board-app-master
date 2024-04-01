package com.janev.chongqing_bus_app.tcp.task.app;

import android.util.Log;

import com.blankj.utilcode.util.FileUtils;
import com.janev.chongqing_bus_app.tcp.message.UpgradeStateRequest;
import com.janev.chongqing_bus_app.tcp.task.UpgradeStateDownloadListener;
import com.janev.chongqing_bus_app.tcp.task.downloader.OnDownloadListener;

import java.io.File;

public class AppDownloadListener extends UpgradeStateDownloadListener {
    private static final String TAG = "AppDownloadListener";

    public AppDownloadListener(String msgSerial, String resourceId, String url) {
        super(TYPE_APP, msgSerial, resourceId, url);
    }

    @Override
    public void onComplete(File file, boolean isDownload) {
        sendRequest(COMPLETE);

        if(file.getPath().endsWith(".apk")) {
            sendRequest(SUCCESS);
        } else {
            sendRequest(FAILED);
        }
    }
}
