package com.janev.chongqing_bus_app.ui;

import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.blankj.utilcode.util.UiMessageUtils;
import com.janev.chongqing_bus_app.R;
import com.janev.chongqing_bus_app.adapter.MoreInfoAdapter;
import com.janev.chongqing_bus_app.databinding.FragmentConnectionBinding;
import com.janev.chongqing_bus_app.system.UiEvent;
import com.janev.chongqing_bus_app.tcp.client.TCPClient;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class ConnectionFragment extends BaseFragment<FragmentConnectionBinding>{
    public static ConnectionFragment newInstance() {
        return new ConnectionFragment();
    }

    private Runnable onClickBackRunnable;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_connection;
    }

    public void setOnClickBackRunnable(Runnable onClickBackRunnable) {
        this.onClickBackRunnable = onClickBackRunnable;
    }

    @Override
    protected void initView() {
        binding.rlvList.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rlvList.setAdapter(new MoreInfoAdapter());

        binding.btnBack.setOnClickListener(view -> {
            if(onClickBackRunnable != null){
                onClickBackRunnable.run();
            }
        });

        UiMessageUtils.getInstance().addListener(UiEvent.EVENT_QUERY_TCP_CONNECTION, localMessage -> {
            Object object = localMessage.getObject();
            if(object == null){
                return;
            }
            TCPClient.ConnectionInfo connectionInfo = (TCPClient.ConnectionInfo) object;

            MoreInfoAdapter moreInfoAdapter = (MoreInfoAdapter) binding.rlvList.getAdapter();
            if (moreInfoAdapter.getItemCount() == 0) {
                List<MoreInfoAdapter.MoreInfo> moreInfoList = new ArrayList<>();
                moreInfoList.add(new MoreInfoAdapter.MoreInfo("主服务器地址：",connectionInfo.getMainIp()));
                moreInfoList.add(new MoreInfoAdapter.MoreInfo("主服务器端口：",connectionInfo.getMainPort()));
                moreInfoList.add(new MoreInfoAdapter.MoreInfo("备用服务器地址：",connectionInfo.getSpareIp()));
                moreInfoList.add(new MoreInfoAdapter.MoreInfo("备用服务器端口：",connectionInfo.getSparePort()));
                moreInfoList.add(new MoreInfoAdapter.MoreInfo("连接状态：",connectionInfo.getIsConnect()));
                moreInfoList.add(new MoreInfoAdapter.MoreInfo("登录状态：",connectionInfo.getIsLogon()));
                moreInfoList.add(new MoreInfoAdapter.MoreInfo("登录时间：",connectionInfo.getLoginTime()));
                moreInfoList.add(new MoreInfoAdapter.MoreInfo("心跳响应时间：",connectionInfo.getPulseTime()));
                moreInfoAdapter.setData(moreInfoList);
                binding.pbLoading.setVisibility(View.GONE);
            } else {
                List<MoreInfoAdapter.MoreInfo> list = moreInfoAdapter.getList();
                for (MoreInfoAdapter.MoreInfo moreInfo : list) {
                    switch (moreInfo.getTitle()) {
                        case "主服务器地址：":
                            moreInfo.setContent(connectionInfo.getMainIp());
                            break;
                        case "主服务器端口：":
                            moreInfo.setContent(connectionInfo.getMainPort());
                            break;
                        case "备用服务器地址：":
                            moreInfo.setContent(connectionInfo.getSpareIp());
                            break;
                        case "备用服务器端口：":
                            moreInfo.setContent(connectionInfo.getSparePort());
                            break;
                        case "连接状态：":
                            moreInfo.setContent(connectionInfo.getIsConnect());
                            break;
                        case "登录状态：":
                            moreInfo.setContent(connectionInfo.getIsLogon());
                            break;
                        case "登录时间：":
                            moreInfo.setContent(connectionInfo.getLoginTime());
                            break;
                        case "心跳响应时间：":
                            moreInfo.setContent(connectionInfo.getPulseTime());
                            break;
                    }


                }


            }
        });
    }

    private Disposable disposable;
    @Override
    protected void initData() {
        if(disposable != null && !disposable.isDisposed()){
            disposable.dispose();
            disposable = null;
        }
        disposable = Observable.interval(1,1, TimeUnit.SECONDS)
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> UiMessageUtils.getInstance().send(UiEvent.EVENT_QUERY_TCP_CONNECTION));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(disposable != null && !disposable.isDisposed()){
            disposable.dispose();
            disposable = null;
        }
    }
}
