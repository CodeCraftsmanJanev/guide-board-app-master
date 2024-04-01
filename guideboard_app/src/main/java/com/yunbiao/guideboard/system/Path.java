package com.yunbiao.guideboard.system;


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

        dir = new File(mainDir,cachePath);
        orExistsDir = FileUtils.createOrExistsDir(dir);
        cachePath = dir.getPath();
        d("缓存目录创建结果：" + orExistsDir);
        d("缓存目录：" + cachePath);
    }

    public static String getCrashDir() {
        return crashDir;
    }

    public static String getCachePath(){
        return cachePath;
    }

    private static void d(String msg){
        if(isLog){
            Log.d(TAG, msg);
        }
    }
}