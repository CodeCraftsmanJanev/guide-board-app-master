package com.janev.chongqing_bus_app.serial;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.blankj.utilcode.util.FileIOUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.UiMessageUtils;
import com.janev.chongqing_bus_app.adapter.SiteListAdapter;
import com.janev.chongqing_bus_app.system.Cache;
import com.janev.chongqing_bus_app.system.UiEvent;
import com.janev.chongqing_bus_app.tcp.message.MessageUtils;
import com.janev.chongqing_bus_app.utils.BytesUtils;
import com.janev.chongqing_bus_app.utils.StringUtils;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import tp.xmaihh.serialport.SerialHelper;
import tp.xmaihh.serialport.bean.ComBean;

//二、信息方向
//（1）终端 → LED 屏
//⚫ 改变线路：由终端直接发送信息到头牌、尾牌、腰牌； ⚫ 站点及报站信息：由终端直接发送信息到滚动屏、站点显示牌；
//⚫ 报警显示：由终端直接发送“ＳＯＳ”信息到头牌、尾牌、腰牌；
//⚫ 中心下发广告：由终端转送广告信息到滚动屏；
//⚫ 发出查询指令：由终端直接发送指令到各设备；
//（2）LED 屏 → 终端
//⚫ LED 屏收到指令后应答；
//⚫ LED 屏收到查询指令后应答；

//三、发送模式
//（1）终端 → LED 屏
//⚫ 在没有收到 LED 应答时，发送 2 次，如果 2 次都没有收到 LED 应答，则放弃该条指令(次数可由终端配置)
//（2）LED 屏 → 终端
//⚫ 在收到终端下发数据后，0.5s 内应答终端数据；
public class ChongqingV1Handler extends DataHandler{
    private static final String TAG = "ChongqingV1Handler";

    private String lineName;
    private List<String> upList;
    private List<String> downList;

    private boolean sendBroadSite = true;

    public ChongqingV1Handler(SerialHelper serialHelper) {
        super(serialHelper);
    }

    @Override
    protected Constraint initConstraint() {
        Constraint constraint = new Constraint();
        constraint.addStartEnd("7e00","7e",true);
        constraint.addStartEnd("7f00","7f",false);
        return constraint;
    }

    @Override
    protected void loadCache() {
        upList = new ArrayList<>();
        downList = new ArrayList<>();

        this.lineName = getCacheLineName();
        this.upList.addAll(getCacheList(true));
        this.downList.addAll(getCacheList(false));
        getDriverInfo();

        setLineName(false);
        setCacheList(true,false);
    }

    private void setDriverInfo(int lineStar,String workerId,byte POLITIC){
        Cache.setInt(Cache.Key.LINE_STAR,lineStar);
        Cache.setString(Cache.Key.WORKER_ID,workerId);
        Cache.setBoolean(Cache.Key.POLITIC,POLITIC == 0x01);

        UiMessageUtils.getInstance().send(UiEvent.EVENT_LINE_STAR,lineStar);
        UiMessageUtils.getInstance().send(UiEvent.EVENT_WORKER_ID,workerId);
        UiMessageUtils.getInstance().send(UiEvent.EVENT_POLITIC,POLITIC == 0x01);
    }

    private void getDriverInfo(){
        UiMessageUtils.getInstance().send(UiEvent.EVENT_LINE_STAR,Cache.getInt(Cache.Key.LINE_STAR));
        UiMessageUtils.getInstance().send(UiEvent.EVENT_WORKER_ID,Cache.getString(Cache.Key.WORKER_ID));
        UiMessageUtils.getInstance().send(UiEvent.EVENT_POLITIC,Cache.getBoolean(Cache.Key.POLITIC));
    }

    private void setBroadSite(int upDown, int inOut, int index){
        if(sendBroadSite){
            UiMessageUtils.getInstance().send(UiEvent.EVENT_BROAD_SITE,new int[]{upDown,inOut,index});
        }
    }

    private void setSyncTime(String time) {
        if(TextUtils.isEmpty(time) || time.length() < 12){
            return;
        }

        try {
            int year = Integer.parseInt(time.substring(0, 4));//年
            int month = Integer.parseInt(time.substring(4,6));//月
            int day = Integer.parseInt(time.substring(6,8));//日
            int hours = Integer.parseInt(time.substring(8,10));//日
            int minutes = Integer.parseInt(time.substring(10,12));//日

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("timeStr",time);
            jsonObject.put("timeArray",new int[]{year,month,day,hours,minutes});
            UiMessageUtils.getInstance().send(UiEvent.EVENT_SYNC_TIME,jsonObject);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void setLineName(boolean save){
        if(save){
            MessageUtils.setLineName(this.lineName);
        }

        UiMessageUtils.getInstance().send(UiEvent.EVENT_LINE_NAME,lineName);
    }

    private String getCacheLineName(){
        return MessageUtils.getLineName();
    }

    private void setCacheList(boolean isUp,boolean saveCache){
        //加入文件缓存
        if(saveCache){
            StringBuilder stringBuilder = new StringBuilder();
            List<String> list = new ArrayList<>(isUp ? upList : downList);
            for (int i = 0; i < list.size(); i++) {
                if(i != 0){
                    stringBuilder.append(",");
                }
                stringBuilder.append(list.get(i));
            }
            Cache.setString(isUp ? Cache.Key.SITE_LIST_UP : Cache.Key.SITE_LIST_DOWN, stringBuilder.toString());
        }

        //下行线路为空则反转上行线路
        if(!isUp){
            if(downList.isEmpty()){
                ArrayList<String> strings = new ArrayList<>(upList);
                Collections.reverse(strings);
                downList.addAll(strings);
            }
        }

        //发送站点列表
        SiteList siteList = new SiteList(isUp ? upList : downList);
        UiMessageUtils.getInstance().send(UiEvent.EVENT_SITE_LIST,siteList);

        sendBroadSite = true;
    }

    private List<String> getCacheList(boolean isUp){
        List<String> list = new ArrayList<>();
        String string = isUp ? Cache.getString(Cache.Key.SITE_LIST_UP) : Cache.getString(Cache.Key.SITE_LIST_DOWN);
        Log.d(TAG, "getCacheList: " + string);
        if(!TextUtils.isEmpty(string)){
            String[] split = string.split(",");
            list.addAll(Arrays.asList(split));
        }
        return list;
    }

    @Override
    protected void handle(SerialHelper serialHelper, byte[] bytes) {
        byte FRAME_HEAD = bytes[0];//[1]帧头
        byte MESSAGE_DIRECTION = bytes[1];//[1]信息方向 0x00：GPS发出，0x01：LED发出
        byte DEVICE_NUMBER = bytes[2];//[1]设备编号
        byte ORDER = bytes[3];//[1]命令字
        byte[] DATA = BytesUtils.SubByte(bytes,4,bytes.length - 6);//数据
        byte CHECK_SUM = bytes[bytes.length - 2];//[1]校验和
        byte FRAME_END = bytes[bytes.length - 1];//[1]帧尾

        Log.d(TAG, "handle: " + BytesUtils.bytesToHex(bytes));
        Log.d(TAG, "handle: " + BytesUtils.byteToHex(DEVICE_NUMBER) + " --- " + BytesUtils.byteToHex(ORDER));
        if(FRAME_HEAD == 0x7f){
            if(DEVICE_NUMBER == 0X01 && ORDER == 0x10){
                // 7f 00 01 10 80 02
                // d6d8c7ecb1b1d5beb1b1b9e3b3a12fa3b8a3b1a3b82fbdf5ccecbfb5b6bc00
                // ef 7f
                resolveLineInfo2(bytes);
            }
        } else if(FRAME_HEAD == 0x7e){
            //报站
            if(DEVICE_NUMBER == 0X04 && (ORDER == 0X10)){
                resolveBroadSite(DATA);
                reply(serialHelper);
            }
            //线路站点
            else if(DEVICE_NUMBER == 0X05 && (ORDER == 0X30 || ORDER == 0X31)){
                resolveSiteList(bytes);
                reply(serialHelper);
            }
            //时间同步
            else if(DEVICE_NUMBER == 0X05 && (ORDER == 0X50)){
                resolveSyncTime(DATA);
            }
            //车辆信息
            else if(DEVICE_NUMBER == 0X05 && (ORDER == 0X60)){
                resolveCarInfo(bytes);
            }
            //线路星级
            else if((DEVICE_NUMBER == 0x04 || DEVICE_NUMBER == 0x05 || DEVICE_NUMBER == 0x06) && ORDER == 0X70){
                resolveDriverInfo(DATA);
            }
        }

    }

    private void resolveCarInfo(byte[] bytes){
        // 7e 00 05 60 1b
        // [5] 00 38 32 35 35 37
        // [11]00 d3 e5 42 50 38 37 39 31
        // [20]00 31 33 39 32 30 31 37 30 30 30 37
        // 00
        // 587e

        String s = BytesUtils.bytesToHex(BytesUtils.SubByte(bytes, 5, 6));
        d("resolveCarInfo: 车辆自编号：" + s);
        MessageUtils.setCarNumber(s);

        String s1 = BytesUtils.bytesToHex(BytesUtils.SubByte(bytes,11,9));
        d("resolveCarInfo: 车牌号：" + s1);
        MessageUtils.setCarLicenseNumber(s1);

        //已经解码后的终端编号
        String terminalId = BytesUtils.bytesToHex(BytesUtils.SubByte(bytes,20,12));
        terminalId = terminalId.replaceAll("^0+","");
        terminalId = StringUtils.hexStringToString(terminalId,"GBK");
        terminalId = MessageUtils.addZero(terminalId,12);
        d("resolveCarInfo: 终端编号：" + terminalId);
        if(!TextUtils.equals(MessageUtils.getTerminalNumber(),terminalId)){
            MessageUtils.setTerminalNumber(terminalId);
            UiMessageUtils.getInstance().send(UiEvent.EVENT_CONNECT_TCP);
        }
    }

    /**
     * 解析站点列表
     * @param data
     */
    private void resolveSiteList(byte[] data){
        byte upDown = data[3];
        d("resolveSiteList: " + (upDown == (byte)0x30 ? "上行" : "下行"));
        final Deque<Byte> byteQueue = new LinkedList<>();
        for (byte datum : data) {
            byteQueue.offer(datum);
        }

        //跳过首位四个字节
        for (int i = 0; i < 4; i++) {
            byteQueue.pollFirst();
        }

        //数据长度
        byte[] lengthBytes = new byte[2];
        for (int i = 0; i < lengthBytes.length; i++) {
            lengthBytes[i] = byteQueue.pollFirst();
        }

        //跳过结尾2个字节
        for (int i = 0; i < 2; i++) {
            byteQueue.pollLast();
        }

        while (!byteQueue.isEmpty()) {
            try {
                //站点序号
                int siteIndex = BytesUtils.hex16to10(BytesUtils.byteToHex(byteQueue.pollFirst()));
                d("序号：" + siteIndex);

                //清除上下行站点
                if(siteIndex <= 1){
                    if(upDown ==(byte)0x30){
                        upList.clear();
                    } else {
                        downList.clear();
                    }
                }

                //中文名长度
                int chLength = BytesUtils.hex16to10(BytesUtils.byteToHex(byteQueue.pollFirst()));

                //中文名
                if(chLength > 0){
                    byte[] chBytes = new byte[chLength];
                    for (int i = 0; i < chBytes.length; i++) {
                        chBytes[i] = byteQueue.pollFirst();
                    }
                    String chName = StringUtils.hexStringToString(BytesUtils.bytesToHex(chBytes));
                    d("站名: " + chName);

                    if(upDown ==(byte)0x30){
                        upList.add(chName);
                    } else {
                        downList.add(chName);
                    }
                }

                //英文名长度
                int enLength = BytesUtils.hex16to10(BytesUtils.byteToHex(byteQueue.pollFirst()));
                if(enLength > 0){
                    byte[] enBytes = new byte[enLength];
                    for (int i = 0; i < enBytes.length; i++) {
                        enBytes[i] = byteQueue.pollFirst();
                    }
                    String enName = StringUtils.hexStringToString(BytesUtils.bytesToHex(enBytes));
                    d("站名: " + enName);
                }
            } catch (Exception e){
                Log.e(TAG, "resolveSiteList: ",e);
            }
        }

        setCacheList(upDown ==(byte)0x30,true);
    }

    private void addToList(Queue<Byte> data,byte order,int siteIndex,String siteName){
        boolean isUp = order == 0x30;
        d((isUp ? "上行" : "下行") + "：" + siteIndex + "，" + siteName);

        if(isUp){
            upList.add(siteName);
        } else {
            downList.add(siteName);
        }

        if(data.isEmpty()){
            d((isUp ? "上行" : "下行") + "站点收集结束");
            //保存缓存
            setCacheList(isUp,true);
        }
    }

    /**
     * 解析线路信息
     * @param data
     */
    private void resolveLineInfo2(byte[] data){
        try {
            sendBroadSite = false;

            d("解析线路名：数据长度 -- " + data.length);
            String hex = BytesUtils.bytesToHex(BytesUtils.SubByte(data, 6, data.length - 6 - 2));
            d("解析线路名：数据裁剪 -- " + hex);
            while (hex.endsWith("0")) {
                hex = hex.substring(0, hex.length() - 1);
            }
            d("解析线路名：数据裁剪2 -- " + hex);

            String content = StringUtils.hexStringToString(hex);
            d("解析线路名：转换内容 -- " + content);

            if(!TextUtils.isEmpty(content) && content.contains("/")){
                String[] split = content.split("/");
                if(split.length > 0){
                    d("起点站: " + split[0]);
                    d("线路号: " + split[1]);
                    d("终点站: " + split[2]);
                    this.lineName = com.blankj.utilcode.util.StringUtils.toDBC(split[1]);
                    setLineName(true);
                }
            }
        } catch (Exception e){
            e.printStackTrace();
            e(e);
        }
    }

    /**
     * 解析线路信息
     * @param data
     */
    private void resolveLineInfo(byte[] data){
        int lineNameLength = BytesUtils.hex16to10(BytesUtils.byteToHex(data[0]));
        byte[] bytes = BytesUtils.SubByte(data, 1, lineNameLength);
        String lineName = StringUtils.hexStringToString(BytesUtils.bytesToHex(bytes));
        d("线路号：" + lineName + "，线路号长度：" + lineNameLength);
        this.lineName = lineName;
        setLineName(true);
    }

    private byte upDown_Byte = 0x00;
    /**
     * 解析报站
     * @param data
     */
    private void resolveBroadSite(byte[] data){
        byte UP_DOWN = data[1];
        byte IN_OUT = data[3];

        int upDown = UP_DOWN == 0x00 ? 0 : 1;
        int inOut = IN_OUT == 0x00 ? 0 : 1;
        int index = BytesUtils.hex16to10(BytesUtils.byteToHex(data[2]));
        d("站号：" + index + "，" + (IN_OUT == 0x00 ? "进站" : "出站") + "，" + (UP_DOWN == 0x00 ? "上行" : "下行"));
        if(upDown_Byte != UP_DOWN){
            upDown_Byte = UP_DOWN;
            setCacheList(upDown_Byte == 0x00,false);
        }

        //下行计算站号
        if(upDown_Byte == 0x01){
            index = index - upList.size() + 1;
            d("下行站点号：" + index);
        }

        setBroadSite(upDown,inOut,index);
    }

    private void resolveSyncTime(byte[] data){
        // 7E 00 05
        // 50 时间同步
        // 08 数据长度
        // 20 22 10 12 21 38 18 03
        // 00 预留
        // 5F 7E

        // 2022101221381803

        byte datum = data[0];
        byte[] bytes = BytesUtils.SubByte(data, 1, datum);
        String s = BytesUtils.bytesToHex(bytes);
        d("同步时间：" + s);
        setSyncTime(s);
    }

    private void resolveDriverInfo(byte[] bytes) {
        //取数据长度
        int length = Integer.parseInt(BytesUtils.bytesToHex(BytesUtils.SubByte(bytes, 0, 1)), 16);
        Log.d(TAG, "test: 数据长度：" + length);

        //根据2f截取
        int index = 0;
        for (int i = 0; i < bytes.length; i++) {
            if (bytes[i] == (byte)0x2f) {
                index = i;
                break;
            }
        }

        //前半段
        byte[] preBytes = BytesUtils.SubByte(bytes, 1, index - 1);
        String s4 = BytesUtils.bytesToHex(preBytes);
        Log.d(TAG, "test: 前半段：" + s4);

        String lineNumber = BytesUtils.bytesToHex(BytesUtils.SubByte(preBytes, 0, preBytes.length - 2));
        Log.d(TAG, "test: 线路号：" + lineNumber);

        int lineStar = Integer.parseInt(BytesUtils.bytesToHex(BytesUtils.SubByte(preBytes, preBytes.length - 1, 1)),16);
        Log.d(TAG, "test: 线路星级：" + lineStar);

        byte[] suffixBytes = BytesUtils.SubByte(bytes, index, bytes.length - index - 1);
        String s3 = BytesUtils.bytesToHex(suffixBytes);
        Log.d(TAG, "test: 后半段：" + s3);

        byte POLITIC = BytesUtils.SubByte(suffixBytes, suffixBytes.length - 1, 1)[0];
        Log.d(TAG, "test: 党员：" + POLITIC);
        if(POLITIC == (byte)0x01){
            UiMessageUtils.getInstance().send(UiEvent.EVENT_POLITIC,true);
        }

        String info = BytesUtils.bytesToHex(BytesUtils.SubByte(suffixBytes, 0, suffixBytes.length - 1));
        Log.d(TAG, "test: 信息：" + info);
        info = StringUtils.hexStringToString(info);
        Log.d(TAG, "test: 信息：" + info);

        String[] split1 = info.split("/");
        String workerId = split1[2];
        setDriverInfo(lineStar,workerId,POLITIC);
    }

    private void reply(SerialHelper serialHelper){
        if(serialHelper == null){
            return;
        }
        String HEX = "7E0106120000";
        serialHelper.sendHex(HEX);
    }

    public static class SiteList{
        private List<SiteListAdapter.Site> dataList;
        private String start,end;

        public SiteList(List<String> list) {
            this.dataList = new ArrayList<>();
            if(list == null || list.isEmpty()){
                return;
            }
            for (int i = 0; i < list.size(); i++) {
                String s = list.get(i);
                String name = s;
                boolean isResponsive = false;
                if (name.contains("_")) {
                    String[] s1 = name.split("_");
                    name = s1[0];
                    String isRStr = s1[1];
                    isResponsive = !TextUtils.isEmpty(isRStr) && Boolean.parseBoolean(isRStr);
                }
                /*if(name.contains("1")){
                    name = name.replaceAll("1","一");
                } else if(name.contains("2")){
                    name = name.replaceAll("2","二");
                } else if(name.contains("3")){
                    name = name.replaceAll("3","三");
                } else if(name.contains("4")){
                    name = name.replaceAll("4","四");
                } else if(name.contains("5")){
                    name = name.replaceAll("5","五");
                } else if(name.contains("6")){
                    name = name.replaceAll("6","六");
                } else if(name.contains("7")){
                    name = name.replaceAll("7","七");
                } else if(name.contains("8")){
                    name = name.replaceAll("8","八");
                } else if(name.contains("9")){
                    name = name.replaceAll("9","九");
                }*/
                this.dataList.add(new SiteListAdapter.Site(i + 1,name,isResponsive));
            }
            this.start = list.get(0);
            this.end = list.get(list.size() -1);
        }

        public List<SiteListAdapter.Site> getList() {
            return dataList;
        }

        public String getStart() {
            return start;
        }

        public String getEnd() {
            return end;
        }
    }


    public void test(){
        new Thread(() -> {
//            File file = new File(Environment.getExternalStorageDirectory(),"20230714");
//            List<String> strings = FileIOUtils.readFile2List(file);
//            for (String string : strings) {
//                if(!string.contains("SerialPortService:  <---")){
//                    continue;
//                }
//
//                String[] split = string.split("SerialPortService:  <---");
//                String s = split[1];
//                byte[] bytes = BytesUtils.hexToByteArray(s);
//
//                check(null,new ComBean("",bytes,bytes.length));
//            }

            // 7e 00 06
            // 70
            // 2a
            // 35363000032fd2fcd7e6bda82f3231313139382fcef7b2bfb9abbdbb2db8dfd0c2b7d6b9abcbbe2f01
            // 00
            // 1b7e
//            String s = "2a3536300003" +
//                    "2fd2fcd7e6bda82f3231313139382fcef7b2bfb9abbdbb2db8dfd0c2b7d6b9abcbbe2f01" +
//                    "00";
//            byte[] bytes = BytesUtils.hexToByteArray(s);
//
//            //取数据长度
//            int length = Integer.parseInt(BytesUtils.bytesToHex(BytesUtils.SubByte(bytes, 0, 1)), 16);
//            Log.d(TAG, "test: 数据长度：" + length);
//
//            //根据2f截取
//            int index = 0;
//            for (int i = 0; i < bytes.length; i++) {
//                if (bytes[i] == (byte)0x2f) {
//                    index = i;
//                    break;
//                }
//            }
//
//            //前半段
//            byte[] preBytes = BytesUtils.SubByte(bytes, 1, index - 1);
//            String s4 = BytesUtils.bytesToHex(preBytes);
//            Log.d(TAG, "test: 前半段：" + s4);
//
//            String lineNumber = BytesUtils.bytesToHex(BytesUtils.SubByte(preBytes, 0, preBytes.length - 2));
//            Log.d(TAG, "test: 线路号：" + lineNumber);
//
//            String lineStar = BytesUtils.bytesToHex(BytesUtils.SubByte(preBytes, preBytes.length - 2, 1));
//            Log.d(TAG, "test: 线路星级：" + lineStar);
//
//            byte[] suffixBytes = BytesUtils.SubByte(bytes, index, bytes.length - index - 1);
//            String s3 = BytesUtils.bytesToHex(suffixBytes);
//            Log.d(TAG, "test: 后半段：" + s3);
//
//            String dangyuan = BytesUtils.bytesToHex(BytesUtils.SubByte(suffixBytes, suffixBytes.length - 1, 1));
//            Log.d(TAG, "test: 党员：" + dangyuan);
//
//            String info = BytesUtils.bytesToHex(BytesUtils.SubByte(suffixBytes, 0, suffixBytes.length - 1));
//            Log.d(TAG, "test: 信息：" + info);
//            info = StringUtils.hexStringToString(info);
//            Log.d(TAG, "test: 信息：" + info);

//            String s = "7e0006702a35363000032fd2fcd7e6bda82f3231313139382fcef7b2bfb9abbdbb2db8dfd0c2b7d6b9abcbbe2f01001b7e";
//            byte[] bytes = BytesUtils.hexToByteArray(s);
//            check(null,new ComBean("",bytes,bytes.length));

            File file = new File(Environment.getExternalStorageDirectory(),"20240304");

            new Thread(() -> {
                List<String> strings = FileIOUtils.readFile2List(file);
                for (String string : strings) {
                    if(!string.contains("<---")){
                        continue;
                    }
                    String[] split = string.split("<---");
                    String s = split[1];
                    s = s.replaceAll(" ","");
                    if(TextUtils.isEmpty(s)){
                        continue;
                    }
                    Log.d(TAG, "test: " + s);
                    byte[] bytes = BytesUtils.hexToByteArray(s);
                    check(new ComBean("",bytes,bytes.length));
                }
            }).start();
        }).start();

    }

    public void test1(){
        // 7E 00 05
        // 10 数据指令
        // 05 滚动速度
        // 00 上行
        // 01 站点号
        // 01 出站
        // 02 左移
        // 00 红色
        // 20 CF C2 D2 BB D5 BE 20 BF C6 BC BC D1 A7 D4 BA C4 CF C3 C5 C2 B7 BF DA 20 00
        // 41 7E

        // 7E 00 05
        // 10 报站
        // 05 滚动速度
        // 00 上行
        // 02 站点号
        // 00 进站
        // 02 左移
        // 00 红色
        // 20 BF C6 BC BC D1 A7 D4 BA C4 CF C3 C5 C2 B7 BF DA 20 B5 BD C1 CB 20 00
        // 4E 7E
//      String s = "7E 00 05 10 05 00 02 01 02 00 20 CF C2 D2 BB D5 BE 20 BF C6 BC BC D1 A7 D4 BA C4 CF C3 C5 20 00 52 7E".replaceAll(" ","");

        Log.d(TAG, "test1: 当前站号：" + siteNumberInt);
        Log.d(TAG, "test1: 当前进出站：" + inOut);
        Log.d(TAG, "test1: 当前上下行：" + upDown);

        String siteNumber = Integer.toHexString(siteNumberInt);
        while (siteNumber.length() < 2) {
            siteNumber = "0" + siteNumber;
        }

        String s = ("7E 00 04 10 05 " + upDown + siteNumber + inOut + "02 00 4E 7E").replaceAll(" ","");
        Log.d(TAG, "test1: " + s);
        byte[] bytes = BytesUtils.hexToByteArray(s);
        check(new ComBean("",bytes,bytes.length));

        if("00".equals(inOut)){
            inOut = "01";
        } else if("01".equals(inOut)){
            inOut = "00";
            siteNumberInt += 1;
        }

        if ("00".equals(upDown) && siteNumberInt > upList.size()) {
            siteNumberInt = upList.size();
            upDown = "01";
        } else if("01".equals(upDown) && siteNumberInt > (upList.size() + downList.size())){
            siteNumberInt = 1;
            upDown = "00";
        }
    }

    private boolean is1 = true;
    private String upDown = "00";//00上行，01下行
    private int siteNumberInt = 1;
    private String inOut = "00";//00进站，01出站
    public void test2(){
        List<String> strings = new ArrayList<>();
        if(is1){
            is1 = false;
            strings.add("7E 00 05 30 01 44 01 0C B4 F3 D1 A7 B3 C7 B6 AB D2 BB C2 B7 00 02 10 BF C6 BC BC D1 A7 D4 BA C4 CF C3 C5 C2 B7 BF DA 00 03 0C BF C6 BC BC D1 A7 D4 BA C4 CF C3 C5 00 04 08 BF C6 BC BC D1");
            strings.add("A7 D4 BA 00 05 0C BF C6 BC BC D1 A7 D4 BA C2 B7 BF DA 00 06 04 BB A2 CF AA 00 07 0A B4 F3 D1 A7 B3 C7 C3 C0 D4 BA 00 08 0D B4 F3 D1 A7 B3 C7 CE F7 C2 B7 32 D5 BE 00 09 0D B4 F3 D1 A7 B3");
            strings.add("C7 CE F7 C2 B7 31 D5 BE 00 0A 07 C1 FA BA FE 55 B3 C7 00 0B 08 D6 D8 CA A6 C4 CF C3 C5 00 0C 0A B4 F3 D1 A7 B3 C7 B1 B1 C2 B7 00 0D 08 BD DC C7 E0 B4 F3 B5 C0 00 0E 0A B4 F3 D1 A7 B3 C7");
            strings.add("D6 D8 CA A6 00 0F 0A B4 F3 D1 A7 B3 C7 D2 BD B4 F3 00 10 08 D2 BD D2 A9 B8 DF D7 A8 00 11 0C D2 BD D2 A9 B8 DF D7 A8 CB DE C9 E1 00 12 08 C7 DA CE F1 D1 A7 D4 BA 00 13 06 B5 E7 D7 D3 D0");
            strings.add("A3 00 14 06 E2 F9 D4 C3 C2 B7 00 15 0A B3 C2 B6 AB C2 B7 C2 B7 BF DA 00 16 06 C7 C5 B1 B1 C2 B7 00 17 0A B4 F3 D1 A7 B3 C7 D2 BB D0 A1 00 18 08 BC D2 BF B5 D2 BD D4 BA 00 19 10 D1 A7 B8");
            strings.add("AE D4 C3 D4 B0 B5 DA B6 FE D0 A1 D1 A7 00 76 7E");
        } else {
            strings.add("7e000531014b0110d1a7b8aed4c3d4b0b5dab6fed0a1d1a7000208bcd2bfb5d2bdd4ba00030ab4f3d1a7b3c7d2bbd0a1000406c7c5b1b1c2b700050ab3c2");
            strings.add("b6abc2b7c2b7bfda000606e2f9d4c3c2b7000706b5e7d7d3d0a3000808c7dacef1d1a7d4ba00090cd2bdd2a9b8dfd7a8cbdec9e1000a08d2bdd2a9b8dfd7");
            strings.add("a8000b0ab4f3d1a7b3c7d2bdb4f3000c0ab4f3d1a7b3c7d6d8caa6000d08bddcc7e0b4f3b5c0000e0ab4f3d1a7b3c7b1b1c2b7000f08d6d8caa6c4cfc3c5");
            strings.add("00100db4f3d1a7b3c7cef7c2b731d5be00110db4f3d1a7b3c7cef7c2b732d5be00120ab4f3d1a7b3c7c3c0d4ba001304bba2cfaa00140eb4f3d1a7b3c7c4");
            strings.add("cfc2b7d6d0b6ce00150cbfc6bcbcd1a7d4bac2b7bfda001608bfc6bcbcd1a7d4ba00170cbfc6bcbcd1a7d4bac4cfc3c5001810bfc6bcbcd1a7d4bac4cfc3");
            strings.add("c5c2b7bfda00190cb4f3d1a7b3c7b6abd2bbc2b7006a7e");
        }

        new Thread(){

            @Override
            public void run() {
                for (String string : strings) {

                    String s = string.replaceAll(" ", "");
                    Log.d(TAG, "test2: " + s);
                    byte[] bytes = BytesUtils.hexToByteArray(s);
                    Log.d(TAG, "test2: " + bytes.length);
                    check(new ComBean("",bytes,bytes.length));

                    try {
                        Thread.sleep(60);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    public void test3(){
        // 7E 00 06
        // 50 时间同步
        // 08 20 22 10 12 21 38 19 03 00
        // 5D 7E

//        String s = "7E 00 05 50 08 20 22 10 12 21 38 18 03 00 5F 7E".replaceAll(" ","");
//        byte[] bytes = BytesUtils.hexToByteArray(s);
//        check(null,new ComBean("",bytes,bytes.length));


//        String s = "7E 00 05 70 ";
//
//        String lineName = "622路";
//        String lineNameHex = BytesUtils.bytesToHex(lineName.getBytes(Charset.forName("GB2312")));
//        Log.d(TAG, "test3: 线路名：" + lineName + " --- " + lineNameHex);
//
//        String lineNameLengthHex = Integer.toHexString(lineNameHex.length() / 2);
//        while (lineNameLengthHex.length() < 2) {
//            lineNameLengthHex = "0" + lineNameLengthHex;
//        }
//        Log.d(TAG, "test3: 线路名长度：" + lineNameLengthHex);
//
//        String lineStarHex = "05";
//
//        String driverInfoHex = "2F 00 2F 00 2F 00 2F 00";
//
//        String TEMP = "00";
//
//        s = s + lineNameLengthHex + lineNameHex + lineStarHex + driverInfoHex + TEMP + "5A 7E";
//
//        Log.d(TAG, "test3: 数据：" + s);
//
//        byte[] bytes = BytesUtils.hexToByteArray(s.replaceAll(" ",""));
//        check(null,new ComBean("",bytes,bytes.length));



        String ss = "7F 00 01 10 80 02 B4 F3 D1 A7 B3 C7 B6 AB D2 BB C2 B7 2F A3 B2 A3 B7 A3 B2 2F D1 A7 B8 AE D4 C3 D4 B0 B5 DA B6 FE D0 A1 D1 A7 00 F0 7F".replaceAll(" ","");
        byte[] bytes = BytesUtils.hexToByteArray(ss);
        Log.d(TAG, "test3: " + bytes.length);
        ComBean comBean = new ComBean("",bytes,bytes.length);

        if(comBean.bRec != null && comBean.bRec.length > 0){
            if(comBean.bRec[0] == 0x7F && comBean.bRec[comBean.bRec.length - 1] == 0x7F){
                byte DEVICE_NUMBER = comBean.bRec[2];//[1]设备编号
                byte ORDER = comBean.bRec[3];//[1]命令字

                if((DEVICE_NUMBER == 0x01 || DEVICE_NUMBER == 0x02 || DEVICE_NUMBER == 0X03) && ORDER == 0X10){
                    d("解析线路名：数据长度 -- " + comBean.bRec.length);
                    String hex = BytesUtils.bytesToHex(BytesUtils.SubByte(comBean.bRec, 6, comBean.bRec.length - 6 - 2));
                    d("解析线路名：数据裁剪 -- " + hex);
                    while (hex.endsWith("0")) {
                        hex = hex.substring(0, hex.length() - 1);
                    }
                    d("解析线路名：数据裁剪2 -- " + hex);

                    String content = StringUtils.hexStringToString(hex);
                    d("解析线路名：转换内容 -- " + content);

                    if(!TextUtils.isEmpty(content) && content.contains("/")){
                        String[] split = content.split("/");
                        if(split.length > 0){
                            d("起点站: " + split[0]);
                            d("线路号: " + split[1]);
                            d("终点站: " + split[2]);
                            this.lineName = com.blankj.utilcode.util.StringUtils.toDBC(split[1]);
                            setLineName(true);
                        }
                    }
                }
            }
        }

        // 7E 00 04 10
        // 05
        // 00
        // 01
        // 01
        // 02
        // 00 20 C6 F0 B5 E3 D5 BE 20 B4 F3 D1 A7 B3 C7 B6 AB D2 BB C2 B7 20 00 7C 7E
    }

}


// 7e 帧头
// 00 信息方向
// 05 设备编号
// 30 命令
// 00ef 数据帧长度
// 01 第一站序号
// 0c 中文名长度
// c1fad6decde5cae0c5a6d5be 中文名
// 00 英文名长度
// 02
// 08 d3e3bafac2b7bfda 00
// 03
// 0a d3e3bafac2b7d0a1d1a7 00
// 04
// 0a bcfdbad3c2b7d6d0b6ce 00
// 05
// 06 bcfdbad3c2b7 00
// 06
// 06 bdadd6dec2b7 00
// 07
// 08 d0c2c3f1bdd6bfda 00
// 08
// 08 d3e3b6b4b6fed0a3 00
// 09
// 06 cfc2bad3c2b7 00
// 0a
// 06 d3e3c7e1c2b7 00
// 0b
// 08 c8cbc3f1b9e3b3a1 00
// 0c
// 08 d3e3b6b4cbc4d0a3 00
// 0d
// 08 b0ebb5babfb5b3c7 00
// 0e
// 06 b7e1bbaac2b7 00
// 0f
// 08 cce5d3fdd6d0d0c4 00
// 10
// 08 d3e3b6b4d6d0d1a7 00
// 11
// 06 c8fdbdadbdd6 00
// 12
// 06 c7ecbdadb3a7 00
// 13
// 08 c7d8bcd2d4bad7d3 00
// 14
// 0c b4f3bdadcef7c2b7d6d0b6ce 00
// 15
// 0c b4f3bdadcef7c2b7c4cfb6ce 00
// cb 校验和
// 7e 结尾