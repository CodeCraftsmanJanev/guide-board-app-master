package com.yunbiao.publicity_guideboard.ui;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSONObject;
import com.blankj.utilcode.util.UiMessageUtils;
import com.bumptech.glide.Glide;
import com.yunbiao.publicity_guideboard.R;
import com.yunbiao.publicity_guideboard.databinding.FragmentDriverInfoBinding;
import com.yunbiao.publicity_guideboard.system.Cache;
import com.yunbiao.publicity_guideboard.utils.DriverInfoManager;

public class DriverInfoFragment extends BaseFragment<FragmentDriverInfoBinding>{
    @Override
    protected int getLayoutId() {
        return R.layout.fragment_driver_info;
    }

    @Override
    protected void initView() {

    }

    @Override
    protected void initData() {
        UiMessageUtils.getInstance().addListener(DriverInfoManager.START,startListener);
        UiMessageUtils.getInstance().addListener(DriverInfoManager.SUCCESS,successListener);
        UiMessageUtils.getInstance().addListener(DriverInfoManager.FAILED,failedListener);
        UiMessageUtils.getInstance().addListener(DriverInfoManager.FINISH,finishListener);

        getLifecycle().addObserver(DriverInfoManager.getInstance());
    }

    private final UiMessageUtils.UiMessageCallback startListener = localMessage -> {
        binding.pbLoading.setVisibility(View.VISIBLE);
    };

    private final UiMessageUtils.UiMessageCallback successListener = localMessage -> {
        String driverName = Cache.getString(Cache.Key.DRIVER_NAME);
        String driverCode = Cache.getString(Cache.Key.DRIVER_CODE);
        String driverStar = Cache.getString(Cache.Key.DRIVER_STAR);
        String driverHead = Cache.getString(Cache.Key.DRIVER_HEAD);
        String complainMobile = Cache.getString(Cache.Key.COMPLAIN_MOBILE);
        if(!TextUtils.isEmpty(driverHead)){
            try {
                DriverInfoManager.getInstance().loadImage(requireContext(),driverHead,binding.ivHead);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        binding.edtName.setText(driverName);
        binding.edtNumber.setText(driverCode);
        if(!TextUtils.isEmpty(driverStar)){
            binding.rbStar.setRating(Float.parseFloat(driverStar));
        }
        binding.tvPhone.setText("值班电话：" + complainMobile);
    };

    private final UiMessageUtils.UiMessageCallback failedListener = localMessage -> {

    };

    private final UiMessageUtils.UiMessageCallback finishListener = localMessage -> {
        binding.pbLoading.setVisibility(View.GONE);
    };

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getLifecycle().removeObserver(DriverInfoManager.getInstance());
    }
}
