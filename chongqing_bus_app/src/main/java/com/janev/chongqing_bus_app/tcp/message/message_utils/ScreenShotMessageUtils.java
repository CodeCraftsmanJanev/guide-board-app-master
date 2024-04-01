package com.janev.chongqing_bus_app.tcp.message.message_utils;

import android.util.Log;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.FileUtils;
import com.janev.chongqing_bus_app.system.Path;
import com.janev.chongqing_bus_app.tcp.message.MessageUtils;
import com.janev.chongqing_bus_app.tcp.message.ReplyRequest;
import com.janev.chongqing_bus_app.tcp.message.ScreenShotRequest;
import com.janev.chongqing_bus_app.tcp.task.uploader.FtpUploader;
import com.janev.chongqing_bus_app.tcp.task.uploader.OnUploadListener;
import com.janev.chongqing_bus_app.utils.BytesUtils;
import com.janev.chongqing_bus_app.utils.L;
import com.janev.chongqing_bus_app.utils.SmdtUtils;
import com.janev.chongqing_bus_app.utils.StringUtils;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class ScreenShotMessageUtils {
    private static final String TAG = "ScreenShotMessageUtils";

    public static void screenShot(byte order, String msgSerial, ByteBuffer byteBuffer){
        new ReplyRequest(BytesUtils.byteToHex(order), msgSerial,"00").send();

        byte shotType = byteBuffer.get();
        Log.d(TAG, "截图类型");

        int fileIdLength = MessageUtils.getByteInt(byteBuffer);
        Log.d(TAG, "文件ID长度：" + fileIdLength);

        String fileId = MessageUtils.getBytesHex(byteBuffer, fileIdLength);
        fileId = StringUtils.hexStringToString(fileId);
        Log.d(TAG, "文件ID：" + fileId);

        int ftpAddressLength = MessageUtils.getByteInt(byteBuffer);
        Log.d(TAG, "ftp地址长度：" + ftpAddressLength);

        String ftpAddress = MessageUtils.getBytesHex(byteBuffer, ftpAddressLength);
        ftpAddress = StringUtils.hexStringToString(ftpAddress);
        Log.d(TAG, "ftp地址：" + ftpAddress);

        int ftpUserLength = MessageUtils.getByteInt(byteBuffer);
        Log.d(TAG, "ftp用户名长度：" + ftpUserLength);

        String ftpUser = MessageUtils.getBytesHex(byteBuffer, ftpUserLength);
        ftpUser = StringUtils.hexStringToString(ftpUser);
        Log.d(TAG, "ftp用户名：" + ftpUser);

        int ftpPasswordLength = MessageUtils.getByteInt(byteBuffer);
        Log.d(TAG, "ftp密码长度：" + ftpPasswordLength);

        String ftpPassword = MessageUtils.getBytesHex(byteBuffer, ftpPasswordLength);
        ftpPassword = StringUtils.hexStringToString(ftpPassword);
        Log.d(TAG, "ftp密码：" + ftpPassword);

        if(shotType == (byte)0x00 || shotType == (byte) 0x02){
            screenShot(msgSerial,shotType,fileId, ftpAddress, ftpUser, ftpPassword);
        } else {
            reply(msgSerial,shotType,fileId,"",2);
        }
    }

    private static Disposable disposable;
    private static void screenShot(String msgSerial,byte shotType,String fileID,String ftpAddress,String ftpUser,String ftpPassword){
        File destFile = new File(Path.getScreenShotPath(), fileID + ".jpg");
        if(disposable != null && !disposable.isDisposed()){
            disposable.dispose();
            disposable = null;
        }
        disposable = Observable
                .fromCallable(() -> {
                    SmdtUtils.getInstance().screenShotToFile(destFile.getPath(),"",ActivityUtils.getTopActivity());
                    return destFile;
                })
                .flatMap((Function<File, ObservableSource<File>>) file ->
                        //循环检测文件是否存在
                        Observable
                                .fromCallable(() -> {
                                    boolean fileExists = FileUtils.isFileExists(file);
                                    d("检测文件是否存在：" + fileExists);
                                    return fileExists;
                                })
                                .flatMap((Function<Boolean, ObservableSource<File>>) aBoolean -> {
                                    if(aBoolean){
                                        return Observable.just(file);
                                    }
                                    return Observable.error(new Throwable("未检测到文件"));
                                })
                                .retryWhen(new Retry())
                )
                .flatMap(file -> new FtpUploader(file,ftpAddress,ftpUser,ftpPassword).setListener(new OnUploadListener() {
                    @Override
                    public void onStart() {
                        Log.d(TAG, "onStart: ");
                    }

                    @Override
                    public void onProgress(int percent) {
                        Log.d(TAG, "onProgress: " + percent);
                    }

                    @Override
                    public void onComplete(Boolean b, boolean isDownload) {
                        Log.d(TAG, "onComplete: " + b);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        Log.e(TAG, "onError: ", throwable);
                    }
                }).upload().doOnTerminate(() -> FileUtils.delete(file)))
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(
                        aBoolean -> reply(msgSerial, shotType, fileID, destFile.getName(), aBoolean ? 0 : 3),
                        throwable -> reply(msgSerial,shotType,fileID,destFile.getName(),1)
                );
    }

    private static class Retry implements Function<Observable<? extends Throwable>, ObservableSource<?>>{
        private int retryTimes = 0;
        private final int MAX_TIMES = 10;

        @Override
        public ObservableSource<?> apply(Observable<? extends Throwable> observable) throws Exception {
            return observable.flatMap((Function<Throwable, ObservableSource<?>>) throwable -> {
                d("请求失败：" + throwable.getMessage());
                if(++ retryTimes <= MAX_TIMES){
                    return Observable.timer(1, TimeUnit.SECONDS);
                }
                return Observable.error(throwable);
            });
        }
    }

    private static void reply(String msgSerial,byte TYPE,String fileID,String fileName,int replyResult){
        new ScreenShotRequest(msgSerial, TYPE, fileID, fileName, replyResult).send();
    }

    private static void d(String log){
        L.tcp(TAG,log);
    }

}
