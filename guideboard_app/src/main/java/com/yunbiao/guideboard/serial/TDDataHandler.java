package com.yunbiao.guideboard.serial;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.blankj.utilcode.util.FileIOUtils;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.UiMessageUtils;
import com.yunbiao.guideboard.system.Cache;
import com.yunbiao.guideboard.system.Constants;
import com.yunbiao.guideboard.utils.BytesUtils;
import com.yunbiao.guideboard.utils.StringUtils;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import tp.xmaihh.serialport.bean.ComBean;

public class TDDataHandler extends DataHandler {

    private final StringBuilder hexStringBuilder;
    private static final String START_HEX = "bb10";
    private static final String END_HEX = "55";

    public TDDataHandler() {
        super(new Constraint(START_HEX,END_HEX,-1));
        hexStringBuilder = new StringBuilder();
    }

    @Override
    protected void loadCache() {
        String siteList = Cache.getString(Cache.Key.SITE_LIST);
        if(!TextUtils.isEmpty(siteList)){
            UiMessageUtils.getInstance().send(Constants.SITE_LIST);
        }

        String lineNumber = Cache.getString(Cache.Key.LINE_NUMBER);
        if(!TextUtils.isEmpty(lineNumber)){
            UiMessageUtils.getInstance().send(Constants.LINE_NUMBER);
        }

        UiMessageUtils.getInstance().send(Constants.PRICE);
    }

    @Override
    public void check(ComBean comBean) {
        if(comBean == null){
            d("comBean is null");
            return;
        }
        byte[] bytes = comBean.bRec;
        if (bytes == null || bytes.length <= 5) {
            d("bytes is null");
            return;
        }

        //起始字节
        byte start = bytes[0];
        String startHex = BytesUtils.byteToHex(start);

        //485转TTL有时候会出现结尾5500的情况，需要把结尾的00去掉之后再校验
        byte[] end1 = BytesUtils.SubByte(bytes,bytes.length - 2,2);
        String endHex2 = BytesUtils.bytesToHex(end1);
        if(TextUtils.equals("5500",endHex2)){
            d( "analysisData: 结尾是5500，截取后解析");
            bytes = BytesUtils.SubByte(bytes,0,bytes.length - 1);
        }

        //末尾字节
        byte end = bytes[bytes.length - 1];
        String endHex = BytesUtils.byteToHex(end);

        //开始
        if (START_HEX.equals(startHex)) {
            d( "-----------start---------");
            //清除数据
            hexStringBuilder.setLength(0);
        }

        //数据串
        String hex = BytesUtils.bytesToHex(bytes);
        //拼接数据
        hexStringBuilder.append(hex);

        //结束
        if (END_HEX.equals(endHex)) {
            d( "-----------end---------");
            d("收集结束：" + hexStringBuilder);

            //裁剪数据
            String[] hexArray = hexStringBuilder.toString().split(START_HEX);
            hexStringBuilder.setLength(0);

            //发送数据
            for (String ss : hexArray) {
                if (!TextUtils.isEmpty(ss)) {
                    d( "--->" + ss);
                    byte[] byteArray = BytesUtils.hexToByteArray(START_HEX + ss);
                    handle(byteArray);
                }
            }
        }
    }

    // bb10b011
    // 2f230acad0d5feb9abd4b0cef72408cad0d5b9c0c0b9dd250acad0d5b9c0c0b9ddb6ab260af4e4b4e4bbaacda5b6ab270ac8fdcaaed2bbd6d0cef7280cd4b6b4f3b9fab1f6b8aecef72914bec6b3c7b4f3b5c0cfa3d2c4b4f3b5c0c2b7bfda2a0ed9f1cedfbbb9d4add0a1c7f8c4cf2b14bec6b3c7b4f3b5c0cebacee4b4f3b5c0c2b7bfda2c10bec6b3c7b4f3b5c0bda8b0b2c2b7bfda2d08c6fbb3b5c4cfd5be2e08b8dfccfac4cfd5bec87555bb200601a0c70055

    private static final byte LINE_NAME = 0x02;
    private static final byte BROAD_SITE = 0x03;
    private static final byte SITE_LIST = 0x11;
    private static final DateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

    @Override
    protected void handle(byte[] bytes) {
        byte ADDR = bytes[1];
        byte CMD = bytes[3];
        byte[] data = BytesUtils.SubByte(bytes, 4, bytes.length - 7);

        if(ADDR != (byte)0X10){
            return;
        }

        //判断是否是时间
        if(CMD == (byte)0x11 && data.length == 7){
            try {
                String string = BytesUtils.bytesToHex(data);
                Date parse = sdf.parse(string);
                d("是时间：" + string);
                return;
            } catch (ParseException e) {
                Log.e(TAG, "chioseAgr: ", e);
            }
        }

        Queue<Byte> byteQueue = new LinkedList<>();
        for (byte datum : data) {
            byteQueue.add(datum);
        }

        switch (CMD) {
            case LINE_NAME:
                d( "handle: 线路名称");
//                handleLineName(bytes);
                handleLineName(byteQueue);
                break;
//            case BROAD_SITE:
//                d( "handle: 报站");
//                broadSite(bytes);
//                break;
            case SITE_LIST:
                d( "handle: 站点列表");
//                handleSiteList(bytes);
                handleSiteList(byteQueue);
                break;
        }
    }

    private String startSite,endSite;
    private void handleLineName(Queue<Byte> byteQueue){
        int length = BytesUtils.hex16to10(BytesUtils.byteToHex(byteQueue.poll()));
        String lineNumber = StringUtils.hexStringToString(BytesUtils.bytesToHex(getBytes(byteQueue,length)));

        length = BytesUtils.hex16to10(BytesUtils.byteToHex(byteQueue.poll()));
        startSite = StringUtils.hexStringToString(BytesUtils.bytesToHex(getBytes(byteQueue,length)));

        length = BytesUtils.hex16to10(BytesUtils.byteToHex(byteQueue.poll()));
        endSite = StringUtils.hexStringToString(BytesUtils.bytesToHex(getBytes(byteQueue,length)));

        d("线路号：" + lineNumber + " , " + "起点字符：" + startSite + " , " + "终点字符：" + endSite);

        Cache.setString(Cache.Key.LINE_NUMBER,lineNumber);
        UiMessageUtils.getInstance().send(Constants.LINE_NUMBER);
    }

    private void handleLineName(byte[] bytes){
        StringBuilder builder = new StringBuilder();
        for (int i = 4; i < bytes.length - 3; i++) {
            Integer length = Integer.valueOf(BytesUtils.byteToHex(bytes[i]), 16);
            byte[] nameByteArry = new byte[length];
            for (int j = 0; j < length; j++) {
                i = i + 1;
                nameByteArry[j] = bytes[i];
            }
            String hex = BytesUtils.bytesToHex(nameByteArry);
            String name = StringUtils.hexStringToString(hex);
            builder.append(name + ",");
        }
        String line = builder.toString();
        line = line.substring(0, line.length() - 1);
        String[] lineInfo = line.split(",");
        d( "analysisLineInfo------" + line);

        Cache.setString(Cache.Key.LINE_NUMBER,lineInfo[0]);
        UiMessageUtils.getInstance().send(Constants.LINE_NUMBER);
    }

    private boolean isRefreshList = false;
    private void handleSiteList(Queue<Byte> byteQueue){

        if(siteList == null){
            siteList = new LinkedList<>();
        }

        int siteCount = BytesUtils.hex16to10(BytesUtils.byteToHex(byteQueue.poll()));
//        d("站点总数：" + siteCount);

        int tag = 0;
        int length = 0;
        int siteIndex = 0;
        readList:while (!byteQueue.isEmpty()) {
            switch (tag) {
                case 0:
                    siteIndex = BytesUtils.hex16to10(BytesUtils.byteToHex(byteQueue.poll()));
//                    d("站点序号：" + siteIndex);
                    if(siteIndex == 0){
                        d("发现起始站，清除旧数据");
                        siteList.clear();
                        isRefreshList = true;
                    }
                    tag = 1;
                    break;
                case 1:
                    length = BytesUtils.hex16to10(BytesUtils.byteToHex(byteQueue.poll()));
//                    d("站点名长度：" + length);
                    tag = 2;
                    break;
                case 2:
                    tag = 0;
                    if(byteQueue.size() >= length){
                        String siteName = StringUtils.hexStringToString(BytesUtils.bytesToHex(getBytes(byteQueue,length)));
                        d("站点名：" + siteIndex + "_" + siteName + "，站点总数：" + siteCount + " --- " + (isRefreshList && siteList.size() < siteCount));
                        //增加站名收集控制，如果不在刷新状态则不添加
                        if(isRefreshList && siteList.size() < siteCount){
                            siteList.add(siteName);
                            if(siteList.size() >= siteCount){
                                Log.d(TAG, "handleSiteList: ------------------------------ ");
                                break readList;
                            }
                        }
                    }
                    break;
            }
        }

        //增加控制，如果不在刷新状态则不添加刷新站点列表
        if(isRefreshList && siteList.size() >= siteCount){
            Log.e(TAG, "handleSiteList: ------------------------------ ");
            isRefreshList = false;
            if(siteCount != siteList.size()){
                d("获取站点列表完成，通知界面刷新：" + siteCount + " , " + siteList.size() + " , " + siteList);
                d("站点列表数量错误");
            } else {
                d("获取站点列表完成，通知界面刷新：" + siteCount + " , " + siteList.size() + " , " + siteList);
                d("站点列表数量正确");
            }
            String s = siteList.get(0);
            String s1 = siteList.get(siteList.size() - 1);
            if(TextUtils.equals(s,startSite) && TextUtils.equals(s1,endSite)){
                d("站点列表正确");
            } else {
                d("站点列表异常");
            }

            StringBuilder stringBuilder = new StringBuilder();
            for (int i1 = 0; i1 < siteList.size(); i1++) {
                if(i1 != 0){
                    stringBuilder.append("—");
                }
                stringBuilder.append(siteList.get(i1));
            }

            String endSite = siteList.get(siteList.size() - 1);
            Cache.setString(Cache.Key.END_SITE,endSite);
            Cache.setString(Cache.Key.SITE_LIST,stringBuilder.toString());
            UiMessageUtils.getInstance().send(Constants.SITE_LIST);
        }
    }

    private byte[] getBytes(Queue<Byte> byteQueue,int length){
        byte[] bytes = new byte[length];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = byteQueue.poll();
        }
        return bytes;
    }

    private List<String> siteList;
    private void handleSiteList(byte[] bytes){
        if(siteList == null){
            siteList = new ArrayList<>();
        }
        int allNum = Integer.valueOf(BytesUtils.byteToHex(bytes[4]), 16);//总站点数
        for (int i = 5; i < bytes.length - 3; i++) {
            Integer index = Integer.valueOf(BytesUtils.byteToHex(bytes[i]), 16);//站序号
            i = i + 1;
            int length = Integer.valueOf(BytesUtils.byteToHex(bytes[i]), 16);//站名长度
            byte[] byteName = new byte[length];
            for (int j = 0; j < length; j++) {
                i = i + 1;
                byteName[j] = bytes[i];
            }
            String name = StringUtils.hexStringToString(BytesUtils.bytesToHex(byteName));
            d( "text: allNum->"+allNum+" ,index:"+index+" ,name:"+name);
            siteList.add(name);

            if(allNum == (index + 1)){
                d( "公交站点获取完成");
                d( "handleSiteList: " + siteList);
                StringBuilder stringBuilder = new StringBuilder();
                for (int i1 = 0; i1 < siteList.size(); i1++) {
                    if(i1 != 0){
                        stringBuilder.append("—");
                    }
                    stringBuilder.append(siteList.get(i1));
                }
                String endSite = siteList.get(siteList.size() - 1);
                siteList.clear();

                Cache.setString(Cache.Key.END_SITE,endSite);
                Cache.setString(Cache.Key.SITE_LIST,stringBuilder.toString());
                UiMessageUtils.getInstance().send(Constants.SITE_LIST);
            }
        }
    }
    
    private void d(String log){
//        LogUtils.d(TAG,log);
        Log.d(TAG, log);
    }

    private static final String TAG = "TDDataHandler";
    private int i = 1;
    public void test(){
        new Thread(() -> {

            File file = new File(Environment.getExternalStorageDirectory(),"789.txt");
            Log.e(TAG, "test: " + file.getPath() );
            List<String> strings = FileIOUtils.readFile2List(file);
            for (String string : strings) {
                if(!string.contains("<---")){
                    continue;
                }

                int i = string.lastIndexOf("<---");
                String substring = string.substring(i + 4);

                byte[] bytes = BytesUtils.hexToByteArray(substring);

                ComBean comBean = new ComBean("",bytes,bytes.length);
                check(comBean);

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

/*
            File file = new File(Environment.getExternalStorageDirectory(),"TDTD" + i + ".txt");


            Log.d(TAG, "test: 加载文件：" + file.getPath());
            i += 1;
            if(i > 4){
                i = 1;
            }


            if(FileUtils.isFileExists(file)){
                List<byte[]> bytesList = new ArrayList<>();
                List<String> strings = FileIOUtils.readFile2List(file);
                for (String string : strings) {
                    if(!string.contains("<---")){
                        continue;
                    }

                    int i = string.lastIndexOf("<---");
                    String substring = string.substring(i + 4);

                    byte[] bytes = BytesUtils.hexToByteArray(substring);
                    bytesList.add(bytes);
                }

                for (byte[] bytes : bytesList) {
                    String s = BytesUtils.bytesToHex(bytes);
                    d("<---" + s);

                    ComBean comBean = new ComBean("",bytes,bytes.length);
                    check(comBean);
                }

            }*/
        }).start();
    }

}
