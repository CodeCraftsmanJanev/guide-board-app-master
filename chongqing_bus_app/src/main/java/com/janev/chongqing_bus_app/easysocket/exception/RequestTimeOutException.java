package com.janev.chongqing_bus_app.easysocket.exception;

/**
 * Author：Alex
 * Date：2019/6/4
 * Note：请求超时异常
 */
public class RequestTimeOutException extends Exception{

    public RequestTimeOutException(String s){
        super(s);
    }
}
