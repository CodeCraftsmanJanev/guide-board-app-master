package com.janev.chongqing_bus_app.tcp.message;

public class ReplyRequest extends IRequest{
    public static final String SUCCESS = "00";
    public static final String FAILED = "01";
    public static final String NOT_SUPPORT = "02";

    private final String responseOrderHex;
    private final String messageSerialHex;
    private final String resultHex;
    public ReplyRequest(String responseOrderHex, String messageSerialHex,String resultHex) {
        super("01");
        this.responseOrderHex = responseOrderHex;
        this.messageSerialHex = messageSerialHex;
        this.resultHex = resultHex;
    }

    @Override
    protected String getTag() {
        return "发送回复";
    }

    @Override
    protected String getHexData() {
        return responseOrderHex + messageSerialHex + resultHex;
    }
}
