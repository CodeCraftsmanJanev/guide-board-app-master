package com.yunbiao.publicity_guideboard.db;

import android.content.Context;
import android.util.Log;

import org.greenrobot.greendao.database.Database;

import java.util.ArrayList;
import java.util.List;

public class DaoManager {
    public static final String TAG = "DaoManager";
    public static final String OLD_DB_NAME = "bus.db";
    private DaoMaster daoMaster;
    private DaoSession daoSession;

    public static final long FAILURE = -1;
    public static final long SUCCESS = 0;

    private static final class Holder{
        public static final DaoManager INSTANCE = new DaoManager();
    }

    public static DaoManager get() {
        return Holder.INSTANCE;
    }

    private DaoManager() {
    }

    public void initDB(Context context,String dirPath){
        createDatabase(new DaoContext(context,dirPath),OLD_DB_NAME);
    }

    public void createDatabase(Context context, String name) {
        MySQLiteHelper helper = new MySQLiteHelper(context, name, null);
        Log.d(TAG, "Initialized database: " + helper.getDatabaseName());
        Database db = helper.getWritableDb();
        Log.d(TAG, "Writable database: " + db);
        daoMaster = new DaoMaster(db);
        Log.d(TAG, "Dao master: " + daoMaster);
        daoSession = daoMaster.newSession();
        Log.d(TAG, "Dao session: " + daoSession);
        daoSession.clear();

        daoSession.getAdvertDao().detachAll();
    }

    public DaoSession getDaoSession() {
        return daoSession;
    }

    public DaoMaster getDaoMaster() {
        return daoMaster;
    }

    public <T> long add(T clazz) {
        if (daoSession == null) {
            return FAILURE;
        }
        return daoSession.insert(clazz);
    }

    public <T> long update(T t) {
        if (daoSession == null) {
            return FAILURE;
        }
        daoSession.update(t);
        return SUCCESS;
    }

    public <T> long addOrUpdate(T clazz) {
        if (daoSession == null) {
            return FAILURE;
        }
        return daoSession.insertOrReplace(clazz);
    }

    public <T> List<T> query(Class<T> clazz) {
        if (daoSession == null) {
            return new ArrayList<>();
        }
        List<T> ts = daoSession.loadAll(clazz);
        if(ts == null){
            ts = new ArrayList<>();
        }
        return ts;
    }

    public <T> long delete(T t) {
        if (daoSession == null) {
            return FAILURE;
        }
        daoSession.delete(t);
        return SUCCESS;
    }

    public boolean deleteAll(Class clazz) {
        if (daoSession == null) {
            return false;
        }
        daoSession.deleteAll(clazz);
        return true;
    }

    public void clear(){
        if(daoSession == null){
            return;
        }
        daoSession.clear();
    }

    public Advert queryAdvertByNumber(String number){
        if(daoSession == null){
            return null;
        }
        return daoSession.getAdvertDao().queryBuilder().where(AdvertDao.Properties.Number.eq(number)).unique();
    }
}
