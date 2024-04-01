package com.janev.chongqing_bus_app.system;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.blankj.utilcode.util.FileUtils;
import com.janev.chongqing_bus_app.db.Material;
import com.janev.chongqing_bus_app.db.StartUpLogo;
import com.janev.chongqing_bus_app.db.StationPicture;

import java.io.File;
import java.io.FileFilter;
import java.util.List;

public enum Path {
    ROOT("",null, Environment.getExternalStorageDirectory().getPath()),

    APP_ROOT("bus_app",ROOT),
    BUS_RESOURCE("bus_resource",ROOT),

    CACHE("cache",APP_ROOT),
    DATABASE("database",APP_ROOT),
    TEMP("temp",APP_ROOT),
    SCREENSHOT("screenShot",APP_ROOT),
    RESOURCE("resource",APP_ROOT),
    LOG("log",APP_ROOT),


    MATERIAL("material",RESOURCE),
    APP_RESOURCE("appResource",RESOURCE),

    CRASH("hive-crash",LOG),
    SERIAL_LOG("serial",LOG),
    APP_LOG("app",LOG),
    RUN_LOG("run",LOG),

    ;

    private final String name;
    private final Path parent;
    private String path;
    Path(String name, Path parent) {
        this(name,parent,"");
    }
    Path(String name, Path parent, String path) {
        this.name = name;
        this.parent = parent;
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    private static final String TAG = "PathE";
    public static void init(){
        Path[] values = values();
        for (Path value : values) {
            if(TextUtils.isEmpty(value.name)
                    || (!TextUtils.isEmpty(value.getPath()) && FileUtils.isFileExists(value.getPath()))){
                Log.d(TAG, "跳过创建：" + value.name());
                continue;
            }
            File dir = new File(value.parent.getPath(),value.name);
            boolean orExistsDir = FileUtils.createOrExistsDir(dir);
            if (orExistsDir) {
                value.setPath(dir.getAbsolutePath());
            }
            Log.d(TAG, "创建目录：" + value.parent.name + "/" + value.name + " --- " + orExistsDir);
        }
    }

    public static String getCrashDir() {
        return CRASH.getPath();
    }

    public static String getCachePath(){
        return CACHE.getPath();
    }

    public static String getDatabasePath() {
        return DATABASE.getPath();
    }

    public static String getTempDir() {
        return TEMP.getPath();
    }

    public static String getResourceDir() {
        return RESOURCE.getPath();
    }

    public static String getMaterialDir() {
        return MATERIAL.getPath();
    }

    public static String getAppResourceDir() {
        return APP_RESOURCE.getPath();
    }

    public static String getScreenShotPath() {
        return SCREENSHOT.getPath();
    }

    public static String getLogPath(){
        return LOG.getPath();
    }

    public static String getSerialLogPath(){
        return SERIAL_LOG.getPath();
    }

    public static String getAppLogPath(){
        return APP_LOG.getPath();
    }

    public static String getRunLogPath(){
        return RUN_LOG.getPath();
    }

    public static String getMaterialNameNoExtension(Material material){
        return FileUtils.getFileNameNoExtension(material.getUrl());
    }

    public static String getMaterialPath(Material material){
        String nameNoExtension = getMaterialNameNoExtension(material);
        List<File> files = FileUtils.listFilesInDirWithFilter(getMaterialDir(), pathname -> pathname.getName().contains(nameNoExtension));
        if(files.isEmpty()){
            return new File(getMaterialDir(), nameNoExtension).getPath();
        } else {
            return files.get(0).getAbsolutePath();
        }
    }

    public static String getLocalMaterialPath(File dir,Material material){
        String nameNoExtension = getMaterialNameNoExtension(material);
        List<File> files = FileUtils.listFilesInDirWithFilter(dir, pathname -> pathname.getName().contains(nameNoExtension));
        if(files.isEmpty()){
            return new File(getMaterialDir(), nameNoExtension).getPath();
        } else {
            return files.get(0).getAbsolutePath();
        }
    }

    public static String getStartUpLogoPath(StartUpLogo start){
        return getAppResourcePath(start.getUrl());
    }
    public static String getStationPicturePath(StationPicture stationPicture){
        return getAppResourcePath(stationPicture.getUrl());
    }
    public static String getAppResourcePath(String url){
        String nameNoExtension = FileUtils.getFileNameNoExtension(url);
        List<File> files = FileUtils.listFilesInDirWithFilter(getAppResourceDir(), pathname -> pathname.getName().contains(nameNoExtension));
        if(files.isEmpty()){
            return new File(getMaterialDir(), nameNoExtension).getPath();
        } else {
            return files.get(0).getAbsolutePath();
        }
    }

    public static String getBusResource(){
        return BUS_RESOURCE.getPath();
    }
}
