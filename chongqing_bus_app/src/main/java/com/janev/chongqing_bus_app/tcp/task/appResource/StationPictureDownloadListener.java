package com.janev.chongqing_bus_app.tcp.task.appResource;

import com.janev.chongqing_bus_app.db.StationPicture;
import com.janev.chongqing_bus_app.tcp.task.UpgradeStateDownloadListener;

public class StationPictureDownloadListener extends UpgradeStateDownloadListener {
    public StationPictureDownloadListener(StationPicture stationPicture,String msgSerial, String resourceId) {
        super(TYPE_APP_RESOURCE,msgSerial,resourceId, stationPicture.getUrl());
    }
}
