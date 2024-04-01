package com.janev.chongqing_bus_app.easysocket.interfaces.config;

import com.janev.chongqing_bus_app.easysocket.entity.SocketAddress;
import com.janev.chongqing_bus_app.easysocket.interfaces.conn.IConnectionManager;

/**
 * Author：Alex
 * Date：2019/6/4
 * Note：
 */
public interface IConnectionSwitchListener {
    void onSwitchConnectionInfo(IConnectionManager manager, SocketAddress oldAddress, SocketAddress newAddress);
}
