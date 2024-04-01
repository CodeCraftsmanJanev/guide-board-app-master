package com.janev.chongqing_bus_app.easysocket.interfaces.conn;

import com.janev.chongqing_bus_app.easysocket.connection.heartbeat.HeartManager;

/**
 * Author：Alex
 * Date：2019/12/8
 * Note：
 */
public interface IHeartManager {

    /**
     * 开始心跳
     */
    void startHeartbeat(HeartManager.HeartbeatListener listener);

    /**
     * 停止心跳
     */
    void stopHeartbeat();


    /**
     * 接收到心跳
     */
    void onReceiveHeartBeat();
}
