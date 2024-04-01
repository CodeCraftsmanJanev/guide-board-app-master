package com.janev.chongqing_bus_app.easysocket.interfaces.conn;

import com.janev.chongqing_bus_app.easysocket.entity.basemsg.SuperCallbackSender;
import com.janev.chongqing_bus_app.easysocket.interfaces.conn.IConnectionManager;

/**
 * Author：Alex
 * Date：2019/6/5
 * Note：发送接口
 */
public interface ISend {

    /**
     * 发送一个有回调的消息
     * @param sender
     * @return
     */
    IConnectionManager upCallbackMessage(SuperCallbackSender sender);

    /**
     * 发送bytes
     * @param bytes
     * @return
     */
    IConnectionManager upBytes(byte[] bytes);
}
