package com.yunbiao.guideboard.ui;


import android.content.Context;
import android.graphics.Color;
import android.os.Environment;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;

import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.FragmentUtils;
import com.blankj.utilcode.util.ServiceUtils;
import com.blankj.utilcode.util.ShellUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.blankj.utilcode.util.UiMessageUtils;
import com.blankj.utilcode.util.Utils;
import com.yunbiao.guideboard.BuildConfig;
import com.yunbiao.guideboard.databinding.ActivityMainBinding;
import com.yunbiao.guideboard.serial.TDDataHandler;
import com.yunbiao.guideboard.system.Cache;
import com.yunbiao.guideboard.system.Constants;
import com.yunbiao.guideboard.system.Path;
import com.yunbiao.guideboard.R;
import com.yunbiao.guideboard.serial.SerialPortManager;
import com.yunbiao.guideboard.alive.KeepAlive;

import java.util.Arrays;

public class MainActivity extends BaseActivity<ActivityMainBinding> {
    private static final String TAG = "MainActivity";

    private static final String TIP = "请前门上  后门下  票价%1$s元";

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void initView() {
        Path.init();

        Cache.init();

        if(!ServiceUtils.isServiceRunning(KeepAlive.class)){
            ServiceUtils.startService(KeepAlive.class);
        }

        UiMessageUtils.getInstance().addListener(Constants.LINE_NUMBER,lineNumberCallback);
        UiMessageUtils.getInstance().addListener(Constants.SITE_LIST,siteListCallback);
        UiMessageUtils.getInstance().addListener(Constants.PRICE,priceCallback);

        binding.flClick.setOnClickListener(v -> {
            if(menuFragment != null && menuFragment.isAdded()){
                FragmentUtils.remove(menuFragment);
            } else {
                menuFragment = new MenuFragment();
                FragmentUtils.add(getSupportFragmentManager(),menuFragment,R.id.fl_menu_container);
            }
        });

        test();
    }

    private void test(){
        if(!BuildConfig.DEBUG){
            return;
        }
        binding.btnTest.setVisibility(View.VISIBLE);

        TDDataHandler handler = new TDDataHandler();
        binding.btnTest.setOnClickListener(v -> {
            handler.test();
        });

        binding.btnTest1.setVisibility(View.VISIBLE);
        binding.btnTest1.setSelected(false);
        binding.btnTest1.setText("开启日志收集");
        binding.btnTest1.setOnClickListener(v -> {
            binding.btnTest1.setSelected(!binding.btnTest1.isSelected());
            if(binding.btnTest1.isSelected()){
                binding.btnTest1.setText("关闭日志收集");
                Utils.Task<ShellUtils.CommandResult> commandResultTask = ShellUtils.execCmdAsync("logcat > /sdcard/log.txt", false, new Utils.Consumer<ShellUtils.CommandResult>() {
                    @Override
                    public void accept(ShellUtils.CommandResult commandResult) {
                        Log.e(TAG, "22222 test: " + commandResult.toString());
                        ToastUtils.make().setTextSize(50).setTextColor(Color.WHITE).setBgColor(Color.BLACK).setDurationIsLong(true).show("开启日志收集：" + commandResult.toString());
                    }
                });
                Log.d(TAG, "11111 test: " + commandResultTask.toString());
            } else {
                binding.btnTest1.setText("开启日志收集");
                Utils.Task<ShellUtils.CommandResult> commandResultTask = ShellUtils.execCmdAsync("killall -SIGINT logcat", false, new Utils.Consumer<ShellUtils.CommandResult>() {
                    @Override
                    public void accept(ShellUtils.CommandResult commandResult) {
                        Log.e(TAG, "44444 test: " + commandResult.toString());
                        ToastUtils.make().setTextSize(50).setTextColor(Color.WHITE).setBgColor(Color.BLACK).setDurationIsLong(true).show("关闭日志收集：" + commandResult.toString());
                    }
                });
                Log.d(TAG, "33333 test: " + commandResultTask.toString());
            }
        });

    }

    private final UiMessageUtils.UiMessageCallback lineNumberCallback = localMessage -> {
        String lineNumber = Cache.getString(Cache.Key.LINE_NUMBER);
        Log.d(TAG, "收到消息：" + localMessage.getId() + " --- " + lineNumber);
        binding.tvLineName.setText(lineNumber + "路");
    };

    private String mSiteList;
    private String mEndSite;
    private final UiMessageUtils.UiMessageCallback siteListCallback = localMessage -> {
        String siteList = Cache.getString(Cache.Key.SITE_LIST);
        Log.d(TAG, "收到消息：" + localMessage.getId() + " --- " + siteList);
        if(TextUtils.isEmpty(mSiteList) || !TextUtils.equals(mSiteList,siteList)){
            this.mSiteList = siteList;
            binding.tvSiteList.setContent(this.mSiteList);
        }

        String endSite = Cache.getString(Cache.Key.END_SITE);
        Log.d(TAG, "收到消息：" + localMessage.getId() + " --- " + endSite);
        if(TextUtils.isEmpty(mEndSite) || !TextUtils.equals(mEndSite,endSite)){
            this.mEndSite = endSite;
            binding.tvEndSite.post(() -> {
                TextPaint paint = binding.tvEndSite.getPaint();
                float v = paint.measureText(this.mEndSite);
                Log.d(TAG, "文字宽度: " + v + " ----- " + binding.tvEndSite.getWidth());
                //如果文字宽度大于非滚动文字则设置为滚动文字
                if(v > binding.tvEndSite.getWidth()){
                    binding.tvEndSite.setVisibility(View.INVISIBLE);
                    binding.mvEndSite.setVisibility(View.VISIBLE);
                    binding.mvEndSite.setContent(Arrays.asList(this.mEndSite,this.mEndSite));
                } else {
                    binding.mvEndSite.setVisibility(View.GONE);
                    binding.tvEndSite.setVisibility(View.VISIBLE);
                    binding.tvEndSite.setText(this.mEndSite);
                }
            });
        }
    };

    private final UiMessageUtils.UiMessageCallback priceCallback = localMessage -> {
        int pricePosition = Cache.getInt(Cache.Key.PRICE_POSITION, Cache.Default.PRICE);
        String price = Constants.PRICE_ARRAY[pricePosition];
        Log.d(TAG, "收到消息：" + localMessage.getId() + " --- " + price);
        binding.tvTips.setText(String.format(TIP,price));
    };

    @Override
    protected void initData() {
        SerialPortManager.bindService();
    }

    private long mExitTime = 0;
    private MenuFragment menuFragment;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            //如果设置打开则先关闭设置
            if(menuFragment != null && menuFragment.isAdded()){
                FragmentUtils.remove(menuFragment);
                return true;
            }
            if (System.currentTimeMillis() - mExitTime > 2000) { //大于2000ms则认为是误操作，使用Toast进行提示
                Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                //并记录下本次点击“返回键”的时刻，以便下次进行判断
                mExitTime = System.currentTimeMillis();
            } else {
                if(ServiceUtils.isServiceRunning(KeepAlive.class)){
                    ServiceUtils.stopService(KeepAlive.class);
                }
                AppUtils.exitApp();
            }
            return true;
        } else if(keyCode == KeyEvent.KEYCODE_MENU){
            if(menuFragment != null && menuFragment.isAdded()){
                FragmentUtils.remove(menuFragment);
            } else {
                menuFragment = new MenuFragment();
                FragmentUtils.add(getSupportFragmentManager(),menuFragment,R.id.fl_menu_container);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        SerialPortManager.unBindService();
        UiMessageUtils.getInstance().removeListeners(Constants.LINE_NUMBER);
        UiMessageUtils.getInstance().removeListeners(Constants.SITE_LIST);
        UiMessageUtils.getInstance().removeListeners(Constants.PRICE);
        super.onDestroy();
    }
}