package com.janev.chongqing_bus_app.tcp.message;

import android.text.TextUtils;
import android.util.Log;

import com.janev.chongqing_bus_app.db.DaoManager;
import com.janev.chongqing_bus_app.db.Material;
import com.janev.chongqing_bus_app.db.Program;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

public class ResourceInfoReportRequest extends IRequest{
    private final String msgSerialHex;
    private final Runnable finishRunnable;
    private boolean isFirst = true;

    public ResourceInfoReportRequest(String msgSerialHex,Runnable runnable) {
        super("08");
        if(TextUtils.isEmpty(msgSerialHex)){
            this.msgSerialHex = "0000";
        } else {
            this.msgSerialHex = msgSerialHex;
        }
        this.finishRunnable = runnable;
    }

    @Override
    protected String getTag() {
        return "广告信息上报";
    }

    @Override
    protected String getHexData() {
        StringBuilder stringBuilder = new StringBuilder();

        String msgSerial = msgSerialHex;
        Log.d(TAG, "应答流水号: " + msgSerial);
        stringBuilder.append(msgSerial);

        String resourceID = MessageUtils.getResourceID();
        Log.d(TAG, "广告资源ID：" + resourceID);
        stringBuilder.append(resourceID);

        String resFileVersion = MessageUtils.getResourceVersion();

        String length = MessageUtils.getLength(resFileVersion);
        Log.d(TAG, "广告资源版本号长度：" + length);
        stringBuilder.append(length);

        Log.d(TAG, "广告资源版本号：" + resFileVersion);
        stringBuilder.append(resFileVersion);

        List<Program> programList = DaoManager.get().query(Program.class);
        String programNumber = Integer.toHexString(programList.size());
        programNumber = MessageUtils.addZero(programNumber,2);
        Log.d(TAG, "节目单个数：" + programNumber);
        stringBuilder.append(programNumber);

        for (Program program : programList) {
            String programId = MessageUtils.addZero(program.getId(), 16);
            Log.d(TAG, "节目单ID：" + programId);
            stringBuilder.append(programId);

            List<Material> materialList = DaoManager.get().queryMaterialByProgramId(programId);
            String materialNumber = Integer.toHexString(materialList.size());
            materialNumber = MessageUtils.addZero(materialNumber,2);
            Log.d(TAG, "节目单素材个数：" + materialNumber);
            stringBuilder.append(materialNumber);

            for (Material material : materialList) {
                String materialId = MessageUtils.addZero(material.getId(), 16);
                Log.d(TAG, "节目单素材ID：" + materialId);
                stringBuilder.append(materialId);

                String type = Integer.toHexString(material.getMaterialType());
                type = MessageUtils.addZero(type,2);
                Log.d(TAG, "节目单素材类型：" + type);
                stringBuilder.append(type);

                String playTotalTimes = Long.toHexString(material.getPlayTotalTimes());
                playTotalTimes = MessageUtils.addZero(playTotalTimes,4);
                Log.d(TAG, "节目单素材播放次数：" + playTotalTimes);
                stringBuilder.append(playTotalTimes);
            }
        }

        return stringBuilder.toString();
    }

    @Override
    public void setResult(int result) {
        super.setResult(result);
        if(isSuccess()){
            //响应成功先结束重发机制
            disposeResend();
            //响应成功启动30分钟等待机制
            waitSend();

            if(this.finishRunnable != null && isFirst){
                isFirst = false;
                this.finishRunnable.run();
            }
        }
    }

    @Override
    public void send() {
        super.send();
    }

    /**
     * 立即发送一次，然后每隔办小时发送一次
     */
    private Disposable resendDisposable;
    public void autoSend() {
        //发送时先结束30分钟等待机制
        disposableWaitSend();

        disposeResend();
        resendDisposable = Observable
                .interval(0,10,TimeUnit.SECONDS)
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> super.send(), new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Log.e(TAG, "accept: ",throwable);
                    }
                });
    }

    private static final String TAG = "ResourceInfoReportReque";

    private void disposeResend(){
        if(resendDisposable != null && !resendDisposable.isDisposed()){
            resendDisposable.dispose();
            resendDisposable = null;
        }
    }

    private Disposable waitDisposable;
    private void waitSend(){
        disposableWaitSend();
        waitDisposable = Observable
                .timer(30,TimeUnit.MINUTES)
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> send());
    }

    private void disposableWaitSend(){
        if(waitDisposable != null && !waitDisposable.isDisposed()){
            waitDisposable.dispose();
            waitDisposable = null;
        }
    }
}
