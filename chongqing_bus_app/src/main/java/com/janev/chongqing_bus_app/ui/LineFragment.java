package com.janev.chongqing_bus_app.ui;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.UiMessageUtils;
import com.janev.chongqing_bus_app.R;
import com.janev.chongqing_bus_app.databinding.FragmentLineBinding;
import com.janev.chongqing_bus_app.serial.ChongqingV1Handler;
import com.janev.chongqing_bus_app.system.UiEvent;

public class LineFragment extends BaseFragment<FragmentLineBinding> implements UiMessageUtils.UiMessageCallback {
    @Override
    protected int getLayoutId() {
        return R.layout.fragment_line;
    }

    @Override
    protected void initView() {
        UiMessageUtils.getInstance().addListener(this);
    }

    @Override
    protected void initData() {

    }

    @Override
    public void handleMessage(@NonNull UiMessageUtils.UiMessage localMessage) {
        switch (localMessage.getId()) {
            case UiEvent.EVENT_LINE_NAME:
                String lineName = (String) localMessage.getObject();
                if(!TextUtils.isEmpty(lineName)){
                    if(lineName.contains("路")){
                        lineName = lineName.replaceAll("路","");
                    }
                    binding.tvLineName.setText(lineName);
                }
                break;
            case UiEvent.EVENT_SITE_LIST:
                ChongqingV1Handler.SiteList siteList = (ChongqingV1Handler.SiteList) localMessage.getObject();
                String start = siteList.getStart();
                String end = siteList.getEnd();
                if(!TextUtils.isEmpty(start)){
                    if(start.contains("_")){
                        start = start.split("_")[0];
                    }
                    binding.tvStartSite.setText(start);
                }
                if(!TextUtils.isEmpty(end)){
                    if(end.contains("_")){
                        end = end.split("_")[0];
                    }
                    binding.tvEndSite.setText(end);
                }
                break;
            case UiEvent.EVENT_NEXT_SITE:
                String nextSite = localMessage.getObject().toString();
//                if(!TextUtils.isEmpty(nextSite)){
//                    nextSite = "下一站:" + nextSite;
//                }
                binding.tvNextSite.setText(nextSite);
                break;
        }
    }
}
