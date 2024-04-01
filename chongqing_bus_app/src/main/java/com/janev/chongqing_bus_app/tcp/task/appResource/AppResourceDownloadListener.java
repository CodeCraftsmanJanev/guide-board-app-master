package com.janev.chongqing_bus_app.tcp.task.appResource;

import android.util.Log;

import com.blankj.utilcode.util.FileUtils;
import com.janev.chongqing_bus_app.tcp.message.UpgradeStateRequest;
import com.janev.chongqing_bus_app.tcp.task.UpgradeStateDownloadListener;
import com.janev.chongqing_bus_app.tcp.task.downloader.OnDownloadListener;

import java.io.File;

public class AppResourceDownloadListener extends UpgradeStateDownloadListener {

    public AppResourceDownloadListener(String msgSerial, String resourceId, String url) {
        super(TYPE_APP_RESOURCE, msgSerial, resourceId, url);
    }
}
