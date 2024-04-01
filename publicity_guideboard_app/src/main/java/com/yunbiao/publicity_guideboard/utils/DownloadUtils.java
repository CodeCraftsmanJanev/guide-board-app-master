package com.yunbiao.publicity_guideboard.utils;

import static com.yunbiao.publicity_guideboard.utils.PublicityManager.ADD_ADVERT;

import android.os.Environment;
import android.util.Log;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.UiMessageUtils;
import com.blankj.utilcode.util.Utils;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadLargeFileListener;
import com.liulishuo.filedownloader.FileDownloadList;
import com.liulishuo.filedownloader.FileDownloadListener;
import com.liulishuo.filedownloader.FileDownloadQueueSet;
import com.liulishuo.filedownloader.FileDownloadSampleListener;
import com.liulishuo.filedownloader.FileDownloader;
import com.liulishuo.filedownloader.model.FileDownloadStatus;
import com.yunbiao.publicity_guideboard.db.Advert;
import com.yunbiao.publicity_guideboard.system.Path;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DownloadUtils {
    private static final String TAG = "DownloadUtils";

    private final List<BaseDownloadTask> taskStatusList;

    private FileDownloadQueueSet fileDownloadQueueSet;

    private Runnable onFinishedRunnable;

    private Utils.Consumer<Advert> onSingleCompleteConsumer;

    private static final class Holder {
        public static final DownloadUtils INSTANCE = new DownloadUtils();
    }

    public static DownloadUtils getInstance(){
        return Holder.INSTANCE;
    }

    private DownloadUtils(){
        taskStatusList = new ArrayList<>();
    }

    public void setOnFinishedRunnable(Runnable runnable){
        this.onFinishedRunnable = runnable;
    }

    public void setOnSingleCompleteConsumer(Utils.Consumer<Advert> onSingleCompleteConsumer) {
        this.onSingleCompleteConsumer = onSingleCompleteConsumer;
    }

    public List<BaseDownloadTask> getDownloadTask(){
        return taskStatusList;
    }

    public void cancelAll(){
        try {
            fileDownloadQueueSet = null;
            FileDownloader.getImpl().clearAllTaskData();
        } catch (Exception e){
            e( "cancelAll: ", e);
        }
    }

    private List<BaseDownloadTask> transformToTask(List<Advert> advertList){
        List<BaseDownloadTask> taskList = new ArrayList<>();
        if(advertList != null && !advertList.isEmpty()){
            for (Advert advert : advertList) {
                taskList.add(FileDownloader.getImpl().create(advert.getFilePath()).setTag(advert).setPath(advert.getLocalPath()));
            }
        }
        return taskList;
    }

    public static boolean isFinished(BaseDownloadTask task){
        return task.getStatus() == FileDownloadStatus.completed
                || task.getStatus() == FileDownloadStatus.blockComplete
                || task.getStatus() == FileDownloadStatus.error
                || task.getStatus() == FileDownloadStatus.warn;
    }

    private void checkAllStatus(){
        boolean hasRunning = false;
        for (BaseDownloadTask task : taskStatusList) {
            d( "checkAllStatus: " + task.getStatus());
            if (isFinished(task)) {
                continue;
            }
            hasRunning = true;
            break;
        }

        if(hasRunning){
            d( "checkAllStatus: 有任务正在进行");
        } else {
            d( "checkAllStatus: 已全部完成");

            if(this.onFinishedRunnable != null){
                this.onFinishedRunnable.run();
            }
        }
    }

    public void start(List<Advert> advertList){
        cancelAll();

        List<BaseDownloadTask> taskList = transformToTask(advertList);

        taskStatusList.clear();
        taskStatusList.addAll(taskList);

        if(taskList.isEmpty()){
            return;
        }

        fileDownloadQueueSet = new FileDownloadQueueSet(fileDownloadListener)
//                .setTag(this.getClass().getName()) //全局tag
//                .disableCallbackProgressTimes() //禁止单独回调
                .setAutoRetryTimes(3) //重试次数
                .setCallbackProgressMinInterval(1000)
                .setCallbackProgressTimes(500)
                .addTaskFinishListener(task -> checkAllStatus()) //下载结束监听
                .setDirectory(Path.getResourcePath()) //全局下载路径
                .downloadTogether(taskList); //同时开始

        fileDownloadQueueSet.start();
    }

    private final FileDownloadLargeFileListener fileDownloadListener = new FileDownloadLargeFileListener(){

        @Override
        protected void pending(BaseDownloadTask task, long soFarBytes, long totalBytes) {
            i( "pending: " + task.getUrl() + " --- " + task.getLargeFileSoFarBytes() + " , " + task.getLargeFileTotalBytes());
        }

        @Override
        protected void progress(BaseDownloadTask task, long soFarBytes, long totalBytes) {
            i( "progress: " + task.getUrl() + " --- " + task.getLargeFileSoFarBytes() + " , " + task.getLargeFileTotalBytes());
        }

        @Override
        protected void paused(BaseDownloadTask task, long soFarBytes, long totalBytes) {
            i( "paused: " + task.getFilename() + " --- " + task.getLargeFileSoFarBytes() + " , " + task.getLargeFileTotalBytes());
        }

        @Override
        protected void completed(BaseDownloadTask task) {
            i( "completed: " + task.getTargetFilePath() + " --- " + FileUtils.isFileExists(task.getTargetFilePath()));
            if(task.getTag() != null
                    && task.getTag() instanceof Advert
                    && onSingleCompleteConsumer != null){
                onSingleCompleteConsumer.accept((Advert) task.getTag());
            }
        }

        @Override
        protected void error(BaseDownloadTask task, Throwable e) {
            e( "error: " + task.getFilename(),e);
        }

        @Override
        protected void warn(BaseDownloadTask task) {
            w( "warn: " + task.getFilename());
        }
    };

    private void i(String log){
        LogUtils.i(TAG,TAG + ":" + log);
//        Log.i(TAG, "i: " + log);
    }

    private void w(String log){
        LogUtils.w(TAG,TAG + ":" + log);
//        Log.w(TAG, "w: " + log);
    }

    private void d(String log){
        LogUtils.d(TAG,TAG + ":" + log);
//        Log.d(TAG, "d: " + log);
    }

    private void e(String log,Throwable throwable){
        LogUtils.e(TAG,TAG + ":" + log,throwable);
//        Log.e(TAG, "e: " + log,throwable);
    }

}
