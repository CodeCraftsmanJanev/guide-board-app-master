package com.janev.chongqing_bus_app.serial.request_v2;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SiteInfo {
    private static final String TAG = "SiteInfo";

    private String lineName;
    private byte upDown;
    private LinkedHashMap<Integer,String> siteMap;

    public SiteInfo() {
        siteMap = new LinkedHashMap<>();
    }

    public byte getUpDown() {
        return upDown;
    }

    public void setUpDown(byte upDown) {
        this.upDown = upDown;
        Log.e(TAG, "setUpDown: " + upDown);
    }

    public String getLineName() {
        return lineName;
    }

    public void setLineName(String lineName) {
        this.lineName = lineName;
        Log.d(TAG, "setLineName: " + lineName);
    }

    public boolean isComplete(byte ud,String ln,int siteCount){
        return upDown == ud && TextUtils.equals(lineName,ln) && siteMap.size() >= siteCount;
    }

    public List<String> getList() {
        LinkedList<String> list = new LinkedList<>();
        if(!siteMap.isEmpty()){
            list.addAll(siteMap.values());
        }
        return list;
    }

    public int size(){
        return siteMap.size();
    }

    public boolean isEmpty(){
        return siteMap.isEmpty();
    }

    private Map.Entry<Integer,String> firstEntry;
    public Map.Entry<Integer,String> getFirst(){
        if (!siteMap.isEmpty()) {
            firstEntry = new LinkedList<>(siteMap.entrySet()).getFirst();
            return firstEntry;
        }
        return null;
    }

    public LinkedHashMap<Integer, String> getSiteMap() {
        return siteMap;
    }

    public void addAll(LinkedHashMap<Integer, String> siteMap) {
        if(!this.siteMap.isEmpty()){
            this.siteMap.clear();
        }
        this.siteMap.putAll(siteMap);
        Log.d(TAG, "addAll: " + this.siteMap.size());
    }
}
