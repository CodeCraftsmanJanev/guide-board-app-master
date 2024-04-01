package com.janev.chongqing_bus_app.tcp.task.app;

import android.text.TextUtils;
import android.util.Log;

import com.blankj.utilcode.util.UiMessageUtils;
import com.janev.chongqing_bus_app.system.UiEvent;
import com.janev.chongqing_bus_app.tcp.message.MessageUtils;
import com.janev.chongqing_bus_app.tcp.task.downloader.FtpDownloader;
import com.janev.chongqing_bus_app.utils.StringUtils;

import java.io.File;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class UpgradeAppManager2 {
    private static final String TAG = "UpgradeManager2";
    private static final class Holder{
        public static final UpgradeAppManager2 INSTANCE = new UpgradeAppManager2();
    }

    public static UpgradeAppManager2 getInstance(){
        return UpgradeAppManager2.Holder.INSTANCE;
    }

    public void check(String msgSerial,boolean forceUpgrade,String newResourceId,String newVersionHex,String ftpAddress,String ftpUserName,String ftpPassword) {
        String newVersionName = StringUtils.hexStringToString(newVersionHex);
        String cacheVersionName = MessageUtils.getAppVersionName();
        d("消息序列：" + msgSerial);
        d("强制更新：" + forceUpgrade);
        d("远程资源Id：" + newResourceId);
        d("远程资源版本：" + newVersionName);
        d("本地App版本：" + cacheVersionName);
        d("ftp地址：" + ftpAddress);
        d("ftp用户名：" + ftpUserName);
        d("ftp密码：" + ftpPassword);

        int compareResult = compareVersion(newVersionName, cacheVersionName);
        if(compareResult == 1 || forceUpgrade){
            d("checkResource: 当前版本旧或强制更新");
            download(msgSerial,newResourceId,ftpAddress,ftpUserName,ftpPassword);
        } else {
            d("checkResource: 当前是最新版本");
        }
    }

    private Disposable disposable;
    private void download(String msgSerial, String newResourceId, String ftpAddress, String ftpUserName, String ftpPassword) {
        if(disposable != null && !disposable.isDisposed()){
            return;
        }
        disposable = new FtpDownloader(ftpAddress,ftpUserName,ftpPassword,"")
                .setListener(new AppDownloadListener(msgSerial,newResourceId,ftpAddress))
                .download()
                .subscribeOn(Schedulers.io())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        file -> {
                            Log.d(TAG, "处理完成：" + file.getPath());
                            install(file);
                        },
                        throwable -> Log.e(TAG, "下载异常：", throwable)
                );
    }

    private void install(File file){
        d("正在安装");
        UiMessageUtils.getInstance().send(UiEvent.EVENT_UPGRADE_APP,file.getPath());
    }

    private void d(String l){
        Log.d(TAG, l);
    }
    public int compareVersion(String version1, String version2) {
        if(TextUtils.isEmpty(version1) && TextUtils.isEmpty(version2)){
            return 0;
        } else if(TextUtils.isEmpty(version1)){
            return -1;
        } else if(TextUtils.isEmpty(version2)){
            return 1;
        }

        int size1 = version1.length(), size2 = version2.length();
        int size = Math.max(size1, size2);//选择两者中长的那个来做为停止条件
        for(int idx1 = 0,idx2 = 0;idx1<size || idx2<size;++idx1,++idx2)
        {//每次开始新的循环的时候，两者都会清0，因为我们只要当前位置的数字，
            //以前的没必要，反正比较过了
            int num1 = 0, num2 = 0;
            //下面两个while就用来转换成数字，而且就算段数不够，也会因为上面的赋值而补成0
            while(idx1<size1 && version1.charAt(idx1) != '.') num1 = num1*10+version1.charAt(idx1++)-'0';
            while(idx2<size2 && version2.charAt(idx2) != '.') num2 = num2*10+version2.charAt(idx2++)-'0';
            if(num1 > num2)return 1;//只要有一段不相等，就可以return了
            else if(num1 < num2)return -1;
        }
        return 0;//只有全部段都相等，才是真正的相等，所以得在转换完后才能知道
    }
}
