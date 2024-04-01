package com.janev.chongqing_bus_app.utils;

import android.util.Log;

import com.blankj.utilcode.util.FileUtils;
import com.google.gson.internal.LinkedTreeMap;
import com.janev.chongqing_bus_app.db.DaoManager;
import com.janev.chongqing_bus_app.db.Material;
import com.janev.chongqing_bus_app.db.Program;
import com.janev.chongqing_bus_app.db.Time;
import com.janev.chongqing_bus_app.system.Path;
import com.janev.chongqing_bus_app.tcp.task.resource.ResourceManager2;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

public class ProgramLoader {

    public static LinkedHashMap<Material, List<Time>> loadCacheProgram(){
        LinkedHashMap<Material,List<Time>> materialMap = new LinkedHashMap<>();

        List<Program> programList = DaoManager.get().query(Program.class);
        for (Program program : programList) {
            List<Material> materialList = DaoManager.get().queryMaterialByProgramId(program.getId());
            for (Material material : materialList) {
                String materialPath = Path.getMaterialPath(material);
                //如果文件存在
                if (FileUtils.isFileExists(materialPath)) {
                    materialMap.put(material,new ArrayList<>());
                }
            }
        }
        return materialMap;
    }

    private static final String TAG = "ProgramLoader";
    public static LinkedHashMap<Material, List<Time>> loadPlayProgram(){
        Date currDate = ResourceManager2.getCurrDate();
        Date currTime = ResourceManager2.getCurrTime();
        Log.d(TAG, "loadPlayProgram: 当前日期参数：" + currDate.getTime() + " --- " + ResourceManager2.date2String(currDate));
        Log.d(TAG, "loadPlayProgram: 当前时间参数：" + currTime.getTime() + " --- " + ResourceManager2.time2String(currTime));

        List<Program> query = DaoManager.get().query(Program.class);
        for (Program program : query) {
            Log.d(TAG, "节目单: " + ResourceManager2.date2String(program.getStartDate()) + " --- " + ResourceManager2.date2String(program.getEndDate()));
        }

        LinkedHashMap<Material,List<Time>> materialMap = new LinkedHashMap<>();

        List<Program> programs = DaoManager.get().queryProgramByDate(currDate.getTime());
        Log.d(TAG, "查询到节目单: " + programs.size());
        for (Program program : programs) {
            Log.d(TAG, "loadPlayProgram: ------------------------------------------------------ ");
            Log.d(TAG, "loadPlayProgram: 检查节目单：" + program.getId() + " , " + program.getStartDate() + " --- " + program.getEndDate());

            List<Material> materialList = DaoManager.get().queryMaterialByProgramId(program.getId());
            Log.d(TAG, "loadPlayProgram: 查询到素材：" + materialList.size());
            Iterator<Material> materialIterator = materialList.iterator();
            while (materialIterator.hasNext()) {
                Material material = materialIterator.next();
                Log.d(TAG, "loadPlayProgram: 检查素材：" + material.getId());
                //如果日期不同则清空播放次数和时长
                if (material.getPlayLastDate() != currDate.getTime()) {
                    Log.d(TAG, "loadPlayProgram: 刷新素材最后播放日期");
                    material.setPlayLastDate(currDate.getTime());
                    material.setPlayTotalTimes(0);
                    material.setPlayTotalDuration(0);
                }

                //判断播放次数是否达标，是则删除
                if(material.getTotalTimes() != 0 && material.getPlayTotalTimes() >= material.getTotalTimes()){
                    materialIterator.remove();
                    Log.d(TAG, "loadPlayProgram: 播放次数已达素材最大");
                }
                //判断播放时长是否达标，是则删除
                else if(material.getTotalDuration() != 0 && material.getTotalDuration() >= material.getTotalDuration()){
                    materialIterator.remove();
                    Log.d(TAG, "loadPlayProgram: 播放时长已达素材最大");
                }
                //检查时间段
                else {
                    List<Time> times = DaoManager.get().queryTimeByMaterialIdAndTime(material.getId(),currTime.getTime());
                    Log.d(TAG, "loadPlayProgram: 查询时间段：" + times.size());
                    Iterator<Time> timeIterator = times.iterator();
                    while (timeIterator.hasNext()) {
                        Time time = timeIterator.next();
                        Log.d(TAG, "loadPlayProgram: 检查时间段：" + time.getStartTime() + " --- " + time.getEndTime());
                        //如果最后播放的日期不同则清空存储
                        if(time.getPlayLastDate() != currDate.getTime()){
                            Log.d(TAG, "loadPlayProgram: 刷新时段最后播放日期");
                            time.setPlayLastDate(currDate.getTime());
                            time.setPlayTotalTimes(0);
                            time.setPlayTotalDuration(0);
                        }
                        //判断播放次数是否达标，是则删除
                        else if(time.getTotalTimes() != 0 && time.getPlayTotalTimes() >= time.getTotalTimes()){
                            timeIterator.remove();
                            Log.d(TAG, "loadPlayProgram: 播放次数已达时段最大");
                        }
                        //判断播放时长是否达标，是则删除
                        else if(time.getTotalDuration() != 0 && time.getPlayTotalDuration() >= time.getTotalDuration()){
                            timeIterator.remove();
                            Log.d(TAG, "loadPlayProgram: 播放时长已达时段最大");
                        }
                    }
                    //没有可播放的时间段
                    if(times.isEmpty()){
                        materialIterator.remove();
                    } else {
                        materialMap.put(material,times);
                    }
                }
            }
            Log.d(TAG, "loadPlayProgram: ------------------------------------------------------ ");
        }
        Log.d(TAG, "筛选后素材列表: " + materialMap.size());
        return materialMap;
    }

    public static List<Material> loadDownloadList(){
        Date currDate = ResourceManager2.getCurrDate();
        Date currTime = ResourceManager2.getCurrTime();

        List<Material> downloadList = new ArrayList<>();

        //根据日期查出可用的Program（结束日期在当前日期之后）
        List<Program> programs = DaoManager.get().queryAvailableProgramByDate(currDate.getTime());
        Log.d(TAG, "节目单数量: " + programs.size());
        for (Program program : programs) {
            Log.d(TAG, "节目单: " + ResourceManager2.date2String(program.getStartDate()) + " --- " + ResourceManager2.date2String(program.getEndDate()));
            //查出可用的Material
            List<Material> materialList = DaoManager.get().queryMaterialByProgramId(program.getId());
            Log.d(TAG, "素材数量: " + materialList.size());
            Iterator<Material> materialIterator = materialList.iterator();
            while (materialIterator.hasNext()) {
                Material material = materialIterator.next();

                //筛选出离当前时间最近的Time
                List<Time> times = DaoManager.get().queryTimeByMaterialIdAndTime(material.getId(),currTime.getTime());
                Log.d(TAG, "时间段数量: " + times.size());
                if(times.isEmpty()){
                    materialIterator.remove();
                    continue;
                }
                for (Time time : times) {
                    Log.d(TAG, "时间段: " + ResourceManager2.time2String(time.getStartTime()) + " --- " + ResourceManager2.time2String(time.getEndTime()));
                }

                Log.d(TAG, "添加");
                downloadList.add(material);
            }
        }

        Log.d(TAG, "loadDownloadList: " + downloadList.size());

        return downloadList;
    }

}
