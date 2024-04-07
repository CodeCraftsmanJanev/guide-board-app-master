package com.janev.chongqing_bus_app.ui;

import android.content.Context;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSONObject;
import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.FragmentUtils;
import com.blankj.utilcode.util.ServiceUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.blankj.utilcode.util.UiMessageUtils;
import com.janev.chongqing_bus_app.BuildConfig;
import com.janev.chongqing_bus_app.R;
import com.janev.chongqing_bus_app.alive.KeepAlive;
import com.janev.chongqing_bus_app.databinding.ActivityMainBinding;
import com.janev.chongqing_bus_app.serial.ChongqingV1Handler;
import com.janev.chongqing_bus_app.serial.ChongqingV2Handler;
import com.janev.chongqing_bus_app.serial.SerialPortManager;
import com.janev.chongqing_bus_app.serial.ZhuFu;
import com.janev.chongqing_bus_app.system.UiEvent;
import com.janev.chongqing_bus_app.tcp.TCPLog;
import com.janev.chongqing_bus_app.tcp.TCPManager;
import com.janev.chongqing_bus_app.tcp.message.CrashReportRequest;
import com.janev.chongqing_bus_app.utils.L;
import com.janev.chongqing_bus_app.utils.SmdtUtils;
import com.janev.chongqing_bus_app.utils.VolumeManager;

import java.io.File;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends BaseActivity<ActivityMainBinding> implements UiMessageUtils.UiMessageCallback {

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void initView() {
        UiMessageUtils.getInstance().addListener(this);

        //启动保活服务
        if(!ServiceUtils.isServiceRunning(KeepAlive.class)){
            ServiceUtils.startService(KeepAlive.class);
        }

        binding.flClickConfig.setOnLongClickListener(v -> {
            Log.e(TAG, "initView: 点击了");
            showConfigFragment();
            return true;
        });

        //设置缓存音量
        VolumeManager.setCacheVolume();

        //加载标题栏
        replace(R.id.fl_title,new TitleFragment());
        //加载线路视图
        replace(R.id.fl_line,new LineFragment());
        //加载站点视图
        replace(R.id.fl_site,new SiteFragment());
        //加载素材播放
        replace(R.id.fl_resource,new MaterialFragment());

        //启动串口服务
        SerialPortManager.bindService();

        //设置主副屏 zhang
        ZhuFu.initSerial(this);

        //初始化日志服务
        TCPLog.getInstance().init();

        initClient();

        debug();
    }

    private void initClient(){
        Calendar calendar = Calendar.getInstance();
        calendar.set(1970, Calendar.JANUARY, 1);

        Date dateToCheck = new Date();
        boolean is1970 = dateToCheck.equals(calendar.getTime());
        if(is1970){
            L.tcpE(TAG,"时间未同步");
            ToastUtils.make().setBgColor(Color.BLACK).setTextColor(Color.RED).setTextSize(50).setGravity(Gravity.CENTER,0,0).setDurationIsLong(true).show("等待同步时间");
        } else {
            //启动通讯服务
            TCPManager.getInstance().openClient();
        }
    }

    private void debug(){
        if(!BuildConfig.DEBUG){
            return;
        }

//        ChongqingV1Handler handler = new ChongqingV1Handler();
//        ChongqingV2Handler handler = new ChongqingV2Handler(null);

//        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        binding.btnTest1.setVisibility(View.GONE);
        binding.btnTest1.setOnClickListener(v -> {
//            handler.test3();
//            UiMessageUtils.getInstance().send(1513);
//            handler.test();
//            new CrashReportRequest(CrashReportRequest.SERIAL_ERROR,"出现错误",CrashReportRequest.SERIAL_ERROR).send();
//            handler.test();
//            wifiManager.setWifiEnabled(!wifiManager.isWifiEnabled());

//            Resource.Material material = new Resource.Material();
//            material.setUrl(new File(Environment.getExternalStorageDirectory(),"1.mp4").getPath());
//            UiMessageUtils.getInstance().send(UiEvent.EVENT_ADD_MATERIAL,material);
//            handler.test1();

//            String s = "28 28 85 00 22 01 20 00 00 01 00 00 00 00 00 00 56 20 00 1D 05 44 01 03 47 0D 31 38 33 2E 36 36 2E 36 35 2E 31 35 35 48 02 1F 91 09 01 05 0C 01 08 0F 0C".replaceAll(" ","").trim();
//            String s = "28 28 85 00 22 01 20 00 00 01 00 00 00 00 00 00 3F 10 00 1A 04 44 01 03 47 0D 31 38 33 2E 36 36 2E 36 35 2E 31 35 35 48 02 1F 91 09 01 05 55 0C".replaceAll(" ","").trim();
//            String s = "28 28 86 00 22 01 20 00 00 01 00 00 00 00 00 00 1F AA 00 74 05 00 00 00 00 00 00 00 00 42 03 31 2E 31 53 66 74 70 3A 2F 2F 31 32 31 2E 35 2E 31 31 31 2E 31 35 30 2F 41 64 76 65 72 74 52 65 73 6F 75 72 63 65 50 61 63 6B 61 67 65 2F 37 64 34 34 31 34 35 31 2D 38 37 33 32 2D 34 39 35 30 2D 61 37 31 65 2D 61 33 63 38 38 65 62 36 38 35 36 61 2E 6A 73 6F 6E 07 66 74 70 75 73 65 72 09 6D 6D 5F 31 32 33 34 35 36 57 0C".replaceAll(" ","").trim();
//            String s = "28 28 84 00 22 01 20 00 00 01 00 00 00 00 00 00 43 9D 00 05 04 0C 44 47 48 1E 0C".replaceAll(" ","").trim();

            //应用资源包
//            String s = "282886002201200000020000000000007bb0006b0300000000000000003b03312e314a6674703a2f2f3132312e352e3131312e3135302f66696c657265736f757263652f35393130373536622d363637362d343236662d626461352d3630353563656262393732392e6a736f6e0766747075736572096d6d5f313233343536480c";
            //宣传语
//            List<String> list = new ArrayList<>();
//            for (int i = 11; i < 19; i++) {
////                list.add("测试宣传语" + i);
//                list.add("测测测测测" + i);
//            }
//            String hexString = Integer.toHexString(list.size());
//            while (hexString.length() < 2){
//                hexString = "0" + hexString;
//            }
//            StringBuilder content = new StringBuilder("00" + hexString);
//            for (String s : list) {
//                String s1 = BytesUtils.bytesToHex(s.getBytes(Charset.forName("GBK")));
//                String hex = "02" + MessageUtils.getLength(s1, 2) + s1;
//                content.append(hex);
//            }
//
//            String length = MessageUtils.getLength(content.toString(), 4);

            // 00
            // 08

            // 00
            // 11
            // e6b58be8af95e5aea3e4bca0e8afad3131
            // 00
            // 11
            // e6b58be8af95e5aea3e4bca0e8afad31320011e6b58be8af95e5aea3e4bca0e8afad31330011e6b58be8af95e5aea3e4bca0e8afad31340011e6b58be8af95e5aea3e4bca0e8afad31350011e6b58be8af95e5aea3e4bca0e8afad31360011e6b58be8af95e5aea3e4bca0e8afad31370011e6b58be8af95e5aea3e4bca0e8afad3138

//            String s = "" +
//                    "2828" +
//                    "8E" +
//                    "00" +
//                    "220120000002" +
//                    "000000000000" +
//                    "7bb0" +
//                    length +
//                    content +
//                    "480c".toUpperCase();
//            UiMessageUtils.getInstance().send(1111,s);

            //应用程序
//            String s = "2828860022012000000200000000000022c5006a0200000000000000003f03312e34496674703a2f2f3132312e352e3131312e3135302f66696c657265736f757263652f64666465393833352d613464392d343137332d626665342d3533663366306231366539322e61706b0766747075736572096d6d5f313233343536090c";
//            UiMessageUtils.getInstance().send(1111,s);
        });

        binding.btnTest2.setVisibility(View.GONE);
        binding.btnTest2.setOnClickListener(v -> {
//            UiMessageUtils.getInstance().send(1112);
            UiMessageUtils.getInstance().send(UiEvent.EVENT_RESET_MATERIAL);
            UiMessageUtils.getInstance().send(UiEvent.EVENT_ADD_MATERIAL_FINISH);
//            UiMessageUtils.getInstance().send(UiEvent.EVENT_CONNECT_TCP);
//            handler.test2();
        });
        binding.btnTest3.setVisibility(View.GONE);
        binding.btnTest3.setOnClickListener(v -> {
//            handler.test3();
/*

            Resource.Material material = new Resource.Material();
            material.setUrl("ftp://183.66.65.155:6127/Materials/0af83bea-822c-4783-a239-c45c9b507cbc.mp4");
            material.setId("1234567");
            material.setName("0af83bea-822c-4783-a239-c45c9b507cbc");
            FtpDownloader2 ftpDownloader2 = new FtpDownloader2(material,"user2","ftpuser2",Environment.getExternalStorageDirectory().getPath(),Environment.getExternalStorageDirectory().getPath());
            ftpDownloader2.setOnDownloadListener(new Downloader.OnDownloadListener() {
                @Override
                public void onStart(Resource.Material material) {
                    Log.d(TAG, "onStart: " + material.getUrl());
                }

                @Override
                public void onProgress(Resource.Material material, int percent) {
                    Log.d(TAG, "onProgress: 进度：" + percent);
                }

                @Override
                public void onComplete(Resource.Material material, File file, boolean isDownload) {
                    Log.d(TAG, "onComplete: 下载完成：" + file.getPath());
                }

                @Override
                public void onError(Resource.Material material, Throwable throwable) {
                    Log.d(TAG, "onError: 下载失败：" + throwable.getMessage());
                }
            });
            ftpDownloader2.start();
*/

        });
    }

    @Override
    protected void initData() {

//        int resourceType = Cache.getInt(Cache.Key.RESOURCE_TYPE, Cache.Default.RESOURCE_TYPE);
//        //判断本地还是网络
//        if(resourceType == 0){
//            //加载资源视图
//            replace(R.id.fl_resource,new LocalResourceFragment());
//        } else {
//            //加载资源视图
//            replace(R.id.fl_resource,new MaterialFragment());
//        }
    }

    @Override
    protected void onDestroy() {
        TCPLog.getInstance().clear();
        TCPManager.getInstance().close();
        SerialPortManager.unBindService();
        UiMessageUtils.getInstance().removeListener(this);
        super.onDestroy();
    }

    @Override
    public void handleMessage(@NonNull UiMessageUtils.UiMessage localMessage) {
        if (localMessage.getId() == UiEvent.EVENT_SYNC_TIME) {
            Object object = localMessage.getObject();
            if(object instanceof JSONObject){
                String pattern = "yyyyMMddHHmmss";

                JSONObject jsonObject = (JSONObject) object;
                String timeStr = jsonObject.getString("timeStr");

                Date nowDate = TimeUtils.getNowDate();

                L.serialD(TAG,"检查系统时间：" + TimeUtils.date2String(nowDate, pattern) + " >>>>> " + timeStr);

                Date targetDate = TimeUtils.string2Date(timeStr, pattern);

                if(Math.abs(targetDate.getTime() - nowDate.getTime()) > 10 * 60 * 1000){
                    int[] timeArray = jsonObject.getObject("timeArray", int[].class);
                    L.serialD(TAG,"修改系统时间：" + TimeUtils.date2String(nowDate, pattern) + " >>>>> " + timeStr);

                    ThreadUtils.executeBySingle(new ThreadUtils.SimpleTask<Integer>() {
                        @Override
                        public Integer doInBackground() throws Throwable {
                            return SmdtUtils.getInstance().setTime(timeArray[0], timeArray[1], timeArray[2], timeArray[3], timeArray[4]);
                        }

                        @Override
                        public void onSuccess(Integer result) {
                            L.serialD(TAG,"修改系统时间结果：" + result);
                            if(result == 1){
                                ToastUtils.make().setBgColor(Color.BLACK).setTextColor(Color.GREEN).setTextSize(50).setGravity(Gravity.CENTER,0,0).setDurationIsLong(true).show("时间同步完成");
                                //启动通讯服务
                                TCPManager.getInstance().openClient();
                            } else {
                                ToastUtils.make().setBgColor(Color.BLACK).setTextColor(Color.RED).setTextSize(50).setGravity(Gravity.CENTER,0,0).setDurationIsLong(true).show("时间同步失败");
                            }
                        }

                        @Override
                        public void onFail(Throwable t) {
                            L.serialE(TAG,"修改系统时间异常：" + t.getMessage());
                            ToastUtils.make().setBgColor(Color.BLACK).setTextColor(Color.RED).setTextSize(50).setGravity(Gravity.CENTER,0,0).setDurationIsLong(true).show("时间同步失败");
                        }
                    });
                } else {
                    L.serialD(TAG,"无需修改系统时间");
                }
            }
        }
        else if(localMessage.getId() == UiEvent.EVENT_UPGRADE_APP){
            String filePath = (String) localMessage.getObject();
            SmdtUtils.getInstance().installApp(new File(filePath));
        }
    }

    private long mExitTime = 0;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            if(connectFragment != null && connectFragment.isAdded()){
                remove(connectFragment);
            } else if(moreFragment != null && moreFragment.isAdded()){
                remove(moreFragment);
            } else if(configFragment != null && configFragment.isAdded()){
                remove(configFragment);
            } else if (System.currentTimeMillis() - mExitTime > 2000) { //大于2000ms则认为是误操作，使用Toast进行提示
                Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                //并记录下本次点击“返回键”的时刻，以便下次进行判断
                mExitTime = System.currentTimeMillis();
            } else {
//                if(ServiceUtils.isServiceRunning(KeepAlive.class)){
//                    ServiceUtils.stopService(KeepAlive.class);
//                }
                AppUtils.exitApp();
            }
            return true;
        }
//        else if(keyCode == KeyEvent.KEYCODE_MENU){
//            showConfigFragment();
//        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (isShouldHideInput(v, ev)) {

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
            return super.dispatchTouchEvent(ev);
        }
        // 必不可少，否则所有的组件都不会有TouchEvent了
        if (getWindow().superDispatchTouchEvent(ev)) {
            return true;
        }
        return onTouchEvent(ev);
    }
    public  boolean isShouldHideInput(View v, MotionEvent event) {
        if ((v instanceof EditText)) {
            int[] leftTop = { 0, 0 };
            //获取输入框当前的location位置
            v.getLocationInWindow(leftTop);
            int left = leftTop[0];
            int top = leftTop[1];
            int bottom = top + v.getHeight();
            int right = left + v.getWidth();
            if (event.getX() > left && event.getX() < right
                    && event.getY() > top && event.getY() < bottom) {
                // 点击的是输入框区域，保留点击EditText的事件
                return false;
            } else {
                return true;
            }
        }
        return false;
    }
    private ConfigFragment configFragment;
    private void showConfigFragment(){
        if(configFragment != null && configFragment.isAdded()){
            Log.e(TAG, "showConfigFragment: 移除配置页");
            FragmentUtils.remove(configFragment);
        } else {
            Log.e(TAG, "showConfigFragment: 显示配置页");
            configFragment = ConfigFragment.newInstance();
            configFragment.setOnClickMoreRunnable(this::showMoreFragment);
            configFragment.setOnClickConnectionRunnable(this::showConnectFragment);
            FragmentUtils.add(getSupportFragmentManager(),configFragment,R.id.fl_config_container);
        }
    }

    private ConnectionFragment connectFragment;
    private void showConnectFragment(){
        if(connectFragment != null && connectFragment.isAdded()){
            Log.e(TAG, "showConfigFragment: 移除连接页");
            FragmentUtils.remove(connectFragment);
        } else {
            Log.e(TAG, "showConfigFragment: 显示更多页");
            connectFragment = ConnectionFragment.newInstance();
            connectFragment.setOnClickBackRunnable(() -> onKeyDown(KeyEvent.KEYCODE_BACK,new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_BACK)));
            FragmentUtils.add(getSupportFragmentManager(),connectFragment,R.id.fl_config_container);
        }
    }

    private MoreFragment moreFragment;
    private void showMoreFragment(){
        if(moreFragment != null && moreFragment.isAdded()){
            Log.e(TAG, "showConfigFragment: 移除更多页");
            FragmentUtils.remove(moreFragment);
        } else {
            Log.e(TAG, "showConfigFragment: 显示更多页");
            moreFragment = MoreFragment.newInstance();
            moreFragment.setOnClickBackRunnable(() -> onKeyDown(KeyEvent.KEYCODE_BACK,new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_BACK)));
            FragmentUtils.add(getSupportFragmentManager(),moreFragment,R.id.fl_config_container);
        }
    }
}