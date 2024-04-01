package com.yunbiao.publicity_guideboard.system;


import android.os.Environment;
import android.util.Log;

import com.blankj.utilcode.util.FileUtils;

import java.io.File;

public class Path {
    private static final String TAG = "Path";
    private static boolean isLog = true;
    //主目录
    private static String mainDir = "guideBoardAPP";

    //Crash目录
    private static String crashDir = "hive-crash";

    //缓存目录
    private static String cachePath = "cache";

    //资源目录
    private static String resourcePath = "resource";

    //数据库目录
    private static String databasePath = "database";

    //日志目录
    private static String logPath = "bus_log";

    //头像
    private static String headPath = "head";

    /***
     * 初始化公共路径
     */
    public static void init(){
        File crash = new File(Environment.getExternalStorageDirectory(),Path.crashDir);
        boolean is = FileUtils.createOrExistsDir(crash);
        crashDir = crash.getPath();
        d("crash目录创建结果：" + is);
        d("crash目录：" + crashDir);

        File dir = new File(Environment.getExternalStorageDirectory(),Path.mainDir);
        boolean orExistsDir = FileUtils.createOrExistsDir(dir);
        mainDir = dir.getPath();
        d("主目录创建结果：" + orExistsDir);
        d("主目录：" + mainDir);

        dir = new File(mainDir,databasePath);
        orExistsDir = FileUtils.createOrExistsDir(dir);
        databasePath = dir.getPath();
        d("数据库目录创建结果：" + orExistsDir);
        d("数据库目录：" + databasePath);

        dir = new File(mainDir,cachePath);
        orExistsDir = FileUtils.createOrExistsDir(dir);
        cachePath = dir.getPath();
        d("缓存目录创建结果：" + orExistsDir);
        d("缓存目录：" + cachePath);

        dir = new File(mainDir,resourcePath);
        orExistsDir = FileUtils.createOrExistsDir(dir);
        resourcePath = dir.getPath();
        d("资源目录创建结果：" + orExistsDir);
        d("资源目录：" + resourcePath);

        dir = new File(mainDir,headPath);
        orExistsDir = FileUtils.createOrExistsDir(dir);
        headPath = dir.getPath();
        d("头像目录创建结果：" + orExistsDir);
        d("头像目录：" + resourcePath);

        dir = new File(Environment.getExternalStorageDirectory(),logPath);
        orExistsDir = FileUtils.createOrExistsDir(dir);
        logPath = dir.getPath();
        d("日志目录创建结果：" + orExistsDir);
        d("日志目录：" + logPath);
    }

    public static String getDatabasePath() {
        return databasePath;
    }

    public static String getHeadPath(){
        return headPath;
    }

    public static String getResourcePath() {
        return resourcePath;
    }

    public static String getCrashDir() {
        return crashDir;
    }

    public static String getCachePath(){
        return cachePath;
    }

    public static String getLogPath(){
        return logPath;
    }

    private static void d(String msg){
        if(isLog){
            Log.d(TAG, msg);
        }
    }
}