package com.yunbiao.publicity_guideboard.ui;

import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.Utils;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadLargeFileListener;
import com.liulishuo.filedownloader.model.FileDownloadStatus;
import com.lzy.okgo.model.Progress;
import com.yunbiao.publicity_guideboard.R;
import com.yunbiao.publicity_guideboard.databinding.ItemDownloadInfoBinding;
import com.yunbiao.publicity_guideboard.net.Downloader;
import com.yunbiao.publicity_guideboard.utils.DownloadUtils;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class DownloadInfoAdapter extends RecyclerView.Adapter<DownloadInfoAdapter.Holder> {

    private final List<BaseDownloadTask> downloaderList = new ArrayList<>();

    public void setData(List<BaseDownloadTask> list){
        downloaderList.clear();
        if(!list.isEmpty()){
            downloaderList.addAll(list);
        }
        notifyItemRangeChanged(0,downloaderList.size() - 1);
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(
                DataBindingUtil
                        .inflate(
                                LayoutInflater.from(parent.getContext()),
                                R.layout.item_download_info,
                                parent,
                                false
                        ));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        holder.bindData(downloaderList.get(position));
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull Holder holder) {
        holder.detached();
    }

    @Override
    public int getItemCount() {
        return downloaderList.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        private static final String TAG = "Holder";
        private final NumberFormat numberFormat;
        private BaseDownloadTask baseDownloadTask;
        private ItemDownloadInfoBinding binding;
        public Holder(@NonNull ItemDownloadInfoBinding itemDownloadInfoBinding) {
            super(itemDownloadInfoBinding.getRoot());
            this.binding = itemDownloadInfoBinding;

            numberFormat = NumberFormat.getPercentInstance();
            numberFormat.setMinimumFractionDigits(2);
        }

        public void bindData(BaseDownloadTask task) {
            this.baseDownloadTask = task;
            this.baseDownloadTask.setListener(fileDownloadLargeFileListener);

            String fileName = FileUtils.getFileName(task.getTargetFilePath());
            binding.tvName.setText(TextUtils.isEmpty(fileName) ? task.getTargetFilePath() : fileName);
            binding.tvIndex.setText(String.valueOf((getAdapterPosition() + 1)));

            switch (task.getStatus()) {
                case FileDownloadStatus.pending:
                    binding.tvPercent.setText("等待");
                    break;
                case FileDownloadStatus.started:
                    binding.tvPercent.setText("已开始");
                    break;
                case FileDownloadStatus.connected:
                    binding.tvPercent.setText("已连接");
                    break;
                case FileDownloadStatus.progress:
                    binding.tvPercent.setText("正在下载");
                    break;
                case FileDownloadStatus.blockComplete:
                    binding.tvPercent.setText("下载完成");
                    break;
                case FileDownloadStatus.retry:
                    binding.tvPercent.setText("等待重试");
                    break;
                case FileDownloadStatus.error:
                    binding.tvPercent.setText("下载失败");
                    break;
                case FileDownloadStatus.paused:
                    binding.tvPercent.setText("已暂停");
                    break;
                case FileDownloadStatus.completed:
                    binding.tvPercent.setText("下载完成");
                    break;
                case FileDownloadStatus.warn:
                    binding.tvPercent.setText("有相同任务");
                    break;
            }
        }

        private final FileDownloadLargeFileListener fileDownloadLargeFileListener = new FileDownloadLargeFileListener() {
            @Override
            protected void pending(BaseDownloadTask task, long soFarBytes, long totalBytes) {
                binding.tvPercent.setText("等待");
            }

            @Override
            protected void progress(BaseDownloadTask task, long soFarBytes, long totalBytes) {
                binding.tvPercent.setText("进度：" + getFormatSize(soFarBytes));
            }

            @Override
            protected void paused(BaseDownloadTask task, long soFarBytes, long totalBytes) {
                binding.tvPercent.setText("已暂停");
            }

            @Override
            protected void completed(BaseDownloadTask task) {
                binding.tvPercent.setText("下载完成");
            }

            @Override
            protected void error(BaseDownloadTask task, Throwable e) {
                binding.tvPercent.setText("下载失败");
            }

            @Override
            protected void warn(BaseDownloadTask task) {
                binding.tvPercent.setText("有相同任务");
            }
        };

        public void detached(){
            if(this.baseDownloadTask != null){
                this.baseDownloadTask.setListener(null);
            }
        }
    }


    private static String getFormatSize(long size) {
        //这里限定了输入 1T以内 不包括1T
        if (size <= 0 || size >= 1024 * 1024 * 1024) {
            throw new RuntimeException("输入异常");
        }
        //这里可能出现转换异常
        double dSize = 0;
        try {
            dSize = size;
        } catch (Exception e) {
            e.printStackTrace();
        }
        //除数因子
        double divideBasic = 1024;
        if (size < 1024) { //1kb以内
            if (size < 1000) {
                return String.valueOf(size + "B");
            } else { //大于1000B,转化为kb,基于用户习惯
                return String.format("%.2f", dSize / divideBasic) + "Kb";
            }
        } else if (size < 1024 * 1024) { //1M以内
            if (size < 1024 * 1000) {
                return String.format("%.2f", dSize / divideBasic) + "Kb";
            } else {//大于1000Kb,转化为M
                return String.format("%.2f", dSize / divideBasic / divideBasic) + "Mb";
            }
        } else { //1TB以内
            if (size < 1024 * 1024 * 1000) {
                return String.format("%.2f", dSize / divideBasic / divideBasic) + "Mb";
            } else {//大于1000Mb,转化为T
                return String.format("%.2f", dSize / divideBasic / divideBasic / divideBasic) + "Tb";
            }
        }
    }
}
