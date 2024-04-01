package com.janev.chongqing_bus_app.easysocket.interfaces.conn;

import com.janev.chongqing_bus_app.easysocket.interfaces.conn.ISocketActionListener;

/**
 * Author：Alex
 * Date：2019/6/1
 * Note：订阅监听socket
 */
public interface ISubscribeSocketAction {
    /**
     * 注册监听socket的行为
     * @param iSocketActionListener
     */
    void subscribeSocketAction(com.janev.chongqing_bus_app.easysocket.interfaces.conn.ISocketActionListener iSocketActionListener);

    /**
     * 注销监听socket的行为
     * @param iSocketActionListener
     */
    void unSubscribeSocketAction(ISocketActionListener iSocketActionListener);
}
