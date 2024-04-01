package com.janev.chongqing_bus_app.tcp.client;

import java.nio.ByteBuffer;

public abstract class DataResolver {

    public DataResolver() {
    }

    public abstract void resolve(byte[] bytes,OnDataCallback onDataCallback);

    public interface OnDataCallback{
        void resolveCallback(byte order, String msgSerial, ByteBuffer dataBuffer);
    }

}
