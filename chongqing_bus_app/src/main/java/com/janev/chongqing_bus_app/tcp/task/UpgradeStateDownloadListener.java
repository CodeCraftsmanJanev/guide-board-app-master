package com.janev.chongqing_bus_app.tcp.task;

import android.text.TextUtils;

import com.blankj.utilcode.util.FileUtils;
import com.janev.chongqing_bus_app.tcp.message.UpgradeStateRequest;
import com.janev.chongqing_bus_app.tcp.task.downloader.OnDownloadListener;

import java.io.File;

public class UpgradeStateDownloadListener implements OnDownloadListener {

    protected static final String TYPE_FIRMWARE = "01";
    protected static final String TYPE_APP = "02";
    protected static final String TYPE_APP_RESOURCE = "03";
    protected static final String TYPE_DEVICE_PARAMS = "04";
    protected static final String TYPE_RESOURCE = "05";

    protected static final String START = "00";
    protected static final String PROGRESS = "01";
    protected static final String COMPLETE = "02";
    protected static final String ERROR = "03";
    protected static final String SUCCESS = "06";
    protected static final String FAILED = "07";

    private final String type,msgSerial,resourceId,fileName;
    private int progress = 0;

    public UpgradeStateDownloadListener(String type,String msgSerial, String resourceId,String url) {
        this.type = type;
        if(TextUtils.isEmpty(msgSerial)){
            this.msgSerial = "0000";
        } else {
            this.msgSerial = msgSerial;
        }
        if(TextUtils.isEmpty(resourceId)){
            this.resourceId = "0000000000000000";
        } else {
            this.resourceId = resourceId;
        }
        this.fileName = FileUtils.getFileName(url);
    }

    @Override
    public void onStart() {
        sendRequest(START);
    }

    @Override
    public void onProgress(int percent) {
        progress = percent;
        sendRequest(PROGRESS);
    }

    @Override
    public void onComplete(File file, boolean isDownload) {
        progress = 100;
        sendRequest(COMPLETE);

        if(isAllComplete()){
            sendRequest(SUCCESS);
        }
    }

    @Override
    public void onError(Throwable throwable) {
        sendRequest(ERROR);

        if(isAllComplete()){
            sendRequest(SUCCESS);
        }
    }

    protected void sendRequest(String pro){
        new UpgradeStateRequest(msgSerial,type,resourceId,pro,fileName, progress).send();
    }

    public boolean isAllComplete(){
        return true;
    }

}
