package com.janev.chongqing_bus_app.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.Fragment;

import com.blankj.utilcode.util.UiMessageUtils;

public abstract class BaseFragment<T extends ViewDataBinding> extends Fragment implements UiMessageUtils.UiMessageCallback {
    protected final String TAG = getClass().getSimpleName();
    protected T binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater,getLayoutId(),container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Log.d(TAG, view.getWidth() + " --- " + view.getHeight());
                view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
        initView();
        initData();
    }

    protected abstract int getLayoutId();
    protected abstract void initView();
    protected abstract void initData();

    @Override
    public void handleMessage(@NonNull UiMessageUtils.UiMessage localMessage) {

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        UiMessageUtils.getInstance().removeListener(this);
    }
}
