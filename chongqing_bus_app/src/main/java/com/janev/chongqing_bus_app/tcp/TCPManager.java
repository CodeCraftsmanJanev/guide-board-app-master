package com.janev.chongqing_bus_app.tcp;

import android.app.Service;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.blankj.utilcode.util.ServiceUtils;
import com.blankj.utilcode.util.UiMessageUtils;
import com.janev.chongqing_bus_app.system.UiEvent;
import com.janev.chongqing_bus_app.utils.L;

public class TCPManager implements ServiceConnection{
    private static final String TAG = "TCPManager";

    private static final class Holder {
        public static final TCPManager INSTANCE = new TCPManager();
    }

    private TCPManager() {
    }

    public static TCPManager getInstance(){
        return Holder.INSTANCE;
    }

    public void openClient(){
        close();
        ServiceUtils.bindService(TCPClientService.class,this, Service.BIND_AUTO_CREATE);

        UiMessageUtils.getInstance().send(UiEvent.UPDATE_RESOURCE);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        d( "onServiceConnected: " + name.getClassName());
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        d( "onServiceDisconnected: " + name.getClassName());
    }

    public void close(){
        if(ServiceUtils.isServiceRunning(TCPClientService.class)){
            ServiceUtils.unbindService(this);

        }
    }

    private static void d(String log){
        L.tcp(TAG,log);
    }
}
