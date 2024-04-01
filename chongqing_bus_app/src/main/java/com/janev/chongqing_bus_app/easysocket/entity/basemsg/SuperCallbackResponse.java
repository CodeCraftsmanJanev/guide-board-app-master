package com.janev.chongqing_bus_app.easysocket.entity.basemsg;

import com.janev.chongqing_bus_app.easysocket.entity.basemsg.IResponse;

/**
 * Author：Alex
 * Date：2019/12/7
 */
public abstract class SuperCallbackResponse implements IResponse {

    public abstract String getCallbackId();

    public abstract void setCallbackId(String callbackId);

}
