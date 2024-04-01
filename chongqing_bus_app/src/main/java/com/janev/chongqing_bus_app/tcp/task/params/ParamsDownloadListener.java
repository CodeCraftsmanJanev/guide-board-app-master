package com.janev.chongqing_bus_app.tcp.task.params;

import com.janev.chongqing_bus_app.tcp.task.UpgradeStateDownloadListener;

public class ParamsDownloadListener extends UpgradeStateDownloadListener {

    public ParamsDownloadListener(String msgSerial, String resourceId, String url) {
        super(TYPE_DEVICE_PARAMS, msgSerial, resourceId, url);
    }
}
