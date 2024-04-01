package com.janev.chongqing_bus_app.tcp.task.resource;

import android.util.Log;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.blankj.utilcode.util.FileIOUtils;
import com.janev.chongqing_bus_app.db.Material;
import com.janev.chongqing_bus_app.db.Program;
import com.janev.chongqing_bus_app.db.Time;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.functions.Function;

public class ResourceDataResolver implements Function<File, ResourceDataResolver.Data> {
    private static final String TAG = "ResourceDataResolver";

    @Override
    public Data apply(File file) throws Exception {
        try {
            String string = FileIOUtils.readFile2String(file);
            Log.d(TAG, "解析刷库");
            List<Program> programList = new ArrayList<>();
            List<Material> materialList = new ArrayList<>();
            List<Time> timeList = new ArrayList<>();

            JSONObject jsonObject = JSONObject.parseObject(string);
            String resFileId = jsonObject.getString("id");
            String resFileVersion = jsonObject.getString("version");

            JSONArray programJsonArray = jsonObject.getJSONArray("programList");
            for (int i = 0; i < programJsonArray.size(); i++) {
                JSONObject programJsonObject = programJsonArray.getJSONObject(i);
                Program program = new Program();
                program.setId(programJsonObject.getString("id"));
                program.setName(programJsonObject.getString("name"));
                program.setState(programJsonObject.getIntValue("state"));
                program.setPriority(programJsonObject.getIntValue("priority"));
                String programStartDate = programJsonObject
                        .getString(programJsonObject.containsKey("startDate") ? "startDate" : "satrtDate");
                String programEndDate = programJsonObject.getString("endDate");
                program.setStartDate(ResourceManager2.string2Date(programStartDate).getTime());
                program.setEndDate(ResourceManager2.string2Date(programEndDate).getTime());
                programList.add(program);
//                long add = DaoManager.get().add(program);
//                Log.d(TAG, "添加Program：" + program.getId() + " , " + add);

                JSONArray materialJsonArray = programJsonObject.getJSONArray("materialList");
                for (int i1 = 0; i1 < materialJsonArray.size(); i1++) {
                    JSONObject materialJsonObject = materialJsonArray.getJSONObject(i1);
                    Material material = new Material();
                    material.setProgramId(program.getId());
                    material.setId(materialJsonObject.getString("id"));
                    material.setMaterialType(materialJsonObject.getIntValue("materialType"));
                    material.setName(materialJsonObject.getString("name"));
                    material.setSize(materialJsonObject.getLong("size"));
                    material.setMd5(materialJsonObject.getString("md5"));
                    material.setUrl(materialJsonObject.getString("url"));
                    material.setOrder(materialJsonObject.getIntValue("order"));
                    material.setTotalTimes(materialJsonObject.getLongValue("totalTimes"));
                    material.setTotalDuration(materialJsonObject.getLongValue("totalDuration"));
                    materialList.add(material);
//                    long add1 = DaoManager.get().add(material);
//                    Log.d(TAG, "添加Material：" + material.getProgramId() + " --- " + material.getId() + " , " + add1);

                    JSONArray timeJsonArray = materialJsonObject.getJSONArray("timeList");
                    for (int i2 = 0; i2 < timeJsonArray.size(); i2++) {
                        JSONObject timeJsonObject = timeJsonArray.getJSONObject(i2);
                        Time time = new Time();
                        time.setMaterialId(material.getId());
                        time.setRepeatTimes(timeJsonObject.getLongValue("repeatTimes"));
                        time.setDuration(timeJsonObject.getLongValue("duration"));
                        time.setTotalTimes(timeJsonObject.getLongValue("totalTimes"));
                        time.setTotalDuration(timeJsonObject.getLongValue("totalDuration"));
                        String startTime = timeJsonObject.getString("startTime");
                        String endTime = timeJsonObject.getString("endTime");
                        time.setStartTime(ResourceManager2.string2Time(startTime).getTime());
                        time.setEndTime(ResourceManager2.string2Time(endTime).getTime());
                        timeList.add(time);
//                        long add2 = DaoManager.get().add(time);
//                        Log.d(TAG, "添加Time：" + " --- " + time.getMaterialId() + " --- " + time.getId() + " , " + add2);
                    }
                }
            }
            return new Data(file,resFileId,resFileVersion,string,programList,materialList,timeList);
        } catch (Exception e){
            Log.e(TAG, "resolve: ", e);
            throw e;
        }
    }

    public static class Data {
        private final File file;
        private final String string;
        private final String resFileId;
        private final String resFileVersion;
        private final List<Program> programs;
        private final List<Material> materials;
        private final List<Time> times;

        public Data(File file, String resFileId,String resFileVersion,String string, List<Program> programs, List<Material> materials, List<Time> times) {
            this.file = file;
            this.string = string;
            this.resFileId = resFileId;
            this.resFileVersion = resFileVersion;
            this.programs = programs;
            this.materials = materials;
            this.times = times;
        }

        public String getString() {
            return string;
        }

        public String getResFileId() {
            return resFileId;
        }

        public String getResFileVersion() {
            return resFileVersion;
        }

        public File getFile() {
            return file;
        }

        public List<Program> getPrograms() {
            return programs;
        }

        public List<Material> getMaterials() {
            return materials;
        }

        public List<Time> getTimes() {
            return times;
        }

        @Override
        public String toString() {
            return "Data{" +
                    "file=" + file +
                    ", string='" + string + '\'' +
                    ", resFileId='" + resFileId + '\'' +
                    ", resFileVersion='" + resFileVersion + '\'' +
                    ", programs=" + programs +
                    ", materials=" + materials +
                    ", times=" + times +
                    '}';
        }
    }
}
