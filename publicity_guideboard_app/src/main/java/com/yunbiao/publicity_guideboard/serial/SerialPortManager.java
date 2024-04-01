package com.yunbiao.publicity_guideboard.serial;

import android.app.Service;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.blankj.utilcode.util.ServiceUtils;

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
            Log.d(SerialPortService.TAG, "onServiceConnected: " + name.getPackageName());
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(SerialPortService.TAG, "onServiceDisconnected: " + name.getPackageName());
        }
    };

}
