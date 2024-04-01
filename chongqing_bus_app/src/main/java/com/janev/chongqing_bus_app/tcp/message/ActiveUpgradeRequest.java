package com.janev.chongqing_bus_app.tcp.message;

import android.text.TextUtils;

import com.janev.chongqing_bus_app.utils.BytesUtils;

import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class ActiveUpgradeRequest extends IRequest{
    public static final String SYSTEM = "01";
    public static final String APP = "02";
    public static final String APP_RESOURCE = "03";
    public static final String DEVICE_PARAMS = "04";
    public static final String ADS_RESOURCE = "05";

    private String upgradeType;
    public ActiveUpgradeRequest(String upgradeType) {
        super("07");
        this.upgradeType = upgradeType;
    }

    @Override
    protected String getTag() {
        return "主动升级查询";
    }

    @Override
    protected String getHexData() {
        String QUERY_TYPE_HEX = this.upgradeType;

        String publicityIdHex = "0000000000000000";
        String publicityVersionHex = "";
        switch (QUERY_TYPE_HEX) {
            case APP:
                String string = MessageUtils.getAppResId();
                if(!TextUtils.isEmpty(string)){
                    publicityIdHex = string;
                }
                publicityVersionHex = MessageUtils.getAppVersionHex();
                break;
            case ADS_RESOURCE:
                string = MessageUtils.getResourceID();
                if(!TextUtils.isEmpty(string)){
                    publicityIdHex = string;
                }
                string = MessageUtils.getResourceVersion();
                if(!TextUtils.isEmpty(string)){
                    publicityVersionHex = BytesUtils.bytesToHex(string.getBytes(Charset.forName("gbk")));;
                }
                break;
            case APP_RESOURCE:
                string = MessageUtils.getAppResourceId();
                if(!TextUtils.isEmpty(string)){
                    publicityIdHex = string;
                }
                string = MessageUtils.getAppResourceVersion();
                if(!TextUtils.isEmpty(string)){
                    publicityVersionHex = BytesUtils.bytesToHex(string.getBytes(Charset.forName("gbk")));;
                }
                break;
        }

        String publicityVersionLengthHex = MessageUtils.getLength(publicityVersionHex);

        return QUERY_TYPE_HEX + publicityIdHex + publicityVersionLengthHex + publicityVersionHex;
    }

    @Override
    public void setResult(int result) {
        super.setResult(result);
        dispose();
    }

    public void dispose(){
        super.dispose();
        if(disposable != null && !disposable.isDisposed()){
            disposable.dispose();
            disposable = null;
        }
    }

    private Disposable disposable;
    public void sendWaitResult(){
        dispose();
        disposable = Observable
                .interval(0,10, TimeUnit.SECONDS)
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> super.send());
    }
}
