package com.yunbiao.publicity_guideboard;

import android.content.Context;

import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import com.blankj.utilcode.util.Utils;
import com.liulishuo.filedownloader.FileDownloader;
import com.liulishuo.filedownloader.connection.FileDownloadUrlConnection;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.db.DownloadManager;

public class App extends MultiDexApplication {

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;

        Utils.init(this);

        OkGo.getInstance().init(this);

        FileDownloader.setupOnApplicationOnCreate(this)
                .connectionCreator(new FileDownloadUrlConnection
                        .Creator(new FileDownloadUrlConnection.Configuration()
                        .connectTimeout(15_000) // set connection timeout.
                        .readTimeout(15_000) // set read timeout.
                ))
                .commit();

        /*// below codes just for monitoring thread pools in the FileDownloader:
        IThreadDebugger debugger = ThreadDebugger.install(
                ThreadDebuggers.create() *//** The ThreadDebugger with known thread Categories **//*
                        // add Thread Category
                        .add("OkHttp").add("okio").add("Binder")
                        .add(FileDownloadUtils.getThreadPoolName("Network"), "Network")
                        .add(FileDownloadUtils.getThreadPoolName("Flow"), "FlowSingle")
                        .add(FileDownloadUtils.getThreadPoolName("EventPool"), "Event")
                        .add(FileDownloadUtils.getThreadPoolName("LauncherTask"), "LauncherTask")
                        .add(FileDownloadUtils.getThreadPoolName("ConnectionBlock"), "Connection")
                        .add(FileDownloadUtils.getThreadPoolName("RemitHandoverToDB"), "RemitHandoverToDB")
                        .add(FileDownloadUtils.getThreadPoolName("BlockCompleted"), "BlockCompleted"),

                2000, *//** The frequent of Updating Thread Activity information **//*

                new ThreadDebugger.ThreadChangedCallback() {
                    *//**
                     * The threads changed callback
                     **//*
                    @Override
                    public void onChanged(IThreadDebugger debugger) {
                        // callback this method when the threads in this application has changed.
                        Log.d(TAG, debugger.drawUpEachThreadInfoDiff());
                        Log.d(TAG, debugger.drawUpEachThreadSizeDiff());
                        Log.d(TAG, debugger.drawUpEachThreadSize());
                    }
                });*/
    }

    public static Context getContext(){
        return context;
    }
}
