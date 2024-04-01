package com.yunbiao.publicity_guideboard.ui;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ServiceUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.UiMessageUtils;
import com.yunbiao.publicity_guideboard.App;
import com.yunbiao.publicity_guideboard.BuildConfig;
import com.yunbiao.publicity_guideboard.R;
import com.yunbiao.publicity_guideboard.alive.KeepAlive;
import com.yunbiao.publicity_guideboard.databinding.ActivityMainBinding;
import com.yunbiao.publicity_guideboard.db.DaoManager;
import com.yunbiao.publicity_guideboard.serial.HYDataHandler;
import com.yunbiao.publicity_guideboard.serial.SerialPortManager;
import com.yunbiao.publicity_guideboard.serial.TMDataHandler;
import com.yunbiao.publicity_guideboard.system.Cache;
import com.yunbiao.publicity_guideboard.system.Path;
import com.yunbiao.publicity_guideboard.utils.PublicityManager;
import com.yunbiao.publicity_guideboard.utils.SmdtUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class MainActivity extends BaseActivity<ActivityMainBinding> {
    private static final String TAG = "MainActivity";

    private SettingFragment settingFragment;
    private DownloadInfoFragment downloadInfoFragment;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    public static final int CORRECT_TIME = 111;
    @Override
    protected void initView() {
        UiMessageUtils.getInstance().addListener(CORRECT_TIME,uiMessageCallback);

        //初始化路径
        Path.init();

        //初始化缓存
        Cache.init();

        //初始化数据库
        DaoManager.get().initDB(App.getContext(),Path.getDatabasePath());

        //初始化日志配置
        LogUtils.getConfig().setLog2FileSwitch(Cache.getBoolean(Cache.Key.DEBUG));
        LogUtils.getConfig().setDir(Path.getLogPath());
        Log.d(TAG, "initView: 是否开启日志收集：" + LogUtils.getConfig().isLog2FileSwitch());

        //启动保活服务
        if(!ServiceUtils.isServiceRunning(KeepAlive.class)){
            ServiceUtils.startService(KeepAlive.class);
        }

        //加载司机信息视图
        replace(R.id.fl_driver_info,new DriverInfoFragment());
        //加载宣传信息视图
        replace(R.id.fl_publicity,new PublicityFragment());
        //加载报站视图
        replace(R.id.fl_guide_board,new GuideBoardFragment());

        //如果用户名密码或车号为空则显示设置弹窗
        String userName = Cache.getString(Cache.Key.USER_NAME,Cache.Default.USER_NAME);
        String password = Cache.getString(Cache.Key.PASSWORD,Cache.Default.PASSWORD);
        String busCode = Cache.getString(Cache.Key.BUS_CODE);
        if(TextUtils.isEmpty(userName) || TextUtils.isEmpty(password) || TextUtils.isEmpty(busCode)){
            showSetting();
        }

        binding.flRightTop.setOnLongClickListener(v -> {
            showSetting();
            return true;
        });

        binding.flRightBottom.setOnLongClickListener(v -> {
            showDownloadInfo();
            return true;
        });

        debug();
    }

    private void debug(){
        if(BuildConfig.DEBUG){
            HYDataHandler hyDataHandler = new HYDataHandler();
            binding.btnList.setVisibility(View.VISIBLE);
            binding.btnList.setOnClickListener(v -> hyDataHandler.testList());

            binding.btnInfo.setVisibility(View.VISIBLE);
            binding.btnInfo.setOnClickListener(v -> hyDataHandler.testInfo());

            binding.btnNext.setVisibility(View.VISIBLE);
            binding.btnNext.setOnClickListener(v -> hyDataHandler.testInOut());
        }
    }

    private final UiMessageUtils.UiMessageCallback uiMessageCallback = localMessage -> {
//        int[] timeArray = (int[]) localMessage.getObject();
//
//        int seconds = timeArray[timeArray.length - 1];
//        Log.d(TAG, "秒数: " + seconds);
//
//        int delay = 60 - seconds - 5;
//        Log.d(TAG, "延迟重启: " + delay);
//
//        long lastTime = System.currentTimeMillis();
//
//        ThreadUtils.executeByCachedWithDelay(new ThreadUtils.SimpleTask<Integer>() {
//            @Override
//            public Integer doInBackground() throws Throwable {
//                Log.e(TAG, "doInBackground: 设置时间");
//                return SmdtUtils.getInstance().setTime(timeArray[0], timeArray[1], timeArray[2], timeArray[3], timeArray[4]);
//            }
//
//            @Override
//            public void onSuccess(Integer result) {
//                Log.d(TAG, "onSuccess: " + System.currentTimeMillis() + " --- " + lastTime);
//                //判断设置后时间比设置前时间超过30秒就重启
//                if (Math.abs(System.currentTimeMillis() - lastTime) > 30 * 1000) {
//                    Log.d(TAG, "onSuccess: 设置时间重启");
//                    AppUtils.relaunchApp(true);
//                }
//            }
//        },delay,TimeUnit.SECONDS);
    };

    public void showDownloadInfo(){
        if(downloadInfoFragment == null || !downloadInfoFragment.isAdded()){
            downloadInfoFragment = new DownloadInfoFragment();
            add(R.id.fl_download_info,downloadInfoFragment);
        }
    }

    public void removeDownloadInfo(){
        if(downloadInfoFragment != null && downloadInfoFragment.isAdded()){
            remove(downloadInfoFragment);
        }
    }

    public void showSetting(){
        if(settingFragment == null || !settingFragment.isAdded()){
            settingFragment = new SettingFragment();
            add(R.id.fl_setting,settingFragment);
        }
    }

    public void removeSetting(){
        if(settingFragment != null && settingFragment.isAdded()){
            remove(settingFragment);
        }
    }

    @Override
    protected void initData() {
        SerialPortManager.bindService();
    }

    private void replace(int containerId, Fragment fragment){
        getSupportFragmentManager().beginTransaction().replace(containerId,fragment).commit();
    }

    private void add(int containerId, Fragment fragment){
        getSupportFragmentManager().beginTransaction().add(containerId,fragment).show(fragment).commit();
    }

    private void remove(Fragment fragment){
        getSupportFragmentManager().beginTransaction().remove(fragment).commit();
    }

    @Override
    protected void onDestroy() {
        UiMessageUtils.getInstance().removeListener(CORRECT_TIME,uiMessageCallback);
        SerialPortManager.unBindService();
        super.onDestroy();
    }

    private long mExitTime = 0;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            //如果设置打开则先关闭设置
            if(downloadInfoFragment != null && downloadInfoFragment.isAdded()){
                removeDownloadInfo();
            }
            else if(settingFragment != null && settingFragment.isAdded()){
                removeSetting();
            }
            else if (System.currentTimeMillis() - mExitTime > 2000) { //大于2000ms则认为是误操作，使用Toast进行提示
                Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                //并记录下本次点击“返回键”的时刻，以便下次进行判断
                mExitTime = System.currentTimeMillis();
            }
            else {
                if(ServiceUtils.isServiceRunning(KeepAlive.class)){
                    ServiceUtils.stopService(KeepAlive.class);
                }
                AppUtils.exitApp();
            }
            return true;
        }
        else if(keyCode == KeyEvent.KEYCODE_MENU){
            showSetting();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}