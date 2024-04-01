package com.yunbiao.publicity_guideboard.serial;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.blankj.utilcode.util.FileIOUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.UiMessageUtils;
import com.blankj.utilcode.util.Utils;
import com.yunbiao.publicity_guideboard.system.Cache;
import com.yunbiao.publicity_guideboard.system.Constants;
import com.yunbiao.publicity_guideboard.ui.MainActivity;
import com.yunbiao.publicity_guideboard.utils.BytesUtils;
import com.yunbiao.publicity_guideboard.utils.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import tp.xmaihh.serialport.SerialHelper;
import tp.xmaihh.serialport.bean.ComBean;

public class HYDataHandler extends DataHandler{

    private final List<String> upList = new ArrayList<>();
    private final List<String> downList = new ArrayList<>();
    private String mUpDownFlag;
    private final HYData hyData;

    public HYDataHandler() {
        super(new Constraint("24","0d",0));
        hyData = new HYData(data -> {
            if (data.length <= 0) {
                return;
            }
            handle(null, data);
        });
    }

    @Override
    protected void loadCache() {
        ThreadUtils.executeBySingle(new ThreadUtils.SimpleTask<String>() {
            @Override
            public String doInBackground() throws Throwable {
                getSiteListUp();
                getSiteListDown();
                return getLineName();
            }

            @Override
            public void onSuccess(String lineName) {
                ArrayList<String> siteList = new ArrayList<>(upList);
                if(!siteList.isEmpty()){
                    UiMessageUtils.getInstance().send(Constants.TYPE_SITE_INFO,new String[]{lineName,siteList.get(0),siteList.get(siteList.size() - 1)});
                    UiMessageUtils.getInstance().send(Constants.TYPE_IN_OUT,new String[]{"22",siteList.get(0),siteList.get(1)});
                }
            }
        });
    }

    @Override
    public void check(SerialHelper serialHelper, ComBean comBean) {
        try {
            if(comBean == null || comBean.bRec == null || comBean.bRec.length <= 0){
                return;
            }
            byte[] datas = comBean.bRec;

            byte startByte = datas[0];
            if(startByte == 0x24){
                hyData.setSHex("24");
                hyData.setEHex("0d");
            } else if(startByte == 0x3a){
                hyData.setSHex("3a");
                hyData.setEHex("11");
            } else if(startByte == 0x1E){
                hyData.setSHex("1e");
                hyData.setEHex("1f");
            }
            hyData.handleData(datas);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void handle(SerialHelper serialHelper, byte[] datas) {
        String hex = BytesUtils.bytesToHex(datas);
        d("---->"+hex);
        byte startByte = datas[0];
        if (startByte==0x24){
            byte order = datas[8];
            if (order == (byte)0x5a) {
                //站点列表
                getSiteList(datas);
            } else if(order == (byte)0x54){
                correctTime(datas);
            }
        } else if (startByte==0x3a){
            //到、离站信息
            inOutSite(datas);
        } else if(startByte == 0x1E){
            //线路信息
            getSiteInfo(datas);
        }
    }

    // 24
    // 26
    // 30303030
    // 3030
    // 54
    // 30
    // 3230313330373239313633313230
    // 31
    // 53535353
    private void correctTime(byte[] datas){
        byte[] bytes = BytesUtils.SubByte(datas, 10, 14);
        String time = StringUtils.hexStringToString(BytesUtils.bytesToHex(bytes));
        d( "correctTime: " + time);

        int year = Integer.parseInt(time.substring(0, 4));//年
        int month = Integer.parseInt(time.substring(4,6));//月
        int day = Integer.parseInt(time.substring(6,8));//日
        int hours = Integer.parseInt(time.substring(8,10));//日
        int minutes = Integer.parseInt(time.substring(10,12));//日
        int seconds = Integer.parseInt(time.substring(12));//日
        int[] ints = {year, month, day, hours, minutes,seconds};
        Log.d(TAG, "correctTime: " + Arrays.toString(ints));
        UiMessageUtils.getInstance().send(MainActivity.CORRECT_TIME,ints);
        // 3230313330373239313633313230
    }

    //获取站点列表
    private void getSiteList(byte[] datas){
        d( "车站列表");
        //上、下行
        String upDownFlag = StringUtils.hexStringToString(BytesUtils.byteToHex(datas[9]));

        //当前包数
        int current = Integer.parseInt(StringUtils.hexStringToString(BytesUtils.bytesToHex(new byte[]{datas[10],datas[11]})));

        //包总数
        int total = Integer.parseInt(StringUtils.hexStringToString(BytesUtils.bytesToHex(new byte[]{datas[12], datas[13]})));

        d("---包序号：" + current + " ---包总数：" + total);

        //站点数据
        String site = StringUtils.hexStringToString(BytesUtils.bytesToHex(BytesUtils.SubByte(datas, 14, datas.length-19)));
        String[] siteArray = site.split("#");
        for (String s : siteArray) {
            d( "站点：" + s + " --- " + upDownFlag);
        }

        //0：上行
        if(TextUtils.equals("0",upDownFlag)){
            //如果是第一包，则清除后再添加
            if(current == 1){
                upList.clear();
            }
            upList.addAll(Arrays.asList(siteArray));
            if(current == total){
                saveListUp(upList);
                String lineName = getLineName();
                UiMessageUtils.getInstance().send(Constants.TYPE_SITE_INFO,new String[]{lineName,upList.get(0),upList.get(upList.size() - 1)});
                UiMessageUtils.getInstance().send(Constants.TYPE_IN_OUT,new String[]{"22",upList.get(0),upList.get(1)});
            }
        }
        //1：下行
        else if(TextUtils.equals("1",upDownFlag)){
            if (current == 1){
                downList.clear();
            }
            downList.addAll(Arrays.asList(siteArray));
            if(current == total){
                saveListDown(downList);
            }
        }
    }

    //获取站点信息
    private void getSiteInfo(byte[] datas){
        d( "线路信息");
        // 1E 60 包头 0,1
        // FF 目标地址 2
        // 02 源地址 3
        // 06 消息帧 4
        // 08 消息帧流水号 5
        // 00 22 消息帧长度 6,7
            // 1B 数据帧1标识 8 0
            // 00 1F 数据帧1长度 9,10 1,2
            // 00 线路序号 11 3
            // 00 显示方式 12 4
            // 00 数据压缩 13 5
            // 00 空调、K字控制 14 6
                // 01 数据类型01线路名 15 7
                // 03 线路号长度
                // 31 C2 B7
                // 02 数据类型左站名
                // 08 左站名长度
                // B9 AB BD BB D7 DC D5 BE
                // 03 数据类型右站名
                // 0A 右站名长度
                // C2 CC D0 C4 C5 C9 B3 F6 CB F9
        // FB
        // 1F

        //取出数据长度
        byte[] LENGTH_BYTES = BytesUtils.SubByte(datas, 6, 2);
        int length = BytesUtils.hex16to10(BytesUtils.bytesToHex(LENGTH_BYTES));
        //取出数据帧
        byte[] DATA_FRAME_BYTES = BytesUtils.SubByte(datas, 8, length);
        d("数据帧：" + BytesUtils.bytesToHex(DATA_FRAME_BYTES));
        //线路信息数据
        byte[] LINE_INFO_BYTES = BytesUtils.SubByte(DATA_FRAME_BYTES, 7, DATA_FRAME_BYTES.length - 7);
        d("线路信息数据：" + BytesUtils.bytesToHex(LINE_INFO_BYTES));

        Queue<Byte> byteQueue = new LinkedList<>();
        for (byte line_info_byte : LINE_INFO_BYTES) {
            byteQueue.add(line_info_byte);
        }

        String[] lineInfoArray = new String[3];
        String frameTag = "";
        int tag = 1,dataLength = 1;
        while (!byteQueue.isEmpty()) {
            byte[] newBytes;
            switch (tag) {
                case 1:
                    tag = 2;
                    dataLength = 1;
                    newBytes = new byte[dataLength];
                    for (int i = 0; i < newBytes.length; i++) {
                        newBytes[i] = byteQueue.poll();
                    }
                    frameTag = BytesUtils.bytesToHex(newBytes);
                    break;
                case 2:
                    tag = 3;
                    dataLength = 1;
                    newBytes = new byte[dataLength];
                    for (int i = 0; i < newBytes.length; i++) {
                        newBytes[i] = byteQueue.poll();
                    }
                    dataLength = BytesUtils.hex16to10(BytesUtils.bytesToHex(newBytes));
                    break;
                case 3:
                    tag = 1;
                    newBytes = new byte[dataLength];
                    for (int i = 0; i < newBytes.length; i++) {
                        newBytes[i] = byteQueue.poll();
                    }
                    String s = StringUtils.hexStringToString(BytesUtils.bytesToHex(newBytes));
                    switch (frameTag) {
                        case "01":
                            lineInfoArray[0] = s;
                            saveLineName(s);
                            break;
                        case "02":
                            lineInfoArray[1] = s;
                            break;
                        case "03":
                            lineInfoArray[2] = s;
                            break;
                    }
                    break;
            }
        }

        d( Arrays.toString(lineInfoArray));

        UiMessageUtils.getInstance().send(Constants.TYPE_SITE_INFO,lineInfoArray);
    }

    //进站离站
    private void inOutSite(byte[] datas){
        d( "到、离站信息");

        //上、下行
        byte UP_DOWN_FLAG = datas[6];
        String upDownFlag = BytesUtils.byteToHex(UP_DOWN_FLAG);

        if(TextUtils.isEmpty(mUpDownFlag) || !TextUtils.equals(upDownFlag,mUpDownFlag)){
            d("标识为空或与数据中不同，切换上下行");
            mUpDownFlag = upDownFlag;
            String lineName = getLineName();
            String start = "",end = "";
            //上行
            if (TextUtils.equals("00",mUpDownFlag)) {
                start = upList.isEmpty() ? "" : upList.get(0);
                end = upList.isEmpty() ? "" : upList.get(upList.size() - 1);
            }
            //下行
            else if(TextUtils.equals("ff",mUpDownFlag)){
                if(downList.isEmpty()){
                    //反转上下数据
                    List<String> upSiteList = new ArrayList<>(upList);
                    Collections.reverse(upSiteList);
                    downList.addAll(upSiteList);
                    d("下行数据为空，反转上行数据：" + upList);
                }
                start = downList.isEmpty() ? "" : downList.get(0);
                end = downList.isEmpty() ? "" : downList.get(downList.size() - 1);
            }
            UiMessageUtils.getInstance().send(Constants.TYPE_SITE_INFO,new String[]{lineName,start,end});
        }

        //进出站标识
        byte IN_OUT = datas[3];
        String inOut = BytesUtils.byteToHex(IN_OUT);

        //当前站点
        byte STATION_INDEX = datas[4];
        int inStationIndex = Integer.valueOf(BytesUtils.byteToHex(STATION_INDEX), 16);

        //下行
        if(TextUtils.equals("ff",mUpDownFlag)){
            int currIndex = inStationIndex - 1;
            int nextIndex = inStationIndex > downList.size() - 1 ? 0 : currIndex + 1;

            String inSiteName = downList.get(currIndex);
            String nextSiteName = inStationIndex > downList.size() - 1 ? upList.get(0) : downList.get(nextIndex);

            d("下行 --- [当前站：" + currIndex + "," + inSiteName + "] --- [下一站：" + nextIndex + "," + nextSiteName + "]");
            UiMessageUtils.getInstance().send(Constants.TYPE_IN_OUT,new String[]{inOut,inSiteName,nextSiteName});
        }
        //上行
        else {
            int currIndex = upList.size() - inStationIndex;
            int nextIndex = inStationIndex <= 1 ? 0 : currIndex + 1;

            String inSiteName = upList.get(currIndex);
            String nextSiteName = inStationIndex <= 1 ? downList.get(0) : upList.get(nextIndex);

            d("上行 --- [当前站：" + currIndex + "," + inSiteName + "] --- [下一站：" + nextIndex + "," + nextSiteName + "]");
            UiMessageUtils.getInstance().send(Constants.TYPE_IN_OUT,new String[]{inOut,inSiteName,nextSiteName});
        }
    }


    private void saveLineName(String lineName){
        Cache.setString(Cache.Key.LINE_NAME,lineName);
    }

    private String getLineName(){
        return Cache.getString(Cache.Key.LINE_NAME," --- ");
    }

    private void saveListUp(Collection<String> collection){
        StringBuilder stringBuilder = new StringBuilder();
        for (String s : collection) {
            stringBuilder.append(s).append(",");
        }
        boolean b = Cache.setString(Cache.Key.SITE_LIST_UP, stringBuilder.toString());
        d( "导出上行站点：" + b);
    }

    private void getSiteListUp(){
        upList.clear();
        String string = Cache.getString(Cache.Key.SITE_LIST_UP);
        d("读取上行数据缓存：" + string);
        if(!TextUtils.isEmpty(string)){
            String[] split = string.split(",");
            for (int i = 0; i < split.length; i++) {
                String s = split[i];
                if(!TextUtils.isEmpty(s)){
                    upList.add(s);
                }
            }
        }
    }

    private void saveListDown(Collection<String> collection){
        StringBuilder stringBuilder = new StringBuilder();
        for (String s : collection) {
            stringBuilder.append(s).append(",");
        }
        boolean b = Cache.setString(Cache.Key.SITE_LIST_DOWN, stringBuilder.toString());
        d( "导出下行站点：" + b);
    }

    private void getSiteListDown(){
        downList.clear();
        String string = Cache.getString(Cache.Key.SITE_LIST_DOWN);
        d( "读取下行数据缓存：" + string);
        if(!TextUtils.isEmpty(string)){
            String[] split = string.split(",");
            for (int i = 0; i < split.length; i++) {
                String s = split[i];
                if(!TextUtils.isEmpty(s)){
                    downList.add(s);
                }
            }
        }

        if(downList.isEmpty()){
            List<String> upSiteList = new ArrayList<>(upList);
            Collections.reverse(upSiteList);
            downList.addAll(upSiteList);
            d("下行数据为空，反转上行数据：" + downList);
        }
    }

    private void d(String log){
        LogUtils.d(TAG,log);
//        Log.d(TAG, log);
    }

    static class HYData{
        private static final String TAG = "HYData";
        private final Utils.Consumer<byte[]> consumer;
        private StringBuilder hexString = null;
        private String device = "";
        private String sHex, eHex;

        public HYData(Utils.Consumer<byte[]> consumer) {
            this.consumer = consumer;
        }

        public void setSHex(String sHex) {
            this.sHex = sHex;
        }

        public void setEHex(String eHex) {
            this.eHex = eHex;
        }

        public void handleData(byte[] bytes){
            if (bytes == null || bytes.length <= 0) {
                return;
            }
            if (hexString == null) {
                hexString = new StringBuilder();
            }

            byte start = bytes[0];
            String startHex = BytesUtils.byteToHex(start);

            byte end = bytes[bytes.length - 1];
            String endHex = BytesUtils.byteToHex(end);

            //开始
            if (sHex.equals(startHex)) {
//            Log.d(TAG, "-----------start---------");
                hexString.delete(0, hexString.length());
                device = BytesUtils.byteToHex(bytes[1]);
            }
            //存储数据
            hexString.append(BytesUtils.bytesToHex(bytes));
            //结束
            if (eHex.equals(endHex)) {
//                Log.d(TAG,"-----------end---------");
//                Log.d(TAG, "--->" + hexString.toString());
                String splitStr = sHex + device;
                String[] hexArray = hexString.toString().split(splitStr);
                for (String ss : hexArray) {
                    if (ss != null && !"".equals(ss)) {
//                    System.out.println("bb10" + ss);
                        byte[] byteArray = BytesUtils.hexToByteArray(splitStr + ss);
                        if (consumer!=null){
                            consumer.accept(byteArray);
                        }
                    }
                }
            }
        }
    }

    public void testList(){
        new Thread(() -> {
            StringBuilder stringBuilder = new StringBuilder();

            //第一包
            File file1 = new File(Environment.getExternalStorageDirectory(), "HY1.txt");
            List<String> strings1 = FileIOUtils.readFile2List(file1);
            for (String s : strings1) {
                String trim = s.replaceAll("0x", "").replaceAll(" ", "").trim();
                stringBuilder.append(trim);
            }
            d("第一包：" + stringBuilder.toString());
            byte[] bytes = BytesUtils.hexToByteArray(stringBuilder.toString());
            ComBean comBean = new ComBean("",bytes,bytes.length);
            check(null,comBean);
            stringBuilder.setLength(0);

            //第二包
            File file2 = new File(Environment.getExternalStorageDirectory(), "HY2.txt");
            List<String> strings2 = FileIOUtils.readFile2List(file2);
            for (String s : strings2) {
                String trim = s.replaceAll("0x", "").replaceAll(" ", "").trim();
                stringBuilder.append(trim);
            }
            d("第二包：" + stringBuilder.toString());
            bytes = BytesUtils.hexToByteArray(stringBuilder.toString());
            comBean = new ComBean("",bytes,bytes.length);
            check(null,comBean);
            stringBuilder.setLength(0);

            //第三包
            File file3 = new File(Environment.getExternalStorageDirectory(), "HY3.txt");
            List<String> strings3 = FileIOUtils.readFile2List(file3);
            for (String s : strings3) {
                String trim = s.replaceAll("0x", "").replaceAll(" ", "").trim();
                stringBuilder.append(trim);
            }
            d("第三包：" + stringBuilder.toString());
            bytes = BytesUtils.hexToByteArray(stringBuilder.toString());
            comBean = new ComBean("",bytes,bytes.length);
            check(null,comBean);
            stringBuilder.setLength(0);

            //第四包
            File file4 = new File(Environment.getExternalStorageDirectory(), "HY4.txt");
            List<String> strings4 = FileIOUtils.readFile2List(file4);
            for (String s : strings4) {
                String trim = s.replaceAll("0x", "").replaceAll(" ", "").trim();
                stringBuilder.append(trim);
            }
            d("第四包：" + stringBuilder.toString());
            bytes = BytesUtils.hexToByteArray(stringBuilder.toString());
            comBean = new ComBean("",bytes,bytes.length);
            check(null,comBean);
            stringBuilder.setLength(0);

            //第五包
            File file5 = new File(Environment.getExternalStorageDirectory(), "HY5.txt");
            List<String> strings5 = FileIOUtils.readFile2List(file5);
            for (String s : strings5) {
                String trim = s.replaceAll("0x", "").replaceAll(" ", "").trim();
                stringBuilder.append(trim);
            }
            d("第五包：" + stringBuilder.toString());
            bytes = BytesUtils.hexToByteArray(stringBuilder.toString());
            comBean = new ComBean("",bytes,bytes.length);
            check(null,comBean);
            stringBuilder.setLength(0);
        }).start();
    }

    //$  &  0 0 2 8   0 0 F    0 0 0 2 0 8 0 8 0 8 0 8 0 8 0 8 1 2 3 4 5 6 7 8 9   S S S S
    //24 26 30303238  303046   30303032303830383038303830383038313233343536373839  53535353
    //24 26 30303042  303052   3534333231323030464345410d

    public void testInfo(){
        String s = "24263030303030305430323031333037323931363331323031535353530D";
        byte[] bytes = BytesUtils.hexToByteArray(s);
        ComBean comBean = new ComBean("",bytes,bytes.length);
        check(null,comBean);

        // 1E 60 包头
        // FF 目标地址
        // 02 源地址
        // 06 消息帧
        // 08 消息帧流水号
        // 00 22 消息帧长度
            // 1B 数据帧1标识
            // 00 1F 数据帧1长度
            // 00 线路序号
            // 00 显示方式
            // 00 数据压缩
            // 00 空调、K字控制
            // 01 数据类型01线路名
            // 03 线路号长度
            // 31 C2 B7
            // 02 数据类型左站名
            // 08 左站名长度
            // B9 AB BD BB D7 DC D5 BE
            // 03 数据类型右站名
            // 0A 右站名长度
            // C2 CC D0 C4 C5 C9 B3 F6 CB F9
        // FB
        // 1F
//        String s = "1E 60 FF 02 06 08 00 22 1B 00 1F 00 00 00 00 01 03 31 C2 B7 02 08 B9 AB BD BB D7 DC D5 BE 03 0A C2 CC D0 C4 C5 C9 B3 F6 CB F9 FB 1F".replaceAll(" ","");
//        Log.e(TAG, "testInfo: 数据：" + s);
//        byte[] bytes = BytesUtils.hexToByteArray(s);
//        ComBean comBean = new ComBean("",bytes,bytes.length);
//        check(null,comBean);


//        new Thread(() -> {
//            List<String> orderList = new ArrayList<>();
//            for (String s : FileIOUtils.readFile2List(new File(Environment.getExternalStorageDirectory(), "123.txt"))) {
//                if (s.contains("<---")) {
//                    String substring = s.substring(s.lastIndexOf("<---") + 1).replaceAll("-","");
//                    Log.e(TAG,"裁剪：" + substring);
//                    orderList.add(substring);
//                }
//            }
//
//            StringBuilder stringBuilder = new StringBuilder();
//            for (String s : orderList) {
//                stringBuilder.append(s);
//                if(stringBuilder.toString().startsWith("2426") && stringBuilder.toString().endsWith("0d")){
//                    String s1 = stringBuilder.toString();
//                    Log.e(TAG, "getSiteInfo: 命令：" + s1);
//                    String s2 = StringUtils.hexStringToString(s);
//                    Log.e(TAG, "getSiteInfo: 转换：" + s2);
//                    stringBuilder.setLength(0);
//                }
//
//                else if(stringBuilder.toString().startsWith("3a3a") && stringBuilder.toString().endsWith("11")){
//                    String s1 = stringBuilder.toString();
//                    Log.e(TAG, "getSiteInfo: 命令：" + s1);
//                    String s2 = StringUtils.hexStringToString(s);
//                    Log.e(TAG, "getSiteInfo: 转换：" + s2);
//                    stringBuilder.setLength(0);
//                }
//            }
//
//
//        }).start();
    }

    int index = 1;
    String upDown = "ff";
    String inOut = "22";
    public void testInOut(){
        // 0x3a 0x3a 0x1c 0x22 0x01 0x25 0xff 0x11 0x11
        //   标识头   站数  进站  第几  线号 下行    结束
        String s = Integer.toHexString(index);
        while (s.length() < 2) {
            s = "0" + s;
        }
        String hex = "3a3a1c" + inOut + s + "25" + upDown + "1111";
        Log.d(TAG, "数据：站点序号：" + inOut + " --- " + index + " --- " + upDown);

        byte[] bytes = BytesUtils.hexToByteArray(hex);
        ComBean comBean = new ComBean(null,bytes,bytes.length);
        check(null,comBean);

//        if("ff".equals(upDown)){
//            int i = index - 1;
//            String s1 = downList.get(i);
//            Log.d(TAG, "testInOut: " + s1 + " --- " + downList.size());
//        } else if("00".equals(upDown)){
//            int i = upList.size() - index;
//            String s1 = upList.get(i);
//            Log.d(TAG, "testInOut: " + s1 + " --- " + upList.size());
//        }

        //根据上下行递增或递减索引
        if("ff".equals(upDown)){
            //进站才修改索引
            if("22".equals(inOut)){
                inOut = "11";
            } else if("11".equals(inOut)){
                inOut = "22";
                index ++;
            }
        } else if("00".equals(upDown)){
            //进站才修改索引
            if("22".equals(inOut)){
                inOut = "11";
            } else if("11".equals(inOut)){
                inOut = "22";
                index --;
            }
        }

        //如果大于下行列表数量则切换为上行
        if(index > downList.size()){
            index = upList.size();
            upDown = "00";
            Log.d(TAG, "testInOut: ----------------------");
        } else if(index < 1){
            index = 1;
            upDown = "ff";
            Log.d(TAG, "testInOut: ----------------------------");
        }
    }
}
