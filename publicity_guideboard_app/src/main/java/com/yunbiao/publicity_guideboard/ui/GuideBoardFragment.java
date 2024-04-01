package com.yunbiao.publicity_guideboard.ui;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.UiMessageUtils;
import com.yunbiao.publicity_guideboard.R;
import com.yunbiao.publicity_guideboard.databinding.FragmentGuideBoardBinding;
import com.yunbiao.publicity_guideboard.system.Constants;

public class GuideBoardFragment extends BaseFragment<FragmentGuideBoardBinding>{
    @Override
    protected int getLayoutId() {
        return R.layout.fragment_guide_board;
    }

    @Override
    protected void initView() {

    }

    @Override
    protected void initData() {
        UiMessageUtils.getInstance().addListener(Constants.TYPE_SITE_INFO, siteInfoListener);
        UiMessageUtils.getInstance().addListener(Constants.TYPE_IN_OUT, inOutListener);
    }

    private final UiMessageUtils.UiMessageCallback siteInfoListener = localMessage -> {
        Object object = localMessage.getObject();
        if(object != null){
            String[] strings = (String[]) object;
            String lineName = strings[0];
            String start = strings[1];
            String end = strings[2];

            if(!TextUtils.equals("---",lineName)){
                String l = lineName.contains("路") ? lineName : (lineName + "路");
                binding.tvLineName.setText(l);
            }
            if(!TextUtils.isEmpty(start)){
                binding.tvStart.setText(start);
            }
            if(!TextUtils.isEmpty(end)){
                binding.tvEnd.setText(end);
            }
        }
    };
    private final UiMessageUtils.UiMessageCallback inOutListener = localMessage -> {
        Object object = localMessage.getObject();
        if(object != null){
            String[] strings = (String[]) object;
            String inOut = strings[0];
            String currSite = strings[1];
            String nextSite = strings[2];
            //出站
            if(TextUtils.equals("11",inOut)){
                binding.tvInOut.setText("前方到站");
                binding.tvNext.setVisibility(View.GONE);
                binding.tvNextLabel.setVisibility(View.GONE);
                binding.tvSiteName.setText(nextSite);
            } else {
                binding.tvInOut.setText("已到站");
                binding.tvNext.setVisibility(View.VISIBLE);
                binding.tvNextLabel.setVisibility(View.VISIBLE);
                binding.tvSiteName.setText(currSite);
                binding.tvNext.setText(nextSite);
            }
        }
    };

    @Override
    public void onDestroyView() {
        UiMessageUtils.getInstance().removeListeners(Constants.TYPE_SITE_INFO);
        UiMessageUtils.getInstance().removeListeners(Constants.TYPE_IN_OUT);
        super.onDestroyView();
    }
}
