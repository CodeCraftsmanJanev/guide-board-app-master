package com.janev.chongqing_bus_app.serial;

import android.app.Service;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.blankj.utilcode.util.ServiceUtils;
import com.janev.chongqing_bus_app.utils.L;

public class SerialPortManager {

    public static void bindService(){
        if(!ServiceUtils.isServiceRunning(SerialPortService.class)){
            ServiceUtils.bindService(SerialPortService.class, serviceConnection, Service.BIND_AUTO_CREATE);
        }
    }

    public static void unBindService(){
        if(ServiceUtils.isServiceRunning(SerialPortService.class)){
            ServiceUtils.unbindService(serviceConnection);
        }
    }

    private static final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder iBinder) {
            SerialPortService.MyBinder myBinder = (SerialPortService.MyBinder)iBinder;
            SerialPortService service = myBinder.getService();
            d("onServiceConnected: " + name.getPackageName());
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            d("onServiceDisconnected: " + name.getPackageName());
        }
    };

    private static final String TAG = "SerialPortManager";
    private static void d(String log){
        L.serialD(TAG,log);
    }
}
