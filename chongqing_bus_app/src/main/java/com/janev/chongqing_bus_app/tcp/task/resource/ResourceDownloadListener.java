package com.janev.chongqing_bus_app.tcp.task.resource;

import android.util.Log;

import com.blankj.utilcode.util.FileUtils;
import com.janev.chongqing_bus_app.tcp.message.UpgradeStateRequest;
import com.janev.chongqing_bus_app.tcp.task.UpgradeStateDownloadListener;
import com.janev.chongqing_bus_app.tcp.task.downloader.OnDownloadListener;

import java.io.File;

public class ResourceDownloadListener extends UpgradeStateDownloadListener {

    public ResourceDownloadListener(String msgSerial, String resourceId, String url) {
        super(TYPE_RESOURCE, msgSerial, resourceId, url);
    }
}
