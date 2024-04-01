package com.janev.chongqing_bus_app.ui;

import android.annotation.SuppressLint;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.janev.chongqing_bus_app.R;
import com.janev.chongqing_bus_app.adapter.MoreInfoAdapter;
import com.janev.chongqing_bus_app.databinding.FragmentMoreBinding;
import com.janev.chongqing_bus_app.tcp.message.MessageUtils;
import com.janev.chongqing_bus_app.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class MoreFragment extends BaseFragment<FragmentMoreBinding>{
    public static MoreFragment newInstance() {
        return new MoreFragment();
    }

    private Runnable onClickBackRunnable;

    public void setOnClickBackRunnable(Runnable onClickBackRunnable) {
        this.onClickBackRunnable = onClickBackRunnable;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_more;
    }

    @Override
    protected void initView() {
        binding.rlvList.setLayoutManager(new LinearLayoutManager(requireContext(),GridLayoutManager.VERTICAL,false));
        binding.rlvList.setAdapter(new MoreInfoAdapter());

        binding.btnBack.setOnClickListener(v -> {
            if(onClickBackRunnable != null){
                onClickBackRunnable.run();
            }
        });
    }

    @SuppressLint("MissingPermission")
    @Override
    protected void initData() {
        MoreInfoAdapter moreInfoAdapter = (MoreInfoAdapter) binding.rlvList.getAdapter();

        List<MoreInfoAdapter.MoreInfo> moreInfoList = new ArrayList<>();
        moreInfoList.add(new MoreInfoAdapter.MoreInfo("厂商编码:",MessageUtils.getProductNumberHex()));
        moreInfoList.add(new MoreInfoAdapter.MoreInfo("厂商授权码:",MessageUtils.getAuthNumberHex()));

        moreInfoList.add(new MoreInfoAdapter.MoreInfo("硬件序列号:", MessageUtils.getDeviceId()));
        moreInfoList.add(new MoreInfoAdapter.MoreInfo("硬件版本号:", MessageUtils.getModel()));
        moreInfoList.add(new MoreInfoAdapter.MoreInfo("固件版本号:",MessageUtils.getSdkVersionName()));
        moreInfoList.add(new MoreInfoAdapter.MoreInfo("应用版本号:", MessageUtils.getAppVersionName()));
        moreInfoList.add(new MoreInfoAdapter.MoreInfo("设备出场日期:","00000000"));
        moreInfoList.add(new MoreInfoAdapter.MoreInfo("设备自编号:",MessageUtils.getDeviceNumber()));
        moreInfoList.add(new MoreInfoAdapter.MoreInfo("终端编号:",MessageUtils.getTerminalNumber()));
        moreInfoList.add(new MoreInfoAdapter.MoreInfo("线路号:", MessageUtils.getLineName()));
        moreInfoList.add(new MoreInfoAdapter.MoreInfo("设备地址:",MessageUtils.getDeviceAddressHex()));
        moreInfoList.add(new MoreInfoAdapter.MoreInfo("LCD屏幕参数:",MessageUtils.getScreenParams()));
        moreInfoList.add(new MoreInfoAdapter.MoreInfo("屏幕亮度:", MessageUtils.getBrightness() + ""));
        moreInfoList.add(new MoreInfoAdapter.MoreInfo("设备音量:", MessageUtils.getVolumePercent() + "%"));
        moreInfoList.add(new MoreInfoAdapter.MoreInfo("车辆自编号:",MessageUtils.getCarNumber()));
        moreInfoList.add(new MoreInfoAdapter.MoreInfo("车辆车牌号:",StringUtils.hexStringToString(MessageUtils.getCarLicenseNumber().replaceAll("^0+", ""))));
        moreInfoList.add(new MoreInfoAdapter.MoreInfo("心跳间隔:",MessageUtils.getPulseInterval() + "秒"));
        moreInfoList.add(new MoreInfoAdapter.MoreInfo("消息重发次数:","00"));
        moreInfoList.add(new MoreInfoAdapter.MoreInfo("主服务器地址:",MessageUtils.getMainServerAddress()));
        moreInfoList.add(new MoreInfoAdapter.MoreInfo("主服务器端口:",MessageUtils.getMainServerPort() + ""));
        moreInfoList.add(new MoreInfoAdapter.MoreInfo("备用服务器地址:",MessageUtils.getSpareServerAddress()));
        moreInfoList.add(new MoreInfoAdapter.MoreInfo("备用服务器端口:",MessageUtils.getSpareServerPort() + ""));

        moreInfoAdapter.setData(moreInfoList);
    }
}
