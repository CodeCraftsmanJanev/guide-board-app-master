package com.yunbiao.publicity_guideboard.serial;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.blankj.utilcode.util.FileIOUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.UiMessageUtils;
import com.yunbiao.publicity_guideboard.system.Cache;
import com.yunbiao.publicity_guideboard.system.Constants;
import com.yunbiao.publicity_guideboard.ui.MainActivity;
import com.yunbiao.publicity_guideboard.utils.BytesUtils;
import com.yunbiao.publicity_guideboard.utils.CheckUtils;
import com.yunbiao.publicity_guideboard.utils.StringUtils;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import tp.xmaihh.serialport.SerialHelper;
import tp.xmaihh.serialport.bean.ComBean;

public class TMDataHandler extends DataHandler{

    private final LinkedHashMap<Integer,String> upSiteMap = new LinkedHashMap<>();
    private final LinkedHashMap<Integer,String> downSiteMap = new LinkedHashMap<>();

    public TMDataHandler() {
        super(new Constraint("24242424","55",6));
    }

    @Override
    protected void loadCache() {
        ThreadUtils.executeBySingle(new ThreadUtils.SimpleTask<String>() {
            @Override
            public String doInBackground() throws Throwable {
                readSiteListUp();
                readSiteListDown();
                readSiteVersion();
                return readLineName();
            }

            @Override
            public void onSuccess(String lineName) {
                ArrayList<String> siteList = new ArrayList<>(upSiteMap.values());
                if(!siteList.isEmpty()){
                    UiMessageUtils.getInstance().send(Constants.TYPE_SITE_INFO,new String[]{lineName,siteList.get(0),siteList.get(siteList.size() - 1)});
                    UiMessageUtils.getInstance().send(Constants.TYPE_IN_OUT,new String[]{siteList.get(0),siteList.get(1)});
                }
            }
        });
    }

    @Override
    public void check(SerialHelper serialHelper,ComBean comBean) {
        try {
            if(comBean == null){
                d("comBean is null");
                return;
            }
            byte[] bytes = comBean.bRec;
            if (bytes == null || bytes.length <= 0) {
                d("bytes is null");
                return;
            }

            //过滤长度不正确的指令
            byte length = bytes[6];
            String lengthHex = BytesUtils.byteToHex(length);
            int lengthInt = BytesUtils.hex16to10(lengthHex);
            if(bytes.length < lengthInt){
                d("指令长度不正确");
                return;
            }


            byte[] dataBytes = BytesUtils.SubByte(bytes, 0, lengthInt);
            String hex = BytesUtils.bytesToHex(bytes);
            d( "截取数据片段：" + hex);
            handle(serialHelper,dataBytes);
        } catch (Exception e){
            e(e);
        }
    }

    @Override
    protected void handle(SerialHelper serialHelper,byte[] bytes) {
        try {
            byte deviceAddress = bytes[4];
            byte order = bytes[5];
            byte direction = bytes[7];

            if(deviceAddress == 0x17 && order == 0x19){
                lineInfo(serialHelper,bytes);
            }

            else if(deviceAddress == 0x16 && order == 0x17){
                siteList(bytes);
            }

            else if(order == 0x11){
                inOutSite(bytes);
            }

            else if(order == 0x21){
                correctTime(bytes);
            }
        } catch (Exception e){
            e(e);
        }
    }

    private void correctTime(byte[] bytes){
        // 242424241621103816071001161cbbcb
        byte[] bytes1 = BytesUtils.SubByte(bytes, 8, 6);
        int[] intArray = new int[bytes1.length];
        for (int i = 0; i < intArray.length; i++) {
            intArray[i] = BytesUtils.hex16to10(BytesUtils.byteToHex(bytes1[i]));
        }
        intArray[0] = Integer.parseInt("20" + intArray[0]);
        Log.d(TAG, "correctTime: " + Arrays.toString(intArray));
        UiMessageUtils.getInstance().send(MainActivity.CORRECT_TIME,intArray);
    }

    private void lineInfo(SerialHelper serialHelper,byte[] bytes){
        String head,addr,order,length,flag,status,line,version,auth;

        head = "24242424";
        addr = "17";
        order = "19";

        //组装包标识
        byte PACKAGE_FLAG = bytes[7];
        flag = BytesUtils.byteToHex(PACKAGE_FLAG);
        d("queryLineInfo: 包标识：" + flag);

        status = "0000";
        d("queryLineInfo: 自检状态：" + status);

        //组装线路名称长度和线路名
        String line_name = readLineName();
        if(!TextUtils.isEmpty(line_name)){
            try {
                d( "queryLineInfo: 线路名称：" + line_name);
                byte[] LINE_NAME = line_name.getBytes("GB2312");
                String lineNameHex = BytesUtils.bytesToHex(LINE_NAME);
                d( "queryLineInfo: 线路名称HEX：" + lineNameHex);

                int lineNameLength = BytesUtils.hexToByteArray(lineNameHex).length;
                d( "queryLineInfo: 线路名称长度：" + lineNameLength);

                String lineNameLengthHex = Integer.toHexString(lineNameLength);
                lineNameLengthHex = lineNameLengthHex.length() == 1 ? "0" + lineNameLengthHex : lineNameLengthHex;
                d( "queryLineInfo: 线路名称长度Hex：" + lineNameLengthHex);

                line = lineNameLengthHex + lineNameHex;
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                line = "0100";
            }
        } else {
            line = "0100";
        }
        d("queryLineInfo: 线路名称：" + line);

        //线路版本
        version = readSiteVersion();
        if(TextUtils.isEmpty(version)){
            version = "00";
        }
        d( "queryLineInfo: 线路版本：" + version);

        //组装数据长度
        String lengthHex = Integer.toHexString(BytesUtils.hexToByteArray(head + addr + order + flag + status + line + version).length + 3);
        length = lengthHex.length() == 1 ? "0" + lengthHex : lengthHex;

        String unAuthHex = head + addr + order + length + flag + status + line + version;
        d( "queryLineInfo: 校验前：" + unAuthHex);

        //组装校验位
        byte[] unAuthBytes = BytesUtils.hexToByteArray(unAuthHex);
        byte[] authValueBytes = CheckUtils.getCRCBytes(unAuthBytes);
        auth = BytesUtils.bytesToHex(authValueBytes);
        d("queryLineInfo: 校验位222：" + auth);

        String HEX = head + addr + order + length + flag + status + line + version + auth;
        d( "queryLineInfo: 最终数据：" + HEX);
        if(serialHelper != null){
            serialHelper.sendHex(HEX);
        }
    }

    private void siteList(byte[] bytes){
        int index = 0;

        index = 5;//命令
        byte ORDER = bytes[index];

        index = 7;//站点名列表版本
        byte STATION_LIST_VERSION = bytes[index];
        String stationListVersion = BytesUtils.byteToHex(STATION_LIST_VERSION);
//        d( "站点列表版本：" + stationListVersion);

        index = 8;//线路名长度
        byte LINE_NAME_LENGTH = bytes[index];
        int lineNameLength = BytesUtils.hex16to10(BytesUtils.byteToHex(LINE_NAME_LENGTH));
//        d( "线路名称长度：" + lineNameLength);

        index = 9;//线路名称内容
        byte[] LINE_NAME = BytesUtils.SubByte(bytes, index, lineNameLength);
        String lineNameHex = BytesUtils.bytesToHex(LINE_NAME);
        String lineName = StringUtils.hexStringToString(lineNameHex, "GB2312");
//        d( "线路名称HEX：" + lineNameHex);
//        d( "线路名称：" + lineName);

        index += lineNameLength;//方向 [12]
        byte DIRECTION = bytes[index];
//        d( DIRECTION == 0x00 ? "上行" : "下行");

        index ++;//站点总数
        byte STATION_NUMBER = bytes[index];
        int stationNumber = BytesUtils.hex16to10(BytesUtils.byteToHex(STATION_NUMBER));
//        d( "站点总数：" + stationNumber);

        index ++;//站点编号
        byte STATION_INDEX = bytes[index];
        int stationIndex = BytesUtils.hex16to10(BytesUtils.byteToHex(STATION_INDEX));
//        d( "站点编号：" + stationIndex);

        index ++;//站点名称长度
        byte STATION_NAME_LENGTH = bytes[index];
        int stationNameLength = BytesUtils.hex16to10(BytesUtils.byteToHex(STATION_NAME_LENGTH));
//        d( "站点名称长度：" + stationNameLength);

        index ++;//站点内容
        byte[] STATION_NAME = BytesUtils.SubByte(bytes, index, stationNameLength);
        String stationNameHex = BytesUtils.bytesToHex(STATION_NAME);
        String stationName = StringUtils.hexStringToString(stationNameHex);
//        d( "站点名称HEX：" + stationNameHex);
//        d( "站点名称：" + stationName);

        if(DIRECTION == 0x00){
            if(stationIndex == 1){
                d( "收到数据更新");
                if(!upSiteMap.isEmpty()){
                    d( "清除上行数据");
                    upSiteMap.clear();
                }
            }

            if(upSiteMap.containsKey(stationIndex)){
                String s = upSiteMap.get(stationIndex);
                if(TextUtils.equals(stationName,s)){
                    return;
                }
            }

            upSiteMap.put(stationIndex,stationName);
            d( "上行站点：" + stationNumber + " --- " + stationIndex + " --- " + stationName);

            if(stationIndex == stationNumber){
                d( "上行线路同步完毕------------------------");
                saveLineName(lineName);
                saveSiteVersion(stationListVersion);
                saveSiteListUp(upSiteMap.values());
                //发送线路信息和线路列表
                ArrayList<String> siteList = new ArrayList<>(upSiteMap.values());
                UiMessageUtils.getInstance().send(Constants.TYPE_SITE_INFO,new String[]{lineName,siteList.get(0),siteList.get(siteList.size() - 1)});
                UiMessageUtils.getInstance().send(Constants.TYPE_IN_OUT,new String[]{siteList.get(0),siteList.get(1)});
            }
        } else if(DIRECTION == 0x01){
            if(stationIndex == 1 && downSiteMap.size() > 1){
                downSiteMap.clear();
                d( "清除下行数据");
            }

            if(downSiteMap.containsKey(stationIndex)){
                String s = downSiteMap.get(stationIndex);
                if(TextUtils.equals(stationName,s)){
                    return;
                }
            }

            downSiteMap.put(stationIndex,stationName);
            d( "下行站点：" + stationNumber + " --- " + stationIndex + " --- " + stationName);
            saveSiteListDown(downSiteMap.values());
        }
    }

    private void inOutSite(byte[] bytes){
        byte UP_DOWN_FLAG = bytes[7];
        byte IN_OUT = bytes[8];//进出站标识
        if(IN_OUT == 0x01){
            return;
        }

        byte STATION_INDEX = bytes[14];//站点序号
        int inStationIndex = BytesUtils.hex16to10(BytesUtils.byteToHex(STATION_INDEX));
        int nextStationIndex = inStationIndex + 1;
        d( (IN_OUT == 0x00 ? "进站：" : "出站：") + inStationIndex + " --- 下一站：" + nextStationIndex);

        String inSiteName,nextStationName;
        String lineName = "---",start,end;
        switch (UP_DOWN_FLAG) {
            case 0x00://上行
            default:
                //处理上下行
                ArrayList<String> upSiteList = new ArrayList<>(upSiteMap.values());
                lineName = "---";
                start = upSiteList.get(0);
                end = upSiteList.get(upSiteList.size() - 1);

                //到站
                inSiteName = upSiteMap.get(inStationIndex);
                //下一站
                if(nextStationIndex >= upSiteMap.size()){
                    //反转上下数据
//                    List<String> upList = new ArrayList<>(upSiteMap.values());
//                    Collections.reverse(upList);
//                    for (int i = 0; i < upList.size(); i++) {
//                        downSiteMap.put(i + 1,upList.get(i));
//                    }
//                    d("下行数据为空，反转上行数据：" + downSiteMap.values());
//                    //下一站
//                    nextStationIndex = 0;
//                    nextStationName = downSiteMap.get(nextStationIndex);
                    nextStationName = "";
                } else {
                    nextStationName = upSiteMap.get(nextStationIndex);
                }
                break;
            case 0x01://下行
                if(downSiteMap.isEmpty()){
                    //反转上下数据
                    List<String> upList = new ArrayList<>(upSiteMap.values());
                    Collections.reverse(upList);
                    for (int i = 0; i < upList.size(); i++) {
                        downSiteMap.put(i + 1,upList.get(i));
                    }
                    d("下行数据为空，反转上行数据：" + downSiteMap.values());
                }
                //处理上下行
                ArrayList<String> downSiteList = new ArrayList<>(downSiteMap.values());
                lineName = "---";
                start = downSiteList.get(0);
                end = downSiteList.get(downSiteList.size() - 1);
                //到站
                inSiteName = downSiteMap.get(inStationIndex);
                //下一站
                if(nextStationIndex >= downSiteMap.size()){
//                    nextStationIndex = 0;
//                    nextStationName = upSiteMap.get(nextStationIndex);
                    nextStationName = "";
                } else {
                    nextStationName = downSiteMap.get(nextStationIndex);
                }
                break;
        }

        UiMessageUtils.getInstance().send(Constants.TYPE_SITE_INFO,new String[]{lineName,start,end});
        UiMessageUtils.getInstance().send(Constants.TYPE_IN_OUT,new String[]{inSiteName,nextStationName});
    }

    private String readLineName(){
        String string = Cache.getString(Cache.Key.LINE_NAME);
        d("读取线路名称：" + string);
        return string;
    }

    private void saveLineName(String lineName){
        Cache.setString(Cache.Key.LINE_NAME,lineName);
    }

    private String readSiteVersion(){
        String string = Cache.getString(Cache.Key.SITE_VERSION);
        d("读取站点列表版本：" + string);
        return string;
    }

    private void saveSiteVersion(String siteVersion){
        Cache.setString(Cache.Key.SITE_VERSION,siteVersion);
    }

    private void readSiteListUp(){
        upSiteMap.clear();
        String string = Cache.getString(Cache.Key.SITE_LIST_UP);
        d( "读取上行数据缓存：" + string);
        if(!TextUtils.isEmpty(string)){
            String[] split = string.split(",");
            for (int i = 0; i < split.length; i++) {
                String s = split[i];
                if(!TextUtils.isEmpty(s)){
                    upSiteMap.put(i + 1,s);
                }
            }
        }
    }

    private void saveSiteListUp(Collection<String> collection){
        StringBuilder stringBuilder = new StringBuilder();
        for (String s : collection) {
            stringBuilder.append(s).append(",");
        }
        boolean b = Cache.setString(Cache.Key.SITE_LIST_UP, stringBuilder.toString());
        d( "导出上行站点：" + b);
    }

    private void readSiteListDown(){
        downSiteMap.clear();
        String string = Cache.getString(Cache.Key.SITE_LIST_DOWN);
        d( "读取下行数据缓存：" + string);
        if(!TextUtils.isEmpty(string)){
            String[] split = string.split(",");
            for (int i = 0; i < split.length; i++) {
                String s = split[i];
                if(!TextUtils.isEmpty(s)){
                    downSiteMap.put(i + 1,s);
                }
            }
        }

        if(downSiteMap.isEmpty()){
            List<String> upList = new ArrayList<>(upSiteMap.values());
            Collections.reverse(upList);
            for (int i = 0; i < upList.size(); i++) {
                downSiteMap.put(i + 1,upList.get(i));
            }
            d("下行数据为空，反转上行数据：" + downSiteMap.values());
        }
    }

    private void saveSiteListDown(Collection<String> collection){
        StringBuilder stringBuilder = new StringBuilder();
        for (String s : collection) {
            stringBuilder.append(s).append(",");
        }
        boolean b = Cache.setString(Cache.Key.SITE_LIST_DOWN, stringBuilder.toString());
        d( "导出下行站点：" + b);
    }

    private void d(String log){
        LogUtils.d(TAG,log);
    }
    private void e(Throwable t){
        LogUtils.e(TAG,t);
    }








    public void testList(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<byte[]> orderList = new ArrayList<>();
                List<byte[]> respList = new ArrayList<>();

                List<String> stringList = FileIOUtils.readFile2List(new File(Environment.getExternalStorageDirectory(),"TMTMSite.txt"));
                if(!stringList.isEmpty()){
                    for (String s : stringList) {
                        if(TextUtils.isEmpty(s)){
                            continue;
                        }
                        if(s.contains("<-----")){
                            continue;
                        }
                        if(s.contains("--->")){
                            int i = s.lastIndexOf(">");
                            String substring = s.substring(i + 1);
                            byte[] bytes = BytesUtils.hexToByteArray(substring);
                            if(bytes.length < 6 || bytes[6] != 0x11){
                                continue;
                            }
                            respList.add(bytes);
                        }
                        if(s.contains("<---")){
                            int i = s.lastIndexOf("-");
                            String substring = s.substring(i + 1);

                            byte[] bytes = BytesUtils.hexToByteArray(substring);
                            if(bytes.length < 6){
                                continue;
                            }

                            byte deviceAddress = bytes[4];
                            byte order = bytes[5];
                            if(deviceAddress != 0x17 && deviceAddress != 0x16 && deviceAddress != 0x7f){
                                continue;
                            }
                            if(order != 0x19 && order != 0x17 && order != 0x11){
                                continue;
                            }
                            orderList.add(bytes);
                        }
                    }
                }

                for (byte[] data : orderList) {
                    byte length = data[6];
                    String lengthHex = BytesUtils.byteToHex(length);
                    int lengthInt = BytesUtils.hex16to10(lengthHex);
                    byte[] bytes = BytesUtils.SubByte(data, 0, lengthInt);

                    byte deviceAddress = bytes[4];
                    byte order = bytes[5];

                    if (order == 0x19) {
                    } else if(order == 0x17){
                        Log.d(TAG, "线路站点下载：" + BytesUtils.bytesToHex(bytes));
                        handle(null,data);
                    } else if(order == 0x11){
                    }
                }
            }
        }).start();
    }

    int testIndex = 1;
    String testInOutFlag = "00";
    String testUpDownFlag = "00";
    public void testNext(){
        String indexStr = Integer.toHexString(testIndex);
        String stringBuilder = "24242424" + //包头
                "7f" + //地址
                "11" + //命令
                "11" + //长度
                testUpDownFlag + //方向
                testInOutFlag +//进出站
                "3042313221" +
                ( indexStr.length() == 1 ? ("0" + indexStr) : indexStr) + //站点编号
                "9123";
        d( "next: 命令：" + stringBuilder);
        handle(null,BytesUtils.hexToByteArray(stringBuilder));

        //进站
        if(TextUtils.equals("00",testInOutFlag)){
            testInOutFlag = "01";
        } else {
            testInOutFlag = "00";
            testIndex ++; //进站时再累加序号
        }

        //上行
        if(TextUtils.equals("00",testUpDownFlag)){
            if(testIndex >= upSiteMap.size()){
                testIndex = 1;
                testUpDownFlag = "01";
            }
        } else if(TextUtils.equals("01",testUpDownFlag)){
            if(testIndex >= downSiteMap.size()){
                testIndex = 1;
                testUpDownFlag = "00";
            }
        }

    }

    public void testInfo(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                /*List<byte[]> orderList = new ArrayList<>();

                List<String> stringList = FileIOUtils.readFile2List(new File(Environment.getExternalStorageDirectory(),"TMTM.txt"));
                if(!stringList.isEmpty()){
                    for (String s : stringList) {
                        if(TextUtils.isEmpty(s)){
                            continue;
                        }
                        if(s.contains("<-----")){
                            continue;
                        }
                        if(s.contains("<---")){
                            int i = s.lastIndexOf("-");
                            String substring = s.substring(i + 1);

                            byte[] bytes = BytesUtils.hexToByteArray(substring);
                            if(bytes.length < 6 *//*|| bytes[6] != 0x11*//*){
                                continue;
                            }

                            byte deviceAddress = bytes[4];
                            byte order = bytes[5];
                            if(deviceAddress != 0x17 && deviceAddress != 0x16 && deviceAddress != 0x7f){
                                continue;
                            }
                            if(order != 0x19 && order != 0x17 && order != 0x11){
                                continue;
                            }
                            orderList.add(bytes);
                        }
                    }
                }

                for (byte[] data : orderList) {
                    byte length = data[6];
                    String lengthHex = BytesUtils.byteToHex(length);
                    int lengthInt = BytesUtils.hex16to10(lengthHex);
                    byte[] bytes = BytesUtils.SubByte(data, 0, lengthInt);

                    byte deviceAddress = bytes[4];
                    byte order = bytes[5];

                    if (order == 0x19) {
                        byte PACKAGE_FLAG = bytes[7];
                        String flag = BytesUtils.byteToHex(PACKAGE_FLAG);
                        Log.d(TAG, "线路信息查询：" + flag + " --- " + BytesUtils.bytesToHex(bytes));
                        handle(null,data);
                    } else if(order == 0x17){
//                        Log.d(TAG, "线路站点下载：" + BytesUtils.bytesToHex(bytes));
//                        anlzData(null,data);
                    } else if(order == 0x11) {
//                        Log.d(TAG, "进出站播报：" + BytesUtils.bytesToHex(bytes));
                    }
                }
*/
                String s =
                        "24242424" +
                        "16" +
                        "21" +
                        "10" +
                        "38" +
                        "16071001161c" +
                        "bbcb";
                byte[] bytes = BytesUtils.hexToByteArray(s);
                ComBean comBean = new ComBean("",bytes,bytes.length);
                check(null,comBean);
            }
        }).start();
    }
}
