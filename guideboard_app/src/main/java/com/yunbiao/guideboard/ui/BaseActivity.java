package com.yunbiao.guideboard.ui;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;

public abstract class BaseActivity<T extends ViewDataBinding> extends AppCompatActivity {

    protected T binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,getLayoutId());

        initView();

        initData();
    }

    protected abstract int getLayoutId();

    protected abstract void initView();

    protected abstract void initData();
}
