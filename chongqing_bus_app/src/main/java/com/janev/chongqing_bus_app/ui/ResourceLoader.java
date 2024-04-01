package com.janev.chongqing_bus_app.ui;

import android.util.Log;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.UiMessageUtils;
import com.janev.chongqing_bus_app.adapter.SiteListAdapter;
import com.janev.chongqing_bus_app.system.Cache;
import com.janev.chongqing_bus_app.system.Path;
import com.janev.chongqing_bus_app.system.UiEvent;

import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class ResourceLoader {
    private static final String TAG = "ResourceLoader";

    private static final class Holder {
        public static final ResourceLoader INSTANCE = new ResourceLoader();
    }

    private ResourceLoader(){}

    public static ResourceLoader getInstance(){
        return Holder.INSTANCE;
    }

    public void load(){
        loadLocal();
    }

    private void loadLocal(){
        Observable
                .fromCallable(() -> {
//                    List<File> files = FileUtils.listFilesInDir(Path.getVideoPath());
                    List<File> files = FileUtils.listFilesInDir(Path.getMaterialDir());
                    Log.d(TAG, "loadLocal: 读取资源列表：" + Path.getMaterialDir());
                    Log.d(TAG, "loadLocal: 读取资源列表：" + files.size());
                    return files;
                })
                .flatMap((Function<List<File>, ObservableSource<File>>) Observable::fromIterable)
                .doOnSubscribe(disposable -> {
                    Log.d(TAG, "loadLocal: 发送重置消息");
                    UiMessageUtils.getInstance().send(UiEvent.EVENT_RESET_RES);
                })
                .doOnNext(file -> {
                    Log.d(TAG, "loadLocal: 发送添加资源：" + file.getPath());
                    UiMessageUtils.getInstance().send(UiEvent.EVENT_ADD_RES,file.getPath());
                })
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe();
    }
}
