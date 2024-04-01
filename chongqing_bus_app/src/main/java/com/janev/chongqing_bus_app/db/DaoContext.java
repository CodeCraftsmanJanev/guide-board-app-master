package com.janev.chongqing_bus_app.db;

import android.content.Context;
import android.content.ContextWrapper;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.IOException;

public class DaoContext extends ContextWrapper {
    public final static String TAG = DaoManager.TAG;
    private String dbDirPath;
    public DaoContext(Context context, String path) {
        super(context.getApplicationContext());
        dbDirPath = path;
    }

    /**
     * 获得数据库路径，如果不存在，则创建对象
     *
     * @param dbName
     */
    @Override
    public File getDatabasePath(String dbName) {
        if (TextUtils.isEmpty(dbDirPath)){
            Log.d(TAG,"路径加载失败");
            return null;
        }
        File baseFile = new File(dbDirPath);
        // 目录不存在则自动创建目录
        if (!baseFile.exists()){
            baseFile.mkdirs();
        }

        StringBuffer buffer = new StringBuffer();
        buffer.append(baseFile.getPath());
        buffer.append(File.separator);
        buffer.append(dbName);

        String dbPath = buffer.toString();// 数据库路径
        // 数据库文件是否创建成功
        boolean isFileCreateSuccess = false;
        // 判断文件是否存在，不存在则创建该文件
        File dbFile = new File(dbPath);

        Log.d(TAG,"Database path：" + dbFile.getPath());
        if (!dbFile.exists()) {
            try {
                isFileCreateSuccess = dbFile.createNewFile();// 创建文件
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else
            isFileCreateSuccess = true;
        // 返回数据库文件对象
        if (isFileCreateSuccess)
            return dbFile;
        else
            return super.getDatabasePath(dbName);
    }

    /**
     * 重载这个方法，是用来打开SD卡上的数据库的，android 2.3及以下会调用这个方法。
     *
     * @param name
     * @param mode
     * @param factory
     */
    @Override
    public SQLiteDatabase openOrCreateDatabase(String name, int mode, SQLiteDatabase.CursorFactory factory) {
        SQLiteDatabase result = SQLiteDatabase.openOrCreateDatabase(getDatabasePath(name), factory);
        return result;
    }

    /**
     * Android 4.0会调用此方法获取数据库。
     *
     * @param name
     * @param mode
     * @param factory
     * @param errorHandler
     * @see ContextWrapper#openOrCreateDatabase(String, int,
     * SQLiteDatabase.CursorFactory,
     * DatabaseErrorHandler)
     */
    @Override
    public SQLiteDatabase openOrCreateDatabase(String name, int mode, SQLiteDatabase.CursorFactory factory, DatabaseErrorHandler errorHandler) {
        SQLiteDatabase result = SQLiteDatabase.openOrCreateDatabase(getDatabasePath(name), factory);

        return result;
    }

}