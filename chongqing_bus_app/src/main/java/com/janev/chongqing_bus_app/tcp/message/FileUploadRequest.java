package com.janev.chongqing_bus_app.tcp.message;

import com.janev.chongqing_bus_app.utils.BytesUtils;
import com.janev.chongqing_bus_app.utils.StringUtils;

import java.nio.charset.Charset;

public class FileUploadRequest extends IRequest{

    private final String msgSerial;
    private final byte fileType;
    private final String fileID;
    private final String fileName;
    private final String result;

    public FileUploadRequest(String msgSerial,byte fileType,String fileID,String fileName,String result) {
        super("0A");
        this.msgSerial = msgSerial;
        this.fileType = fileType;
        this.fileID = fileID;
        this.fileName = fileName;
        this.result = result;
    }

    @Override
    protected String getHexData() {
        String msgSerialHex = msgSerial;

        String fileTypeHex = BytesUtils.byteToHex(fileType);

        String fileIDHex = BytesUtils.bytesToHex(fileID.getBytes(Charset.forName("GBK")));

        String fileNameHex = BytesUtils.bytesToHex(fileName.getBytes(Charset.forName("GBK")));

        return msgSerialHex
                + fileTypeHex
                + MessageUtils.getLength(fileIDHex) + fileIDHex
                + result
                + MessageUtils.getLength(fileNameHex) + fileNameHex;
    }
}
