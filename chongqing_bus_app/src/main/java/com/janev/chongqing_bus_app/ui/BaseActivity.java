package com.janev.chongqing_bus_app.ui;

import android.os.Bundle;
import android.view.View;
import android.view.Window;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.Fragment;

public abstract class BaseActivity<T extends ViewDataBinding> extends AppCompatActivity {
    protected String TAG = getClass().getSimpleName();
    protected T binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,getLayoutId());

        initView();

        initData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideNav();
    }

    private void hideNav(){
        Window window = getWindow();
        window.getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LOW_PROFILE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );
    }

    protected abstract int getLayoutId();

    protected abstract void initView();

    protected abstract void initData();


    protected void replace(int containerId, Fragment fragment){
        getSupportFragmentManager().beginTransaction().replace(containerId,fragment).commit();
    }

    protected void add(int containerId, Fragment fragment){
        getSupportFragmentManager().beginTransaction().add(containerId,fragment).show(fragment).commit();
    }

    protected void remove(Fragment fragment){
        getSupportFragmentManager().beginTransaction().remove(fragment).commit();
    }

}
