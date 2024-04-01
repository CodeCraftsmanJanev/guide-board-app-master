package com.janev.chongqing_bus_app.tcp.message;

import com.janev.chongqing_bus_app.utils.BytesUtils;

import java.nio.charset.Charset;

public class ScreenShotRequest extends IRequest{
    private final String msgSerial;
    private final byte TYPE;
    private final String fileID;
    private final String fileName;
    private final int replyResult;

    public ScreenShotRequest(String msgSerial,byte TYPE,String fileID,String fileName,int replyResult) {
        super("09");
        this.msgSerial = msgSerial;
        this.TYPE = TYPE;
        this.fileID = fileID;
        this.fileName = fileName;
        this.replyResult = replyResult;
    }

    @Override
    protected String getHexData() {
        String fileIDHex = BytesUtils.bytesToHex(fileID.getBytes(Charset.forName("GBK")));
        String fileNameHex = BytesUtils.bytesToHex(fileName.getBytes(Charset.forName("GBK")));
        return msgSerial
                + MessageUtils.getLength(fileIDHex) + fileIDHex
                + (TYPE == 0x00 || TYPE == 0x02 ? "00" : "01")
                + ("0" + replyResult)
                + MessageUtils.getLength(fileNameHex) + fileNameHex;
    }
}
