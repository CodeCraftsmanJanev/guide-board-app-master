package com.yunbiao.guideboard.ui;

import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.UiMessageUtils;
import com.yunbiao.guideboard.system.Cache;
import com.yunbiao.guideboard.system.Constants;
import com.yunbiao.guideboard.R;
import com.yunbiao.guideboard.databinding.FragmentMenuBinding;

import java.io.File;

public class MenuFragment extends BaseFragment<FragmentMenuBinding>{
    @Override
    protected int getLayoutId() {
        return R.layout.fragment_menu;
    }

    @Override
    protected void initView() {
        int price = Cache.getInt(Cache.Key.PRICE_POSITION, Cache.Default.PRICE);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(requireActivity(),R.layout.myspiner, Constants.PRICE_ARRAY);
        binding.spSerport.setAdapter(arrayAdapter);
        binding.spSerport.setSelection(price);
        binding.spSerport.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Cache.setInt(Cache.Key.PRICE_POSITION,position);
                UiMessageUtils.getInstance().send(Constants.PRICE);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        boolean aBoolean = Cache.getBoolean(Cache.Key.DEBUG);
        binding.swDebug.setChecked(aBoolean);
        binding.swDebug.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Cache.setBoolean(Cache.Key.DEBUG,isChecked);
            LogUtils.getConfig().setLog2FileSwitch(isChecked);
            LogUtils.getConfig().setDir(new File(Environment.getExternalStorageDirectory(), "bus_log"));
        });
    }

    @Override
    protected void initData() {
    }
}
