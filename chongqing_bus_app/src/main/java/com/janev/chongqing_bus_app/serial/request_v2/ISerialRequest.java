package com.janev.chongqing_bus_app.serial.request_v2;

import androidx.annotation.NonNull;

import com.janev.chongqing_bus_app.tcp.message.MessageUtils;

import tp.xmaihh.serialport.SerialHelper;

public abstract class ISerialRequest {
    public static final String START = "2828";
    public static final String END = "0C";
    private String messageId;
    protected final QueryLineRequest.GetHelperListener getHelperListener;

    public ISerialRequest(@NonNull String messageId,@NonNull QueryLineRequest.GetHelperListener getHelperListener) {
        this.messageId = messageId;
        this.getHelperListener = getHelperListener;
    }

    private String generateHex(){
        String START_HEX = START
                + messageId
                + MessageUtils.getDeviceAddressHex();

        String CONTENT_HEX = getContentHex();

        String LENGTH_HEX = MessageUtils.getLength(CONTENT_HEX,4);

        String HEX = START_HEX + LENGTH_HEX + CONTENT_HEX;

        String BCC_HEX = MessageUtils.getBCC(HEX);

        return HEX + BCC_HEX + END;
    }

    protected abstract String getContentHex();


    public void send(){
        String hex = generateHex();

        SerialHelper serialHelper = getHelperListener.get();
        if(serialHelper != null){
            serialHelper.sendHex(hex);
        }
    }

    public interface GetHelperListener{
        SerialHelper get();
    }
}
