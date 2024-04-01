package com.janev.chongqing_bus_app.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.janev.chongqing_bus_app.R;
import com.janev.chongqing_bus_app.databinding.FragmentBroadSiteBinding;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class BroadSiteFragment extends BaseFragment<FragmentBroadSiteBinding>{
    private static final int CLOSE_DELAY_TIME = 10;
    private static final String KEY_SITE_NAME = "keySiteName";
    private static final String KEY_NEXT_SITE_NAME = "keyNextSiteName";
    private static final String KEY_SITE_RESPONSIVE = "keySiteResponsive";
    public static BroadSiteFragment newInstance(String site, String nextSite,String isResponsiveStr) {
        if(TextUtils.isEmpty(site) && TextUtils.isEmpty(nextSite)){
            return null;
        }
        Bundle bundle = new Bundle();
        bundle.putString(KEY_SITE_NAME,site);
        bundle.putString(KEY_NEXT_SITE_NAME,nextSite);
        bundle.putString(KEY_SITE_RESPONSIVE,isResponsiveStr);
        BroadSiteFragment broadSiteFragment = new BroadSiteFragment();
        broadSiteFragment.setArguments(bundle);
        return broadSiteFragment;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_broad_site;
    }

    @Override
    protected void initView() {

    }

    @Override
    protected void initData() {
        Bundle arguments = getArguments();
        String siteName = arguments.getString(KEY_SITE_NAME);
        String nextSiteName = arguments.getString(KEY_NEXT_SITE_NAME);
        String string = arguments.getString(KEY_SITE_RESPONSIVE);

        update(siteName,nextSiteName,string);
    }

    public void update(String site, String nextSite,String isResponsiveStr) {
        if(!TextUtils.isEmpty(site)){
            binding.tvLabel.setText("本站到达");
            binding.tvSiteName.setText(site);
            binding.tvTips.setVisibility(View.VISIBLE);
            binding.ivTips.setVisibility(View.VISIBLE);
            if(!TextUtils.isEmpty(nextSite)){
                binding.clNextSite.setVisibility(View.VISIBLE);
                binding.tvNextSiteName.setText(nextSite);
            } else {
                binding.clNextSite.setVisibility(View.GONE);
            }
        } else {
            binding.tvLabel.setText("下一站");
            binding.tvSiteName.setText(nextSite);
            binding.clNextSite.setVisibility(View.GONE);
            binding.tvTips.setVisibility(View.GONE);
            binding.ivTips.setVisibility(View.GONE);
        }

        binding.ivResponsive.setVisibility(!TextUtils.isEmpty(isResponsiveStr) && Boolean.parseBoolean(isResponsiveStr) ? View.VISIBLE : View.GONE);

        startTimer();
    }

    private Disposable timerDisposable;
    private void startTimer(){
        stopTimer();
        timerDisposable = Observable.timer(CLOSE_DELAY_TIME, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong ->
                        getParentFragmentManager()
                                .beginTransaction()
                                .setCustomAnimations(R.anim.anim_site_enter,R.anim.anim_site_exit)
                                .remove(this)
                                .commit()
                );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopTimer();
    }

    private void stopTimer(){
        if(timerDisposable != null && !timerDisposable.isDisposed()){
            timerDisposable.dispose();
            timerDisposable = null;
        }
    }
}
