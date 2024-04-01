package com.janev.chongqing_bus_app.serial;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.blankj.utilcode.util.FileIOUtils;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.UiMessageUtils;
import com.janev.chongqing_bus_app.db.DaoManager;
import com.janev.chongqing_bus_app.db.Site;
import com.janev.chongqing_bus_app.serial.request_v2.QueryLineRequest;
import com.janev.chongqing_bus_app.serial.request_v2.ReplyRequest;
import com.janev.chongqing_bus_app.serial.request_v2.SiteInfo;
import com.janev.chongqing_bus_app.system.Cache;
import com.janev.chongqing_bus_app.system.UiEvent;
import com.janev.chongqing_bus_app.tcp.message.CrashReportRequest;
import com.janev.chongqing_bus_app.tcp.message.MessageUtils;
import com.janev.chongqing_bus_app.utils.BytesUtils;
import com.janev.chongqing_bus_app.utils.L;
import com.janev.chongqing_bus_app.utils.StringUtils;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import tp.xmaihh.serialport.SerialHelper;
import tp.xmaihh.serialport.bean.ComBean;

public class ChongqingV2Handler extends DataHandler{
    private SiteInfo siteInfo;
    private final V2SiteCache v2SiteCache;

    private static final String KEY_SITE_LIST = "KEY_V2_SITE_LIST_2";

    private static final String KEY_SITE_INDEX = "KEY_V2_SITE_INDEX_2";

    public ChongqingV2Handler(SerialHelper serialHelper) {
        super(serialHelper);
        this.v2SiteCache = new V2SiteCache(onSiteDataListener);
    }

    @Override
    protected Constraint initConstraint() {
        Constraint constraint = new Constraint();
        constraint.addStartEnd("2828","0c",true);
        return constraint;
    }

    @Override
    public void onOpen() {
//        queryCarInfo();
        queryLine();
        delayReportError();
    }

    @Override
    public void onClose() {
//        disposeQueryCarInfoRequest();
        disposeQueryLineRequest();
        disposeReportError();
    }

    private CrashReportRequest crashReportRequest;
    private Disposable disposable;
    private void disposeReportError(){
        if(disposable != null && !disposable.isDisposed()){
            disposable.dispose();
            disposable = null;
        }
    }
    private void delayReportError(){
        if(disposable != null && !disposable.isDisposed()){
            disposable.dispose();
            disposable = null;
            if(crashReportRequest != null){
                crashReportRequest = null;
                new CrashReportRequest(CrashReportRequest.SERIAL_ERROR,"心跳丢失",CrashReportRequest.STATUS_FINISH).send();
            }
        }
        disposable = Observable.interval(10,1, TimeUnit.MINUTES)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> {
                    if(crashReportRequest == null){
                        crashReportRequest = new CrashReportRequest(CrashReportRequest.SERIAL_ERROR,"持续10分钟心跳丢失",CrashReportRequest.STATUS_ING);
                    }
                    crashReportRequest.send();
                });
    }

    /*private QueryCarInfoRequest queryCarInfoRequest;
    private void queryCarInfo(){
        if(queryCarInfoRequest == null){
            queryCarInfoRequest = new QueryCarInfoRequest(() -> mSerialHelper);
        }
        queryCarInfoRequest.send();
    }

    private void disposeQueryCarInfoRequest(){
        if(queryCarInfoRequest != null){
            queryCarInfoRequest.stop();
            queryCarInfoRequest = null;
            queryLine();
        }
    }*/

    private QueryLineRequest queryLineRequest;
    private void queryLine(){
        if(queryLineRequest == null){
            queryLineRequest = new QueryLineRequest(() -> mSerialHelper);
        }
        queryLineRequest.send();
    }

    private void disposeQueryLineRequest(){
        if(queryLineRequest != null){
            queryLineRequest.stop();
            queryLineRequest = null;
        }
    }

    private void reply(byte MESSAGE_ID){
        new ReplyRequest(MESSAGE_ID, ReplyRequest.SUCCESS, () -> mSerialHelper).send();
    }

    @Override
    protected void loadCache() {
        if(siteInfo == null){
            siteInfo = new SiteInfo();
        }
        loadCacheDriverInfo();
        loadCacheSiteList();
        loadCacheSiteIndex();
    }

    private void loadCacheDriverInfo(){
        UiMessageUtils.getInstance().send(UiEvent.EVENT_LINE_STAR,Cache.getInt(Cache.Key.LINE_STAR));
        UiMessageUtils.getInstance().send(UiEvent.EVENT_WORKER_ID,Cache.getString(Cache.Key.WORKER_ID));
        UiMessageUtils.getInstance().send(UiEvent.EVENT_POLITIC,Cache.getBoolean(Cache.Key.POLITIC));
    }

    private void loadCacheSiteList(){
        try {
            siteInfo = Cache.getObj(Cache.Key.LINE_INFO, SiteInfo.class);
            if(siteInfo == null){
                siteInfo = new SiteInfo();
            }

            d("loadCacheSiteList: " + siteInfo.getLineName() + " --- " + siteInfo.getUpDown() + " --- " + siteInfo.getSiteMap().size());

            /*String string = Cache.getString(KEY_SITE_LIST);
            if(!TextUtils.isEmpty(string)){
                JSONObject jsonObject = JSONObject.parseObject(string);
                byte upDown = jsonObject.getByteValue("UP_DOWN");
                String lineName = jsonObject.getString("LINE_NAME");
                JSONArray siteArray = jsonObject.getJSONArray("SITE_LIST");
                LinkedHashMap<Integer,String> siteMap = new LinkedHashMap<>();
                for (int i = 0; i < siteArray.size(); i++) {
                    JSONObject site = siteArray.getJSONObject(i);
                    int index = site.getIntValue("index");
                    String name = site.getString("name");
                    siteMap.put(index,name);
                }

                siteInfo.setUpDown(upDown);
                siteInfo.setLineName(lineName);
                siteInfo.addAll(siteMap);

                if(!TextUtils.isEmpty(siteInfo.getLineName())){
                    UiMessageUtils.getInstance().send(UiEvent.EVENT_LINE_NAME,siteInfo.getLineName());
                }
                UiMessageUtils.getInstance().send(UiEvent.EVENT_SITE_LIST,new ChongqingV1Handler.SiteList(siteInfo.getList()));
            }*/
            if(!TextUtils.isEmpty(siteInfo.getLineName())){
                UiMessageUtils.getInstance().send(UiEvent.EVENT_LINE_NAME,siteInfo.getLineName());
            }
            UiMessageUtils.getInstance().send(UiEvent.EVENT_SITE_LIST,new ChongqingV1Handler.SiteList(siteInfo.getList()));
        } catch (Exception e){
            Log.e(TAG, "loadCacheSiteList: ", e);
            e( "loadCacheSiteList: " + e.getMessage());
        }
    }

    private void loadCacheSiteIndex(){
        String string = Cache.getString(KEY_SITE_INDEX);
        if(!TextUtils.isEmpty(string)){
            String[] s = string.split("_");
            if(s.length == 3){
                int upDown = Integer.parseInt(s[0]);
                int inOut = Integer.parseInt(s[1]);
                int index = Integer.parseInt(s[2]);
                d("loadCacheSiteIndex: " + upDown + " --- " + inOut + " --- " + index);

                if(upDown != siteInfo.getUpDown()){
                    return;
                }

                int siteIndex = index;
                //下行计算站号
                if(upDown == 0x01){
                    Map.Entry<Integer, String> first = siteInfo.getFirst();
                    if(first != null){
                        int i = first.getKey();
                        siteIndex = ((siteIndex + 1) - i) + 1;
                    }
                    d("下行站点号：" + siteIndex);
                }
                UiMessageUtils.getInstance().send(UiEvent.EVENT_BROAD_SITE,new int[]{upDown,inOut,siteIndex});
            }
        }
    }

    private void setDriverInfo(int lineStar, String workerId, byte POLITIC){
        Cache.setInt(Cache.Key.LINE_STAR,lineStar);
        Cache.setString(Cache.Key.WORKER_ID,workerId);
        Cache.setBoolean(Cache.Key.POLITIC,POLITIC == 0x01);
        UiMessageUtils.getInstance().send(UiEvent.EVENT_LINE_STAR,lineStar);
        UiMessageUtils.getInstance().send(UiEvent.EVENT_WORKER_ID,workerId);
        UiMessageUtils.getInstance().send(UiEvent.EVENT_POLITIC,POLITIC == 0x01);
    }

    private final V2SiteCache.OnSiteDataListener onSiteDataListener = new V2SiteCache.OnSiteDataListener() {
        @Override
        public void onChanged(String lineName, byte upDown, LinkedHashMap<Integer, String> siteMap) {
            try {
                siteInfo.setLineName(lineName);
                siteInfo.setUpDown(upDown);
                siteInfo.addAll(siteMap);

                d("setSiteList: " + siteInfo.getLineName() + " --- " + siteInfo.getUpDown() + " --- " + siteInfo.getList().size());

                for (Map.Entry<Integer, String> integerStringEntry : siteInfo.getSiteMap().entrySet()) {
                    Integer index = integerStringEntry.getKey();
                    String value = integerStringEntry.getValue();
                    String name = value;
                    boolean responsive = false;
                    if(value.contains("_")){
                        String[] s = value.split("_");
                        name = s[0];
                        responsive = Boolean.parseBoolean(s[1]);
                    }

                    Site site = new Site();
                    site.setIndex(index);
                    site.setLineName(siteInfo.getLineName());
                    site.setCount(siteInfo.size());
                    site.setDirection(siteInfo.getUpDown());
                    site.setName(name);
                    site.setEnName("");
                    site.setResponsive(responsive);
                    DaoManager.get().addOrUpdate(site);
                }

                Cache.setObj(Cache.Key.LINE_INFO,siteInfo);

                e( "addToList: 发送列表：" + siteInfo.getList().toString());
                UiMessageUtils.getInstance().send(UiEvent.EVENT_SITE_LIST,new ChongqingV1Handler.SiteList(siteInfo.getList()));
                UiMessageUtils.getInstance().send(UiEvent.EVENT_LINE_NAME,siteInfo.getLineName());
            } catch (Exception e){
                Log.e(TAG,"setSiteList: " ,e);
                e( "setSiteList: " + e.getMessage());
            }
        }
    };

    private void setSiteIndex(int upDown, int inOut, int index){
        d("setSiteIndex: " + inOut + " --- " + upDown + " --- " + index);
        Cache.setString(KEY_SITE_INDEX,upDown + "_" + inOut + "_" + index);

        if(siteInfo.isEmpty() || upDown != siteInfo.getUpDown()){
            return;
        }

        int siteIndex = index;
        //下行计算站号
        if(upDown == 0x01){
            Map.Entry<Integer, String> first = siteInfo.getFirst();
            if(first != null){
                int i = first.getKey();
                siteIndex = ((siteIndex + 1) - i) + 1;
            }
            d("下行站点号：" + siteIndex);
        }
        UiMessageUtils.getInstance().send(UiEvent.EVENT_BROAD_SITE,new int[]{upDown,inOut,siteIndex});
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
            Log.e(TAG,"setSyncTime" , e);
        }
    }

    @Override
    protected void handle(SerialHelper serialHelper, byte[] bytes) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        byteBuffer.position(0);

        try {
            //头部标识
            byte[] startBytes = new byte[2];
            byteBuffer.get(startBytes);

            //消息ID
            byte messageId = byteBuffer.get();

            //设备地址
            byte deviceAddress = byteBuffer.get();

            //长度
            byte[] lengthBytes = new byte[2];
            byteBuffer.get(lengthBytes);
            int length = BytesUtils.hex16to10(BytesUtils.bytesToHex(lengthBytes));

            byte[] contentBytes = new byte[length];
            byteBuffer.get(contentBytes);

            if(messageId == (byte)0x09){
                e( "handle: 心跳");
                delayReportError();
                return;
            }

            ByteBuffer dataBuffer = ByteBuffer.wrap(contentBytes);
            dataBuffer.position(0);

            if(deviceAddress == (byte)0xff){
                switch (messageId) {
                    case (byte)0x01://车辆信息
                        resolveCarInfo(dataBuffer);
                        reply(messageId);
                        break;
                    case (byte)0x04://进出站
                        resolveInOut(dataBuffer);
                        reply(messageId);
                        break;
                    case (byte)0x06://同步时间
                        resolveSyncTime(dataBuffer);
                        break;
                }
            } else if(deviceAddress == (byte)0x05){
                switch (messageId) {
                    case (byte)0x02://站点列表线路号
                        disposeQueryLineRequest();
                        resolveSiteList(dataBuffer);
                        reply(messageId);
                        break;
                    case (byte)0x08://线路星级
                        resolveDriverInfo(dataBuffer);//线路星级
                        reply(messageId);
                        break;
                }
            }
        } catch (Exception e){
            Log.e(TAG,"handle" ,e);
            e(e);
        }
    }

    private void resolveCarInfo(ByteBuffer byteBuffer){
        int carLicenseLength = BytesUtils.hex16to10(BytesUtils.byteToHex(byteBuffer.get()));
        d("车牌号长度：" + carLicenseLength);
        byte[] carLicenseBytes = new byte[carLicenseLength];
        byteBuffer.get(carLicenseBytes);
        String carLicenseHex = BytesUtils.bytesToHex(carLicenseBytes);
        d("车牌号HEX：" + carLicenseHex);
        String carLicenseNumber = StringUtils.hexStringToString(carLicenseHex,"UTF-8");
        d("车牌号：" + carLicenseNumber);
        MessageUtils.setCarLicenseNumber(carLicenseNumber);

        //车辆编号
        int carNumberLength = BytesUtils.hex16to10(BytesUtils.byteToHex(byteBuffer.get()));
        d("车辆编号长度：" + carNumberLength);
        byte[] carNumberBytes = new byte[carNumberLength];
        byteBuffer.get(carNumberBytes);
        String carNumberHex = BytesUtils.bytesToHex(carNumberBytes);
        d("车辆编号HEX：" + carNumberHex);
        String carNumber = StringUtils.hexStringToString(carNumberHex);
        d("车辆编号：" + carNumber);
        MessageUtils.setCarNumber(carNumber);

        //终端号
        byte[] terminalBytes = new byte[6];
        byteBuffer.get(terminalBytes);
        String terminalHex = BytesUtils.bytesToHex(terminalBytes);
        d("终端号HEX：" + terminalHex);
        terminalHex = MessageUtils.addZero(terminalHex,12);
        d("终端号：" + terminalHex);
        if(!TextUtils.equals(MessageUtils.getTerminalNumber(),terminalHex)){
            MessageUtils.setTerminalNumber(terminalHex);
            UiMessageUtils.getInstance().send(UiEvent.EVENT_CONNECT_TCP);
        }
    }

    private void resolveInOut(ByteBuffer byteBuffer){
        //进出站
        byte upDown = byteBuffer.get();
        //站序
        int index = BytesUtils.hex16to10(BytesUtils.byteToHex(byteBuffer.get()));
        //进出标识
        byte inOut = byteBuffer.get();
        d("上下行：" + upDown + "，站序：" + index + "，进出标识：" + inOut);

        setSiteIndex(upDown,inOut,index);
    }

    private void resolveSyncTime(ByteBuffer byteBuffer){
        byte[] bytes = new byte[7];
        byteBuffer.get(bytes);

        String s = BytesUtils.bytesToHex(bytes);
        d("同步时间：" + s);
        setSyncTime(s);
    }

    private void resolveDriverInfo(ByteBuffer byteBuffer){
        int lineNameLength = BytesUtils.hex16to10(BytesUtils.byteToHex(byteBuffer.get()));
        d("线路号长度：" + lineNameLength);
        byte[] lineNameBytes = new byte[lineNameLength];
        byteBuffer.get(lineNameBytes);
        String lineName = StringUtils.hexStringToString(BytesUtils.bytesToHex(lineNameBytes));
        d("线路号：" + lineName);

        int starNumber = BytesUtils.hex16to10(BytesUtils.byteToHex(byteBuffer.get()));
        d("线路星级：" + starNumber);

        int driverNameLength = BytesUtils.hex16to10(BytesUtils.byteToHex(byteBuffer.get()));
        d("驾驶员姓名长度：" + driverNameLength);
        byte[] driverNameBytes = new byte[driverNameLength];
        byteBuffer.get(driverNameBytes);
        String driverName = StringUtils.hexStringToString(BytesUtils.bytesToHex(driverNameBytes));
        d("驾驶员姓名：" + driverName);

        int driverIdLength = BytesUtils.hex16to10(BytesUtils.byteToHex(byteBuffer.get()));
        d("驾驶员工号长度：" + driverIdLength);
        byte[] driverIdBytes = new byte[driverIdLength];
        byteBuffer.get(driverIdBytes);
        String driverId = StringUtils.hexStringToString(BytesUtils.bytesToHex(driverIdBytes));
        d("驾驶员工号：" + driverId);

        byte politic = byteBuffer.get();
        d("驾驶员政治面貌：" + politic);

        int unitNameLength = BytesUtils.hex16to10(BytesUtils.byteToHex(byteBuffer.get()));
        d("单位名称长度：" + unitNameLength);
        byte[] unitNameBytes = new byte[driverIdLength];
        byteBuffer.get(unitNameBytes);
        String unitName = StringUtils.hexStringToString(BytesUtils.bytesToHex(unitNameBytes));
        d("单位名称：" + unitName);

        setDriverInfo(starNumber,driverId,politic);
    }

    // 2828     起始位
    // 02       消息ID
    // 06       目标地址
    // 0017     长度
    // 03       线路号长度
    // 383138   线路号
    // 00       运行方向
    // 25       站点个数
    // 02       分包总数
    // 02       本包序号
    // d0a300250008bdf5ccecbfb5b6bc00
    // 32       校验位
    // 0c       结束位
    private void resolveSiteList(ByteBuffer byteBuffer){
        v2SiteCache.put(byteBuffer);
    }

    public void test1(){
//        File file = new File(Environment.getExternalStorageDirectory(),"20240102");
//        if(FileUtils.isFileExists(file)){
//            List<String> strings = FileIOUtils.readFile2List(file);
//            for (String string : strings) {
//                if (!string.contains("<---")) {
//                    continue;
//                }
//                Log.d(TAG, "test1: " + string);
//                String[] split = string.split("<---");
//                String hex = split[1];
//                byte[] bytes = BytesUtils.hexToByteArray(hex);
//
//                ComBean comBean = new ComBean("",bytes,bytes.length);
//                check(comBean);
//            }
//        }

        File file = new File(Environment.getExternalStorageDirectory(),"240105.txt");
        if(FileUtils.isFileExists(file)){
            List<String> strings = FileIOUtils.readFile2List(file);
            for (String string : strings) {

                String hex;
                if(string.contains("] ")){
                    String[] split = string.split("] ");
                    hex = split[1];
                } else {
                    hex = string;
                }
                hex = hex.replaceAll(" ", "");
                Log.d(TAG, "test1: " + hex);
                byte[] bytes = BytesUtils.hexToByteArray(hex);
                ComBean comBean = new ComBean("",bytes,bytes.length);
                check(comBean);
            }
        }
    }

    public void test2(){
        List<String> dataString = new ArrayList<>();
        File file = new File(Environment.getExternalStorageDirectory(),"Chongqing1.txt");
        List<String> strings = FileIOUtils.readFile2List(file);
        for (String string : strings) {
            dataString.add(string.replaceAll(" ",""));
        }
        for (String s : dataString) {
            Log.d(TAG, "test2: " + s);
            byte[] bytes = BytesUtils.hexToByteArray(s);
            ComBean comBean = new ComBean("",bytes,bytes.length);
            check(comBean);
        }
    }

    List<String> dataString = new ArrayList<>();
    public void test3() {
        if (dataString.isEmpty()) {
            new Thread(() -> {

//                File file = new File(Environment.getExternalStorageDirectory(), "finaltest.txt");
//                List<String> strings = FileIOUtils.readFile2List(file);
//                for (String string : strings) {
//                    int i = string.indexOf(" ");
//                    if(i == -1){
//                        continue;
//                    }
//                    String substring = string.substring(i).replaceAll(" ","");
//                    Log.d(TAG, "test3: " + substring);
//                    dataString.add(substring);
//                }

//                File file = new File(Environment.getExternalStorageDirectory(), "20240219");
//                List<String> strings = FileIOUtils.readFile2List(file);
//                for (String string : strings) {
//                    if (string.contains("<---")) {
//                        String[] split = string.split("<---");
//                        dataString.add(split[1]);
//                    } else {
//                        continue;
//                    }
//                }
                Log.d(TAG, "test3: 填充完毕");
            }).start();
        } else {
            Iterator<String> iterator = dataString.iterator();
            while (iterator.hasNext()) {
                String next = iterator.next();
                Log.d(TAG, "test3: " + next);
                byte[] bytes = BytesUtils.hexToByteArray(next);
                ComBean comBean = new ComBean("", bytes, bytes.length);
                check(comBean);
                iterator.remove();
//                if(next.endsWith("0c")){
//                    break;
//                }
            }
        }
    }
}