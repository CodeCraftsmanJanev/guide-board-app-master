package com.yunbiao.publicity_guideboard.utils;

import android.text.TextUtils;
import android.util.Log;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import com.alibaba.fastjson.JSONObject;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.blankj.utilcode.util.UiMessageUtils;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.convert.StringConvert;
import com.lzy.okgo.model.HttpParams;
import com.lzy.okgo.model.Response;
import com.lzy.okrx2.adapter.ObservableResponse;
import com.yunbiao.publicity_guideboard.db.Advert;
import com.yunbiao.publicity_guideboard.db.DaoManager;
import com.yunbiao.publicity_guideboard.net.Downloader;
import com.yunbiao.publicity_guideboard.net.PublicityCheckFunction;
import com.yunbiao.publicity_guideboard.net.PublicityResponseConverter;
import com.yunbiao.publicity_guideboard.net.PublicityResponse;
import com.yunbiao.publicity_guideboard.net.RetryWithDelay;
import com.yunbiao.publicity_guideboard.net.TimeCheckFunction;
import com.yunbiao.publicity_guideboard.net.TimeResponse;
import com.yunbiao.publicity_guideboard.net.TimeResponseConverter;
import com.yunbiao.publicity_guideboard.system.Cache;
import com.yunbiao.publicity_guideboard.system.Path;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class PublicityManager implements LifecycleObserver {
    private static final String TAG = "PublicityManager";

    public static final int RESET = 200;
    public static final int ADD_ADVERT = 201;
    public static final int SET_CACHE = 202;
    public static final int REMOVE_ADVERT = 203;

    private static final String TIME_URL = "http://221.10.114.137:5012/joffice/system/getAppInfoAppUser.do";
    private static final String URL = "https://221.10.114.137:6057/sdhy-root/advertisingTaskSchedule/hyAdvertisingTaskSchedule/getAdvertisingTaskScheduleByBusCode";
    private static final String NOTIFY_URL = "https://221.10.114.137:6057/sdhy-root/advertisingTaskSchedule/hyAdvertisingTaskSchedule/advertisingVideoCallback";

    private static final class Holder {
        public static final PublicityManager INSTANCE = new PublicityManager();
    }

    private PublicityManager(){
//        UiMessageUtils.getInstance().send(PublicityManager.SET_CACHE);
        DownloadUtils.getInstance().setOnFinishedRunnable(this::notifyDownloadResult);
        DownloadUtils.getInstance().setOnSingleCompleteConsumer(advert -> UiMessageUtils.getInstance().send(ADD_ADVERT,advert.getNumber()));
    }

    public static PublicityManager getInstance(){
        return Holder.INSTANCE;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void cancel(){
        if(dataTask != null && !dataTask.isDisposed()){
            dataTask.dispose();
            dataTask = null;
        }
        OkGo.getInstance().cancelTag(URL);
    }

    private Disposable dataTask;
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    public void load(){
        String userName = Cache.getString(Cache.Key.USER_NAME,Cache.Default.USER_NAME);
        String password = Cache.getString(Cache.Key.PASSWORD,Cache.Default.PASSWORD);
        String busCode = Cache.getString(Cache.Key.BUS_CODE);
        if(TextUtils.isEmpty(userName) || TextUtils.isEmpty(password) || TextUtils.isEmpty(busCode)){
            d("无可用参数，停止处理");
            return;
        }
        dataTask = Observable
                .concat(calibrationTime().subscribeOn(Schedulers.io()),
                        requestData().subscribeOn(Schedulers.io()),
                        downloadTask().subscribeOn(Schedulers.io()))
                .repeatWhen(objectObservable ->
                        objectObservable.flatMap((Function<Object, ObservableSource<?>>) o ->
                                Observable.timer(15, TimeUnit.MINUTES)
                        )
                )
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(o -> {}, throwable -> {}, () -> {});
    }

    private Observable<Object> calibrationTime(){
        return Observable
                .create(emitter ->
                        OkGo.<TimeResponse>get(TIME_URL)
                        .converter(new TimeResponseConverter(TAG,TIME_URL))
                        .adapt(new ObservableResponse<>())
                        .flatMap(new TimeCheckFunction())
                        .subscribe(new Observer<TimeResponse>() {
                            @Override
                            public void onSubscribe(Disposable d) {
                                Log.d(TAG, "onSubscribe: ");
                            }

                            @Override
                            public void onNext(TimeResponse timeResponse) {
                                Log.d(TAG, "onNext: " + timeResponse);
                                List<TimeResponse.Result> results = timeResponse.getResult();
                                if(results != null && !results.isEmpty()){
                                    TimeResponse.Result result = results.get(0);
                                    long time = result.getTime();
                                    String yyyyMMddHHmmss = TimeUtils.date2String(new Date(time * 1000), "yyyyMMddHHmmss");

                                    int year = Integer.parseInt(yyyyMMddHHmmss.substring(0, 4));//年
                                    int month = Integer.parseInt(yyyyMMddHHmmss.substring(4,6));//月
                                    int day = Integer.parseInt(yyyyMMddHHmmss.substring(6,8));//日
                                    int hours = Integer.parseInt(yyyyMMddHHmmss.substring(8,10));//日
                                    int minutes = Integer.parseInt(yyyyMMddHHmmss.substring(10,12));//日
                                    int seconds = Integer.parseInt(yyyyMMddHHmmss.substring(12));//日
                                    int[] ints = {year, month, day, hours, minutes,seconds};

                                    int i = SmdtUtils.getInstance().setTime(ints[0], ints[1], ints[2], ints[3], ints[4]);
                                    Log.d(TAG, "onNext: 校时：" + i);
                                }

                                emitter.onNext(new Object());
                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.e(TAG, "onError: ", e);
                                emitter.onError(e);
                            }

                            @Override
                            public void onComplete() {
                                Log.d(TAG, "onComplete: ");
                                emitter.onComplete();
                            }
                        }));
    }

    private Observable<PublicityResponse> requestData(){
        return Observable
                .create((ObservableOnSubscribe<PublicityResponse>) emitter -> {
                    Log.e(TAG, "requestData: 111111111111");
                    HttpParams httpParams = new HttpParams();
                    httpParams.put("username",Cache.getString(Cache.Key.USER_NAME,Cache.Default.USER_NAME));
                    httpParams.put("password",getPassword());
                    httpParams.put("busCode", Cache.getString(Cache.Key.BUS_CODE));
//                    Observable.fromCallable(() -> {
//                        String s = "{\"success\":true,\"message\":\"操作成功！\",\"code\":200,\"result\":{\"id\":60678,\"busCode\":\"100531\",\"lineCode\":null,\"taskNumber\":\"RW20230717001\",\"taskName\":\"中车测试\",\"state\":\"1\",\"releaseDate\":\"2023-07-17 11:01:13\",\"takeOverDate\":\"2023-07-17 11:42:16\",\"createBy\":null,\"createTime\":null,\"updateBy\":null,\"updateTime\":null,\"delFlag\":null,\"hyAdvertisingReleaseList\":[{\"id\":307,\"advertisingNumber\":\"GG2023060901\",\"advertisingName\":\"20230609嘉州通APP宣传\",\"fileName\":null,\"filePath\":\"https://lsgjgs.cn:6017/adv/Advertise/20230609/20230609嘉州通宣传.mp4\",\"fileType\":\"1\",\"releaseDate\":null,\"whetherIssued\":\"0\",\"createBy\":null,\"createTime\":null,\"updateBy\":null,\"updateTime\":null,\"delFlag\":null,\"advertisementType\":\"1\",\"timeAndSite\":\"轮播\",\"advNumberType\":\"1\"},{\"id\":316,\"advertisingNumber\":\"GG2023070402\",\"advertisingName\":\"20230704人生抉择的瞬间\",\"fileName\":null,\"filePath\":\"https://lsgjgs.cn:6057/adv/Advertise/20230704/20230704人生抉择的瞬间.mp4\",\"fileType\":\"1\",\"releaseDate\":null,\"whetherIssued\":\"0\",\"createBy\":null,\"createTime\":null,\"updateBy\":null,\"updateTime\":null,\"delFlag\":null,\"advertisementType\":\"1\",\"timeAndSite\":\"轮播\",\"advNumberType\":\"1\"},{\"id\":309,\"advertisingNumber\":\"GG2023061601\",\"advertisingName\":\"人人安全\",\"fileName\":null,\"filePath\":\"https://lsgjgs.cn:6017/adv/Advertise/20230616/人人安全.mp4\",\"fileType\":\"1\",\"releaseDate\":null,\"whetherIssued\":\"0\",\"createBy\":null,\"createTime\":null,\"updateBy\":null,\"updateTime\":null,\"delFlag\":null,\"advertisementType\":\"1\",\"timeAndSite\":\"轮播\",\"advNumberType\":\"1\"}],\"advertisingNumber\":null,\"advertisingName\":null,\"advertisementType\":null,\"beginTime\":null,\"endTime\":null,\"screenLocation\":0},\"data\":null,\"timestamp\":1689566209610}";
//                        Response<PublicityResponse> responseResponse = new Response<>();
//                        responseResponse.setBody(JSONObject.parseObject(s,PublicityResponse.class));
//                        return responseResponse;
//                    })
                    OkGo.<PublicityResponse>get(URL).tag(URL)
                            .params(httpParams)
                            .converter(new PublicityResponseConverter(TAG,URL))
                            .adapt(new ObservableResponse<>())
                            .flatMap(new PublicityCheckFunction())
                            .subscribe(new Observer<PublicityResponse>() {
                                @Override
                                public void onSubscribe(Disposable d) {
                                    d("发送请求：" + httpParams);
                                }

                                @Override
                                public void onNext(PublicityResponse publicityResponse) {
                                    emitter.onNext(publicityResponse);
                                }

                                @Override
                                public void onError(Throwable e) {
                                    emitter.onError(e);
                                }

                                @Override
                                public void onComplete() {
                                    emitter.onComplete();
                                }
                            });
                })
                .map(clearDatabase())
                .map(updateDatabase())
                .retryWhen(RetryWithDelay.defaultDelay(TAG,URL));
    }

    private String getPassword(){
        long timeMillis = System.currentTimeMillis() / 1000 - 28800;
        Log.e(TAG, "时间戳：" + timeMillis);
        String hex = Long.toHexString(timeMillis);
        Log.e(TAG, "转换后：" + hex);

        String key = hex + Cache.getString(Cache.Key.PASSWORD);
        d( "生成密钥: " + key);
        return key;
    }

    /**
     * 清理数据库
     * @return
     */
    private Function<PublicityResponse, PublicityResponse> clearDatabase(){
        return publicityResponse -> {
            PublicityResponse.Result result = publicityResponse.getResult();
            if(result != null){
                int deleteNumber = 0;
                List<PublicityResponse.Advertise> remoteList = result.getHyAdvertisingReleaseList();
                if(remoteList != null){
                    //生成Id列表
                    List<Long> remoteIdList = new ArrayList<>();
                    for (PublicityResponse.Advertise advertise : remoteList) {
                        remoteIdList.add(advertise.getId());
                    }
                    //清理数据库
                    List<Advert> localList = DaoManager.get().query(Advert.class);
                    for (Advert advert : localList) {
                        if(remoteIdList.contains(advert.getId())){
                            continue;
                        }
                        DaoManager.get().delete(advert);
                        deleteNumber ++;
                    }
                }
                d("清理数据库：" + deleteNumber + "条");
            } else {
                DaoManager.get().deleteAll(Advert.class);
                d("无广告数据，清除所有");
            }
            return publicityResponse;
        };
    }

    /**
     * 更新数据库
     * @return
     */
    private Function<PublicityResponse, PublicityResponse> updateDatabase(){
        return publicityResponse -> {
            PublicityResponse.Result result = publicityResponse.getResult();
            if(result != null){
                List<PublicityResponse.Advertise> remoteList = result.getHyAdvertisingReleaseList();
                if(remoteList != null){
                    int addNumber = 0,deleteNumber = 0,updateNumber = 0;
                    for (PublicityResponse.Advertise advertise : remoteList) {
                        d("存储数据：" + advertise.toString());
                        String advertisingNumber = advertise.getAdvertisingNumber();
                        int delFlag = advertise.getDelFlag();

                        Advert advert = DaoManager.get().queryAdvertByNumber(advertisingNumber);
                        //数据库中没有该数据
                        if(advert == null){
                            if(delFlag != 1){
                                advert = new Advert();
                                advert.create(advertise);
                                DaoManager.get().add(advert);
                                addNumber ++;
                            }
                        }
                        //标识为删除
                        else if(delFlag == 1){
                            if(!TextUtils.isEmpty(advert.getLocalPath()) && FileUtils.isFileExists(advert.getLocalPath())){
                                FileUtils.delete(advert.getLocalPath());
                            }
                            DaoManager.get().delete(advert);
                            deleteNumber ++;
                        }
                        //更新
                        else if(!advert.compare(advertise)){
                            updateNumber ++;
                            advert.update(advertise);
                            DaoManager.get().update(advert);
                        }
                    }
                    d("更新数据库：" + "添加：" + addNumber + ",删除：" + deleteNumber + ",更新：" + updateNumber);
                } else {
                    d("无可存储数据");
                }
            } else {
                d("无广告数据");
            }

            return publicityResponse;
        };
    }

    private Observable<List<Advert>> downloadTask(){
        return Observable
                .fromCallable(() -> DaoManager.get().query(Advert.class))
                .doOnSubscribe(disposable -> d("下载文件"))
                .doOnNext(advertList -> DownloadUtils.getInstance().start(advertList))
                .doOnError(throwable -> Log.e(TAG, "downloadFile: " + throwable.getMessage()));
    }

    private Disposable notifyDisposable;
    private void notifyDownloadResult(){
        String busCode = Cache.getString(Cache.Key.BUS_CODE);
        if(TextUtils.isEmpty(busCode)){
            return;
        }
        if(notifyDisposable != null && !notifyDisposable.isDisposed()){
            d("正在通知服务器");
            return;
        }
        HttpParams httpParams = new HttpParams();
        httpParams.put("busCode",busCode);

        notifyDisposable = OkGo.<String>get(NOTIFY_URL).tag(NOTIFY_URL)
                .params(httpParams)
                .converter(new StringConvert())
                .adapt(new ObservableResponse<>())
                .delay(5,TimeUnit.SECONDS)
                .observeOn(Schedulers.io())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        stringResponse -> {
                            if (stringResponse.isSuccessful()) {
                                d("请求成功：" + stringResponse.body());
                            } else {
                                d("请求失败");
                            }
                        },
                        throwable -> {
                            e("请求错误：" + throwable.getMessage());
                        }
                );
    }

    private void d(String log){
        LogUtils.d(TAG,TAG + ":" + log);
    }

    private void e(String log){
        LogUtils.e(TAG,TAG + ":" + log);
    }

}
