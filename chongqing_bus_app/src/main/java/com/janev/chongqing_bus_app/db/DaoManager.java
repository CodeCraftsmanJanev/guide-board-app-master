package com.janev.chongqing_bus_app.db;

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
        daoSession.getMaterialTotalDao().detachAll();
        daoSession.getSiteDao().detachAll();
        daoSession.getKnowledgeDao().detachAll();

        daoSession.getMaterialDao().detachAll();
        daoSession.getProgramDao().detachAll();
        daoSession.getTimeDao().detachAll();

        daoSession.getStartUpLogoDao().detachAll();
        daoSession.getStationPictureDao().detachAll();
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

    public MaterialTotal queryMaterialTotal(String resourceId,String programId,String materialId){
        if(daoSession == null){
            return null;
        }
        return daoSession
                .getMaterialTotalDao()
                .queryBuilder()
                .where(
                        MaterialTotalDao.Properties.ResourceId.eq(resourceId),
                        MaterialTotalDao.Properties.ProgramId.eq(programId),
                        MaterialTotalDao.Properties.MaterialId.eq(materialId)
                )
                .unique();
    }

    public void clearSiteByLineName(String lineName){
        if(daoSession != null){
            daoSession.getSiteDao().queryBuilder().where(SiteDao.Properties.LineName.eq(lineName))
                    .buildDelete().forCurrentThread().executeDeleteWithoutDetachingEntities();
        }
    }

    public List<Site> querySiteByLineName(String lineName,int direction){
        List<Site> siteList = new ArrayList<>();
        if(daoSession != null){
            List<Site> list = daoSession.getSiteDao()
                    .queryBuilder()
                    .where(
                            SiteDao.Properties.LineName.eq(lineName),
                            SiteDao.Properties.Direction.eq(direction)
                    )
                    .orderAsc(SiteDao.Properties.Index)
                    .list();
            if(list != null && !list.isEmpty()){
                siteList.addAll(list);
            }
        }
        return siteList;
    }

    public List<Knowledge> queryAllKnowledge(){
        List<Knowledge> knowledgeList = new ArrayList<>();
        if(daoSession != null){
            List<Knowledge> list = daoSession.getKnowledgeDao()
                    .queryBuilder()
                    .list();
            if(list != null && !list.isEmpty()){
                knowledgeList.addAll(list);
            }
        }
        return knowledgeList;
    }

    public void addKnowledge(List<Knowledge> list){
        if(daoSession != null){
            if(!list.isEmpty()){
                daoSession.getKnowledgeDao().insertOrReplaceInTx(list);
            }
        }
    }

    public void setKnowledge(List<Knowledge> list){
        if(daoSession != null){
            KnowledgeDao knowledgeDao = daoSession.getKnowledgeDao();
            knowledgeDao.deleteAll();
            if(list != null && !list.isEmpty()){
                knowledgeDao.insertOrReplaceInTx(list);
            }
        }
    }

    public void addOrUpdatePrograms(List<Program> programs){
        if(daoSession != null){
            daoSession.getProgramDao().insertOrReplaceInTx(programs);
        }
    }

    public List<Program> queryProgramByDate(long date){
        List<Program> list = null;
        if(daoSession != null){
            list = daoSession.getProgramDao()
                    .queryBuilder()
                    .where(ProgramDao.Properties.StartDate.le(date),
                            ProgramDao.Properties.EndDate.ge(date),
                            ProgramDao.Properties.State.eq(0))
                    .orderAsc(ProgramDao.Properties.EndDate)
                    .orderDesc(ProgramDao.Properties.Priority)
                    .list();
            Log.d(TAG, "queryProgramByDate: " + list.size());
        }
        if(list == null){
            list = new ArrayList<>();
        }
        return list;
    }

    public List<Program> queryAvailableProgramByDate(long date){
        List<Program> list = new ArrayList<>();
        if(daoSession != null){
            List<Program> list1 = daoSession.getProgramDao()
                    .queryBuilder()
                    .where(ProgramDao.Properties.EndDate.ge(date),
                            ProgramDao.Properties.State.eq(0))
                    .orderAsc(ProgramDao.Properties.EndDate)
                    .orderDesc(ProgramDao.Properties.Priority)
                    .list();
            list.addAll(list1);
        }
        return list;
    }

    public void addOrUpdateMaterials(List<Material> materials){
        if(daoSession != null){
            daoSession.getMaterialDao().insertOrReplaceInTx(materials);
        }
    }

    public List<Material> queryMaterialByProgramId(String programId) {
        List<Material> list = new ArrayList<>();
        if(daoSession != null){
            List<Material> list1 = daoSession.getMaterialDao()
                    .queryBuilder()
                    .where(MaterialDao.Properties.ProgramId.eq(programId))
                    .orderAsc(MaterialDao.Properties.Order)
                    .list();
            list.addAll(list1);
        }
        return list;
    }

    public void addOrUpdateTimes(List<Time> times){
        if(daoSession != null){
            daoSession.getTimeDao().insertOrReplaceInTx(times);
        }
    }

    /**
     * 播放时查询符合时段的Time(结束时间大于当前时间都算)
     * @param materialId
     * @param time 当前时段（date转换为HH:mm:ss）
     * @return
     */
    public List<Time> queryTimeByMaterialIdAndTime(String materialId,long time) {
        List<Time> list = new ArrayList<>();
        if(daoSession != null){
            List<Time> list1 = daoSession.getTimeDao()
                    .queryBuilder()
                    .where(TimeDao.Properties.MaterialId.eq(materialId),
                            TimeDao.Properties.EndTime.ge(time))
                    .orderAsc(TimeDao.Properties.EndTime)
                    .list();
            list.addAll(list1);
        }
        return list;
    }

    public long queryProgramCount(String cacheResourceId) {
        long count = 0;
        if(daoSession != null){
            count = daoSession.getProgramDao().queryBuilder().count();
        }
        return count;
    }

    public void addOrUpdateStationPictures(List<StationPicture> stationPictures) {
        if(daoSession != null){
            daoSession.getStationPictureDao().insertOrReplaceInTx(stationPictures);
        }
    }

    public long queryUpdateStationPictures(){
        long count = 0;
        if(daoSession != null){
            count = daoSession.getStationPictureDao().queryBuilder().count();
        }
        return count;
    }

    public StationPicture queryStationPicture(String lineName, int upDown, int index) {
        if(daoSession != null){
            return daoSession.getStationPictureDao().queryBuilder().where(
                    StationPictureDao.Properties.RouteNum.eq(lineName),
                    StationPictureDao.Properties.Direction.eq(upDown),
                    StationPictureDao.Properties.StationNum.eq(index)
            ).unique();
        }
        return null;
    }

    public Material queryMaterialBy_Id(long currMaterialId) {
        if(daoSession != null){
            return daoSession.getMaterialDao().queryBuilder().where(MaterialDao.Properties._id.eq(currMaterialId)).unique();
        }
        return null;
    }

}
