package com.janev.chongqing_bus_app.tcp.task.resource;

import android.text.TextUtils;

import com.janev.chongqing_bus_app.db.Material;
import com.janev.chongqing_bus_app.tcp.message.UpgradeStateRequest;
import com.janev.chongqing_bus_app.tcp.task.UpgradeStateDownloadListener;
import com.janev.chongqing_bus_app.tcp.task.downloader.OnDownloadListener;

import java.io.File;

public class MaterialDownloadListener extends UpgradeStateDownloadListener {

    public MaterialDownloadListener(Material material,String msgSerial, String resourceId) {
        super(TYPE_RESOURCE,msgSerial,resourceId,material.getUrl());
    }
}
