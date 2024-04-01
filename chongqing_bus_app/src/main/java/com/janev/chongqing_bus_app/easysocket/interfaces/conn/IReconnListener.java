package com.janev.chongqing_bus_app.easysocket.interfaces.conn;

import com.janev.chongqing_bus_app.easysocket.interfaces.conn.IConnectionManager;

/**
 * Author：Alex
 * Date：2019/6/1
 * Note：
 */
public interface IReconnListener {

    /**
     * 关联连接器
     * @param iConnectionManager
     */
    void attach(IConnectionManager iConnectionManager);

    /**
     * 分离连接器
     */
    void detach();
}
