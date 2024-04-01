package com.yunbiao.publicity_guideboard.ui;

import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.blankj.utilcode.util.ClickUtils;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.yunbiao.publicity_guideboard.R;
import com.yunbiao.publicity_guideboard.databinding.FragmentDownloadInfoBinding;
import com.yunbiao.publicity_guideboard.net.Downloader;
import com.yunbiao.publicity_guideboard.utils.DownloadUtils;
import com.yunbiao.publicity_guideboard.utils.PublicityManager;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class DownloadInfoFragment extends BaseFragment<FragmentDownloadInfoBinding>{
    @Override
    protected int getLayoutId() {
        return R.layout.fragment_download_info;
    }

    @Override
    protected void initView() {
        binding.rlvDownload.setLayoutManager(new LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false));
        binding.rlvDownload.setAdapter(new DownloadInfoAdapter());

        ClickUtils.applyGlobalDebouncing(binding.btnRefresh, 500, v -> initData());
    }

    private Disposable disposable;
    @Override
    protected void initData() {
        if(disposable != null && !disposable.isDisposed()){
            return;
        }
        binding.pbLoading.setVisibility(View.VISIBLE);
        disposable = Observable.timer(1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> {
                    binding.pbLoading.setVisibility(View.GONE);

                    List<BaseDownloadTask> downloadTask = DownloadUtils.getInstance().getDownloadTask();
                    if(downloadTask.isEmpty()){
                        binding.tvTips.setText("暂无下载");
                        binding.tvTips.setVisibility(View.VISIBLE);
                    } else {
                        binding.tvTips.setVisibility(View.GONE);
                        DownloadInfoAdapter adapter = (DownloadInfoAdapter) binding.rlvDownload.getAdapter();
                        adapter.setData(downloadTask);
                    }
                });
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
