package com.janev.chongqing_bus_app.tcp;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.UiMessageUtils;
import com.janev.chongqing_bus_app.system.UiEvent;
import com.janev.chongqing_bus_app.tcp.client.ChongqingV2Client;
import com.janev.chongqing_bus_app.tcp.client.TCPClient;
import com.janev.chongqing_bus_app.tcp.message.CrashReportRequest;
import com.janev.chongqing_bus_app.utils.BytesUtils;
import com.janev.chongqing_bus_app.utils.L;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class TCPClientService extends Service {
    private static final String TAG = "TCPClient";
    private TCPClient tcpClient;

    @Override
    public void onCreate() {
        super.onCreate();
        d( "onCreate: ");
        UiMessageUtils.getInstance().addListener(1111, new UiMessageUtils.UiMessageCallback() {
            @Override
            public void handleMessage(@NonNull UiMessageUtils.UiMessage localMessage) {
//                String s = (String) localMessage.getObject();
//                String s = "2828860022012000000100000000000051a800740500000000000000003103312e30536674703a2f2f3132312e352e3131312e3135302f4164766572745265736f757263655061636b6167652f39643132653166382d666164662d343232352d386533642d3535346530663361393361312e6a736f6e0766747075736572096d6d5f313233343536300c";
//                byte[] bytes = BytesUtils.hexToByteArray(s);
//                tcpClient.onSocketResponse(null,bytes);

                new CrashReportRequest(CrashReportRequest.LCD_ERROR,"LCD屏不亮",CrashReportRequest.STATUS_ING).send();
            }
        });
        UiMessageUtils.getInstance().addListener(1112, new UiMessageUtils.UiMessageCallback() {
            @Override
            public void handleMessage(@NonNull UiMessageUtils.UiMessage localMessage) {
//                String s = (String) localMessage.getObject();
//                String s = "2828860022012000000100000000000051a800740500000000000000003103312e30536674703a2f2f3132312e352e3131312e3135302f4164766572745265736f757263655061636b6167652f39643132653166382d666164662d343232352d386533642d3535346530663361393361312e6a736f6e0766747075736572096d6d5f313233343536300c";
//                byte[] bytes = BytesUtils.hexToByteArray(s);
//                tcpClient.onSocketResponse(null,bytes);

                new CrashReportRequest(CrashReportRequest.LCD_ERROR,"LCD屏不亮",CrashReportRequest.STATUS_FINISH).send();
            }
        });

//        UiMessageUtils.getInstance().addListener(UiEvent.EVENT_CONNECT_TCP, connectTcpEventCallback);
        initTcpClient();
    }

    private Disposable delayConnectDisposable;
    private final UiMessageUtils.UiMessageCallback connectTcpEventCallback = localMessage -> {
        if(tcpClient != null){
            disconnect();
            if(delayConnectDisposable != null && !delayConnectDisposable.isDisposed()){
                delayConnectDisposable.dispose();
                delayConnectDisposable = null;
            }
            delayConnectDisposable = Observable.timer(1, TimeUnit.MINUTES)
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(aLong -> initTcpClient());
        } else {
            initTcpClient();
        }
    };

    private void initTcpClient(){
        tcpClient = new ChongqingV2Client();
        tcpClient.connect();
    }

    private void disconnect(){
        if(tcpClient != null){
            tcpClient.destroy();
            tcpClient = null;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        d( "onBind: ");
        return new TCPBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        d( "onUnbind: ");
        return super.onUnbind(intent);
    }

    public class TCPBinder extends Binder {
        public TCPClientService getService(){
            return TCPClientService.this;
        }
    }

    @Override
    public void onDestroy() {
        d( "onDestroy: ");
        disconnect();
        super.onDestroy();
    }
    
    private void d(String log){
        L.tcp(TAG,log);
    }
}
