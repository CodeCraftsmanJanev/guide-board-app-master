package com.janev.chongqing_bus_app.tcp.task.appResource;

import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.blankj.utilcode.util.FileIOUtils;
import com.janev.chongqing_bus_app.db.StartUpLogo;
import com.janev.chongqing_bus_app.db.StationPicture;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.functions.Function;

public class AppResourceDataResolver implements Function<File, AppResourceDataResolver.Data> {
     private static final String TAG = "AppResourceDataResolver";

     @Override
     public Data apply(File file) throws Exception {
          try {
               StartUpLogo startUpLogo = null;
               List<StationPicture> stationPictureList = new ArrayList<>();

               String string = FileIOUtils.readFile2String(file);
               Log.d(TAG, "解析刷库");
               JSONObject jsonObject = JSONObject.parseObject(string);

               JSONObject sulJsonObject = jsonObject.getJSONObject("startupLogo");
               String url = sulJsonObject.getString("url");
               if(!TextUtils.isEmpty(url)){
                    startUpLogo = new StartUpLogo();
                    startUpLogo.setId(sulJsonObject.getString("id"));
                    startUpLogo.setType(sulJsonObject.getIntValue("type"));
                    startUpLogo.setName(sulJsonObject.getString("name"));
                    startUpLogo.setSize(sulJsonObject.getLongValue("size"));
                    startUpLogo.setMd5(sulJsonObject.getString("md5"));
                    startUpLogo.setUrl(url);
               }

               JSONArray spJsonArray = jsonObject.getJSONArray("stationPicture");
               for (int i = 0; i < spJsonArray.size(); i++) {
                    JSONObject soJsonObject = spJsonArray.getJSONObject(i);
                    StationPicture stationPicture = new StationPicture();
                    stationPicture.setRouteNum(soJsonObject.getString("routeNum"));
                    stationPicture.setDirection(soJsonObject.getIntValue("direction"));
                    stationPicture.setStationNum(soJsonObject.getIntValue("stationNum"));
                    stationPicture.setDuration(soJsonObject.getLongValue("duration"));
                    stationPicture.setId(soJsonObject.getString("id"));
                    stationPicture.setType(soJsonObject.getIntValue("type"));
                    stationPicture.setName(soJsonObject.getString("name"));
                    stationPicture.setSize(soJsonObject.getLong("size"));
                    stationPicture.setMd5(soJsonObject.getString("md"));
                    stationPicture.setUrl(soJsonObject.getString("url"));
                    stationPictureList.add(stationPicture);
               }

               return new Data(file,startUpLogo,stationPictureList);
          } catch (Exception e){
               Log.e(TAG, "apply: ",e );
               throw e;
          }
     }

     public static class Data {
          private File file;
          private StartUpLogo startUpLogo;
          private List<StationPicture> stationPictures;

          public Data(File file, StartUpLogo startUpLogo, List<StationPicture> stationPictures) {
               this.file = file;
               this.startUpLogo = startUpLogo;
               this.stationPictures = stationPictures;
          }

          public File getFile() {
               return file;
          }

          public StartUpLogo getStartUpLogo() {
               return startUpLogo;
          }

          public List<StationPicture> getStationPictures() {
               return stationPictures;
          }
     }
}
