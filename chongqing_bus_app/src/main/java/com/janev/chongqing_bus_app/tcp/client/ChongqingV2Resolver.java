package com.janev.chongqing_bus_app.tcp.client;

import com.janev.chongqing_bus_app.tcp.message.MessageUtils;

import java.nio.ByteBuffer;

public class ChongqingV2Resolver extends DataResolver{

    @Override
    public void resolve(byte[] bodyBytes, OnDataCallback onDataCallback) {
        try {
            ByteBuffer byteBuffer = ByteBuffer.wrap(bodyBytes);

            String start = MessageUtils.getBytesHex(byteBuffer, 2);

            byte msgID = byteBuffer.get();

            String byteHex = MessageUtils.getByteHex(byteBuffer);

            String deviceNumber = MessageUtils.getBytesHex(byteBuffer, 6);

            String terminalNumber = MessageUtils.getBytesHex(byteBuffer, 6);

            String msgSerial = MessageUtils.getBytesHex(byteBuffer, 2);

            int msgLength = MessageUtils.getBytesInt(byteBuffer, 2);

            byte[] bytes = new byte[msgLength];
            byteBuffer.get(bytes);

            String crc = MessageUtils.getByteHex(byteBuffer);

            String end = MessageUtils.getByteHex(byteBuffer);

            if(onDataCallback != null){
                onDataCallback.resolveCallback(msgID,msgSerial,ByteBuffer.wrap(bytes));
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
