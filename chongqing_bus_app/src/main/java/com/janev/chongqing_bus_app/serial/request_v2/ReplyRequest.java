package com.janev.chongqing_bus_app.serial.request_v2;

import androidx.annotation.NonNull;

import com.janev.chongqing_bus_app.tcp.message.MessageUtils;
import com.janev.chongqing_bus_app.utils.BytesUtils;

public class ReplyRequest extends ISerialRequest{
    public static final String SUCCESS = "00";
    public static final String FAILED = "01";
    private final byte order;
    private final String result;
    public ReplyRequest(byte order,String result,@NonNull GetHelperListener getHelperListener) {
        super("80", getHelperListener);
        this.order = order;
        this.result = result;
    }

    @Override
    protected String getContentHex() {
        return MessageUtils.getDeviceAddressHex() + BytesUtils.byteToHex(order) + result;
    }
}
