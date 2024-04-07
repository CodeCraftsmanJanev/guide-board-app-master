package com.janev.chongqing_bus_app.serial;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;

import com.janev.chongqing_bus_app.system.Cache;
import com.janev.chongqing_bus_app.system.ZhuFuEnum;
import com.janev.chongqing_bus_app.utils.L;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ZhuFu {

    public static final String TAG = "ZhuFu";
    private static final int SERVER_PORT = 49152;
    private static NsdManager nsdManager;
    private static String ip;

    public static void initSerial(Context context) {
        int zhufupingType = Cache.getInt(Cache.Key.zhufuping, Cache.Default.zhufuping);
        ZhuFuEnum zhuFuEnum = ZhuFuEnum.get(zhufupingType);
        if (zhuFuEnum == null) {
            d("未选择主副屏或不可用[" + zhufupingType);
            return;
        }
        d("当前屏幕：" + zhuFuEnum.getName());

        // 初始化 NsdManager
        nsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);

        switch (zhuFuEnum) {
            case zhu:
                d("主");
                startServer();
                break;
            case fu:
                d("副");
                // 发现服务时调用，用于解析服务并获取 IP 地址
                nsdManager.discoverServices("_http._tcp.", NsdManager.PROTOCOL_DNS_SD, new NsdManager.DiscoveryListener() {
                    @Override
                    public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                        Log.e(TAG, "Discovery failed: " + errorCode);
                    }

                    @Override
                    public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                        Log.e(TAG, "Stop discovery failed: " + errorCode);
                    }

                    @Override
                    public void onDiscoveryStarted(String serviceType) {
                        Log.d(TAG, "Discovery started");
                    }

                    @Override
                    public void onDiscoveryStopped(String serviceType) {
                        Log.d(TAG, "Discovery stopped");
                    }

                    @Override
                    public void onServiceFound(NsdServiceInfo serviceInfo) {
                        Log.d(TAG, "Service found: " + serviceInfo);
                        if (serviceInfo.getServiceType().equals("_http._tcp.")) {
                            nsdManager.resolveService(serviceInfo, new NsdManager.ResolveListener() {
                                @Override
                                public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                                    Log.e(TAG, "Resolve failed: " + errorCode);
                                }

                                @Override
                                public void onServiceResolved(NsdServiceInfo serviceInfo) {
                                    Log.d(TAG, "Service resolved: " + serviceInfo);
                                    String ipAddress = serviceInfo.getHost().getHostAddress();
                                    ip = ipAddress;
                                    Log.d(TAG, "Device A's IP address: " + ipAddress);

                                    // 解析服务后，连接到主屏
                                    connectToServer();
                                }
                            });
                        }
                    }

                    @Override
                    public void onServiceLost(NsdServiceInfo serviceInfo) {
                        Log.d(TAG, "Service lost: " + serviceInfo);
                    }
                });
                break;
        }
    }

    private static void d(String log) {
        L.serialD(TAG, log);
    }

    private static void startServer() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
                    d("Server started, waiting for connections...");
                    // 等待客户端连接
                    Socket clientSocket = serverSocket.accept();
                    d("Client connected: " + clientSocket.getInetAddress());
                    // 向客户端发送消息

                    OutputStream outputStream = clientSocket.getOutputStream();
                    String message = "你好";
                    outputStream.write(message.getBytes());
                    outputStream.close();
                    // 关闭连接
                    clientSocket.close();
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private static void connectToServer() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // 连接到设备 A
                    Socket socket = new Socket(ip, SERVER_PORT);
                    d("Connected to server: " + ip);

                    // 从服务器接收消息
                    BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String serverResponse = reader.readLine();
                    d("Response from server: " + serverResponse);

                    // 关闭连接
                    reader.close();
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
