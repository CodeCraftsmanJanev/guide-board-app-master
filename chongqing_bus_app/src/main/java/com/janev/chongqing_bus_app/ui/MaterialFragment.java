package com.janev.chongqing_bus_app.ui;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.UiMessageUtils;
import com.blankj.utilcode.util.Utils;
import com.janev.chongqing_bus_app.R;
import com.janev.chongqing_bus_app.databinding.FragmentMaterialBinding;
import com.janev.chongqing_bus_app.tcp.task.resource.ResourceLocal;

public class MaterialFragment extends BaseFragment<FragmentMaterialBinding>{
    @Override
    protected int getLayoutId() {
        return R.layout.fragment_material;
    }

    @Override
    protected void initView() {
        UiMessageUtils.getInstance().addListener(this);
    }

    @Override
    protected void initData() {
        NetworkUtils.isAvailableAsync(aBoolean -> {
            if(!aBoolean){
                ResourceLocal.readFileFromStorage();
            }
        });
    }

    @Override
    public void handleMessage(@NonNull UiMessageUtils.UiMessage localMessage) {
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}
