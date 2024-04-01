package com.janev.chongqing_bus_app.ui;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.input.InputManager;
import android.hardware.usb.UsbConfiguration;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class KeyboardHandler extends BroadcastReceiver {
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private UsbManager usbManager;

    private Context context;

    private static final class Holder {
        public static final KeyboardHandler INSTANCE = new KeyboardHandler();
    }

    private KeyboardHandler(){}

    public static KeyboardHandler getInstance(){
        return Holder.INSTANCE;
    }

    public void init(Context context){
        this.context = context;

        usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);

        IntentFilter intent = new IntentFilter(ACTION_USB_PERMISSION);
        this.context.registerReceiver(this,intent);

        UsbDevice usbDevice = checkUsbDevice();
        setUsbDevice(usbDevice);
    }

    public void unInit(){
        stop();
        this.context.unregisterReceiver(this);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (ACTION_USB_PERMISSION.equals(action)) {
            synchronized (this) {
                UsbDevice usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    if (usbDevice != null) {
                        Log.d(TAG, "onReceive: " + usbDevice.toString());
                        // 在此处执行您的操作
                        if (isKeyBroad(usbDevice)) {
                            setUsbDevice(usbDevice);
                        }
                    }
                } else {
                        // 用户拒绝了USB设备权限请求
                }
            }
        }
    }

    private UsbDevice checkUsbDevice(){
        if(usbManager != null){
            HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
            if(deviceList != null){
                Log.d(TAG, "checkUsbDevice: " + deviceList.size());
                Collection<UsbDevice> values = deviceList.values();
                for (UsbDevice usbDevice : values) {
                    if (isKeyBroad(usbDevice)) {
                        Log.d(TAG, "checkUsbDevice: " + usbDevice.toString());
                        return usbDevice;
                    }
                }
            }
        }
        return null;
    }

    private UsbDevice usbDevice;

    public void setUsbDevice(UsbDevice usbDevice) {
        this.usbDevice = usbDevice;
        if(hasPermission(this.usbDevice)){
            Log.d(TAG, "setUsbDevice: 有权限");
            openDevice(this.usbDevice);
        } else {
            Log.d(TAG, "setUsbDevice: 申请权限");
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, new Intent(ACTION_USB_PERMISSION), 0);
            usbManager.requestPermission(usbDevice,pendingIntent);
        }
    }

    private boolean isKeyBroad(UsbDevice usbDevice){
        int interfaceCount = usbDevice.getInterfaceCount();
        for (int i = 0; i < interfaceCount; i++) {
            UsbInterface anInterface = usbDevice.getInterface(i);
            int interfaceClass = anInterface.getInterfaceClass();
            if(interfaceClass == UsbConstants.USB_CLASS_HID){
                // 设备类别为HID（Human Interface Device），可能是键盘
                // 进一步判断设备的子类别和协议
                int interfaceSubclass = anInterface.getInterfaceSubclass();
                int interfaceProtocol = anInterface.getInterfaceProtocol();
                if (interfaceSubclass == 1 && interfaceProtocol == 1) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean hasPermission(UsbDevice usbDevice){
        return usbManager.hasPermission(usbDevice);
    }

    private final AtomicBoolean runningAtomic = new AtomicBoolean(false);
    public void stop(){
        runningAtomic.set(false);
        if(disposable != null && !disposable.isDisposed()){
            disposable.dispose();
            disposable = null;
        }
    }

    private static final String TAG = "KeyboardHandler";
    private Disposable disposable;
    private void openDevice(UsbDevice usbDevice){
        stop();
        disposable = Observable
                .create(emitter -> {

                    UsbDeviceConnection usbDeviceConnection = usbManager.openDevice(usbDevice);
                    if (usbDeviceConnection != null) {
                        Log.d(TAG, "openDevice: 启动连接：" + usbDeviceConnection.toString());
                        // 获取USB设备的接口
                        UsbInterface usbInterface = usbDevice.getInterface(0);
                        if (usbInterface != null) {
                            Log.d(TAG, "openDevice: 接口：" + usbInterface.toString());
                            usbDeviceConnection.claimInterface(usbInterface, true);
                            UsbEndpoint endpoint = usbInterface.getEndpoint(0);
                            if (endpoint != null) {
                                Log.d(TAG, "openDevice: 端口：" + endpoint.toString());
                                byte[] buffer = new byte[8];
                                while (runningAtomic.get()) {
                                    int bytesRead = usbDeviceConnection.bulkTransfer(endpoint, buffer, buffer.length, 1000);
                                    if(bytesRead > 0){
                                        // 解析按键状态
                                        for (int i = 2; i < buffer.length; i++) {
                                            byte keyState = buffer[i];
                                            int keyCode = keyState & 0x7F; // 低7位表示键码
                                            boolean isKeyDown = (keyState & 0x80) != 0; // 高位表示按键是否被按下

                                            // 创建对应的键盘事件
                                            int action = isKeyDown ? KeyEvent.ACTION_DOWN : KeyEvent.ACTION_UP;
                                            KeyEvent keyEvent = new KeyEvent(action, keyCode);

                                            String s = KeyEvent.keyCodeToString(keyCode);
                                            Log.d(TAG, "openDevice: " + s);
                                            // 处理键盘事件
                                        }
                                        Log.d(TAG, "run: " + Arrays.toString(buffer));
                                    }
                                }
                            }
                            usbDeviceConnection.releaseInterface(usbInterface);
                        }
                        usbDeviceConnection.close();
                    }

                    emitter.onComplete();
                })
                .doOnSubscribe(disposable1 -> runningAtomic.set(true))
                .doOnTerminate(() -> runningAtomic.set(false))
                .doOnDispose(() -> runningAtomic.set(false))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(

                );

    }
}
