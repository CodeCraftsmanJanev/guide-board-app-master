package com.yunbiao.publicity_guideboard.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import com.alibaba.fastjson.JSONObject;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.UiMessageUtils;
import com.bumptech.glide.Glide;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.convert.BitmapConvert;
import com.lzy.okgo.convert.Converter;
import com.lzy.okgo.convert.FileConvert;
import com.lzy.okgo.convert.StringConvert;
import com.lzy.okgo.model.HttpParams;
import com.lzy.okgo.model.Response;
import com.lzy.okrx2.adapter.ObservableResponse;
import com.yunbiao.publicity_guideboard.net.DriverInfoCheckFunction;
import com.yunbiao.publicity_guideboard.net.DriverInfoResponse;
import com.yunbiao.publicity_guideboard.net.DriverInfoResponseConverter;
import com.yunbiao.publicity_guideboard.net.RetryWithDelay;
import com.yunbiao.publicity_guideboard.system.Cache;
import com.yunbiao.publicity_guideboard.system.Path;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

public class DriverInfoManager implements LifecycleObserver {
    private static final String TAG = "DriverInfoManager";

    public static final int START = 100;
    public static final int FINISH = 109;
    public static final int SUCCESS = 101;
    public static final int FAILED = 102;

    private static final String URL = "http://221.10.114.137:5012/joffice/datapush/driverInfoLCDDatapush.do";
    private Disposable disposable;
    private Disposable imageDisposable;

    private static final class Holder{
        public static final DriverInfoManager INSTANCE = new DriverInfoManager();
    }

    public static DriverInfoManager getInstance(){
        return Holder.INSTANCE;
    }

    private DriverInfoManager(){
//        UiMessageUtils.getInstance().send(SUCCESS);
    }


    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void cancel(){
        if(disposable != null && !disposable.isDisposed()){
            disposable.dispose();
            disposable = null;
        }
        OkGo.getInstance().cancelTag(URL);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    public void load(){
        String busCode = Cache.getString(Cache.Key.BUS_CODE);
        if(TextUtils.isEmpty(busCode)){
            return;
        }
        HttpParams httpParams = new HttpParams();
        httpParams.put("busCode",busCode);

        cancel();
        disposable = OkGo.<DriverInfoResponse>post(URL).tag(URL)
                .params(httpParams)
                .converter(new DriverInfoResponseConverter(TAG,URL))//格式化为对象
                .adapt(new ObservableResponse<>())//转换为Rxjava2
                .flatMap(new DriverInfoCheckFunction())//检查结果
                .retryWhen(RetryWithDelay.defaultDelay(TAG,URL))//重试
                .repeatWhen(objectObservable ->
                        objectObservable.flatMap((Function<Object, ObservableSource<?>>) o ->
                                Observable.timer(45,TimeUnit.MINUTES)
                        )
                )
                .subscribeOn(Schedulers.io())//io线程执行
                .observeOn(AndroidSchedulers.mainThread())//主线程回调
                .doOnSubscribe(disposable -> {
                    d("发送请求：" + httpParams);
                    UiMessageUtils.getInstance().send(START);
                })
                .subscribe(
                        driverInfoResponse -> {

                            List<DriverInfoResponse.Data> dataList = driverInfoResponse.getData();
                            if(dataList != null && !dataList.isEmpty()){
                                DriverInfoResponse.Data data = dataList.get(0);
                                Cache.setString(Cache.Key.DRIVER_NAME,data.getDrivername());
                                Cache.setString(Cache.Key.DRIVER_CODE,data.getDrivercode());
                                Cache.setString(Cache.Key.DRIVER_STAR,data.getStarcode());
                                Cache.setString(Cache.Key.DRIVER_HEAD,data.getPhoto());
                                Cache.setString(Cache.Key.COMPLAIN_MOBILE,data.getComplainMobile());
                            } else {
                                Cache.remove(Cache.Key.DRIVER_NAME);
                                Cache.remove(Cache.Key.DRIVER_CODE);
                                Cache.remove(Cache.Key.DRIVER_STAR);
                                Cache.remove(Cache.Key.DRIVER_HEAD);
                                Cache.remove(Cache.Key.COMPLAIN_MOBILE);
                            }

                            UiMessageUtils.getInstance().send(SUCCESS);
                            UiMessageUtils.getInstance().send(FINISH);
                        },
                        throwable -> {
                            e( "throwableConsumer: 错误：" + throwable.getMessage());
                            UiMessageUtils.getInstance().send(FAILED, Objects.requireNonNull(throwable.getMessage()));
                        },
                        () -> {});
    }

    private String mImageUrl;
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void cancelImage(){
        if(imageDisposable != null && !imageDisposable.isDisposed()){
            imageDisposable.dispose();
            imageDisposable = null;
        }
        if(!TextUtils.isEmpty(mImageUrl)){
            OkGo.getInstance().cancelTag(mImageUrl);
        }
    }

//      public void loadImage(Context context,String url, ImageView imageView){
//          if(imageView == null){
//              return;
//          }
//          if(TextUtils.isEmpty(url)){
//              return;
//          }
//
//          Consumer<File> fileConsumer = file -> Glide.with(context).asBitmap().load(file).into(imageView);
//          Consumer<Throwable> throwableConsumer = throwable -> {
//          };
//          Action completeAction = () -> {};
//
//          File destFile = new File(Path.getHeadPath(), FileUtils.getFileName(url));
//          if (FileUtils.isFileExists(destFile)) {
//              d("头像已存在，不下载");
//              try {
//                  fileConsumer.accept(destFile);
//              } catch (Exception e) {
//                  e.printStackTrace();
//              }
//              return;
//          }
//
//          mImageUrl = url;
//          cancelImage();
//          imageDisposable = OkGo.<File>get(url).tag(url)
//                  .converter(new FileConvert())
//                  .adapt(new ObservableResponse<>())
//                  .flatMap((Function<Response<File>, ObservableSource<File>>) fileResponse -> {
//                      if(!fileResponse.isSuccessful()){
//                          return Observable.error(new Exception("bitmap load failed"));
//                      }
//                      File srcFile = fileResponse.body();
//                      if (!FileUtils.move(srcFile,destFile)) {
//                          return Observable.error(new Exception("move image failed"));
//                      }
//                      return Observable.just(srcFile);
//                  })
//                  .retryWhen(RetryWithDelay.defaultDelay(TAG,url))
//                  .subscribeOn(Schedulers.io())
//                  .observeOn(AndroidSchedulers.mainThread())
//                  .doOnSubscribe(disposable -> {
//                      boolean b = FileUtils.deleteAllInDir(Path.getHeadPath());
//                      imageView.setImageBitmap(null);
//                      d("开始下载头像，删除旧头像：" + b);
//                  })
//                  .subscribe(fileConsumer, throwableConsumer, completeAction);
//      }

    public void loadImage(Context context, String url, ImageView imageView) throws Exception {
        String result = null;
        if (imageView == null || TextUtils.isEmpty(url)) {
            return;
        }



        // 截取文件信息，获取文件名
        String originalUrl = url;

// 找到"/attachFiles/"字符串的索引位置
        int index = originalUrl.indexOf("/attachFiles/");
        if (index != -1) {
            // 使用substring方法截取"/attachFiles/"之后的部分
            result = originalUrl.substring(index + "/attachFiles/".length());
            System.out.println(result);
        } else {
            System.out.println("未找到指定字符串");
        }


//        http://221.10.114.137:5012/joffice/datapush/downImagePhotoDown.do?path=2hISkvLvmPdBfuObjCH5Wi6kavNydfyxq7lrep8YCCW22y+6BHvQcTY1gXSpxFBQWyPFkSMWyJAxEY1RU3be3A==



        e(result);
        // 截取文件信息，获取文件名
//        String fileName = url.substring(url.lastIndexOf("/") + 1);
//        e(fileName);

        // 使用AES加密算法CBC模式加密文件名
        String encryptedFileName = Encrypt(result,"w5k5s6djsk552s5f");

        // 构建加密后的URL
        String encryptedUrl = "http://221.10.114.137:5012/joffice/datapush/downImagePhotoDown.do?path=" + encryptedFileName;

        Log.e(TAG, encryptedUrl);

        // 调用接口获取文件
        loadFile(context, encryptedUrl, imageView);
    }


    // 加密文件名
//    private String encryptFileName(String fileName) {
//        String encodedText = null;
//        try {
//            // 密钥
//            String key = "w5k5s6djsk552s5f";
//            byte[] keyBytes = key.getBytes("utf-8");
//            // 偏移向量
//            String iv = "lkdskf1ds131fsad";
//            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv.getBytes("utf-8"));
//
//            String text = fileName;
//
//            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
//            // AES加密/CBC模式/PKCS5Padding填充方式（默认）
//            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
//            // 初始化
//            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivParameterSpec);
//            byte[] encData = cipher.doFinal(text.getBytes("utf-8"));
//
//            // base64
//            Base64.Encoder encoder = null;
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                encoder = Base64.getEncoder();
//            }
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                encodedText = encoder.encodeToString(encData);
//            }
//
//            // 添加日志输出
//            Log.d(TAG, "Encoded Text: " + encodedText);
//        } catch (Exception e) {
//            e.printStackTrace();
//            // 添加异常日志输出
//            Log.e(TAG, "Error encrypting file name: " + e.getMessage());
//        }
//        return encodedText;
//    }
    public static String Encrypt(String sSrc, String sKey) throws Exception {
        if (sKey == null) {
            System.out.print("Key为空null");
            return null;
        }
        // 判断Key是否为16位
        if (sKey.length() != 16) {
            System.out.print("Key长度不是16位");
            return null;
        }
        byte[] raw = sKey.getBytes("utf-8");
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");//"算法/模式/补码方式"

        // 使用随机生成的初始向量
        String ivBytes = "lkdskf1ds131fsad";
        IvParameterSpec iv = new IvParameterSpec(ivBytes.getBytes("UTF-8"));

        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
        byte[] encrypted = cipher.doFinal(sSrc.getBytes());

        // 使用Base64类的静态方法进行编码
        return android.util.Base64.encodeToString(encrypted, Base64.DEFAULT);
    }

    // 调用接口获取文件
    private void loadFile(Context context, String url, ImageView imageView) {
        Consumer<File> fileConsumer = file -> Glide.with(context).asBitmap().load(file).into(imageView);
        Consumer<Throwable> throwableConsumer = throwable -> {
            // 在发生错误时的处理逻辑，您可以根据需要进行处理
        };
        Action completeAction = () -> {
            // 在图片加载完成时的处理逻辑，您可以根据需要进行处理
        };
        File destFile = new File(Path.getHeadPath(), FileUtils.getFileName(url));
        if (FileUtils.isFileExists(destFile)) {
            d("头像已存在，不下载");
            try {
                fileConsumer.accept(destFile);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }

        mImageUrl = url;
        cancelImage();
        imageDisposable = OkGo.<File>get(url).tag(url)
                .converter(new FileConvert())
                .adapt(new ObservableResponse<>())
                .flatMap((Function<Response<File>, ObservableSource<File>>) fileResponse -> {
                    if(!fileResponse.isSuccessful()){
                        return Observable.error(new Exception("bitmap load failed"));
                    }
                    File srcFile = fileResponse.body();
                    if (!FileUtils.move(srcFile,destFile)) {
                        return Observable.error(new Exception("move image failed"));
                    }
                    return Observable.just(srcFile);
                })
                .retryWhen(RetryWithDelay.defaultDelay(TAG,url))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(disposable -> {
                    boolean b = FileUtils.deleteAllInDir(Path.getHeadPath());
                    imageView.setImageBitmap(null);
                    d("开始下载头像，删除旧头像：" + b);
                })
                .subscribe(fileConsumer, throwableConsumer, completeAction);
    }






    private void d(String log){
        LogUtils.d(TAG,TAG + ":" + log);
    }

    private void e(String log){
        LogUtils.e(TAG,TAG + ":" + log);
    }
}
