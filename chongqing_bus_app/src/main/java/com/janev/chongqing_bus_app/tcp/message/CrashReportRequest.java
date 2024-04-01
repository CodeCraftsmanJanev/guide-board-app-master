package com.janev.chongqing_bus_app.tcp.message;

import com.blankj.utilcode.util.ConvertUtils;
import com.janev.chongqing_bus_app.utils.BytesUtils;
import com.janev.chongqing_bus_app.utils.StringUtils;

import java.nio.charset.Charset;

public class CrashReportRequest extends IRequest{
    public static final String LCD_ERROR = "0001";
    public static final String SERIAL_ERROR = "0001";

    public static final String STATUS_ING = "00";
    public static final String STATUS_FINISH = "01";

    private final String errorType;
    private final String errorContent;
    private final String errorStatus;
    public CrashReportRequest(String errorType,String errorContent,String status) {
        super("0B");
        this.errorType = errorType;
        this.errorContent = errorContent;
        this.errorStatus = status;
    }

    @Override
    protected String getTag() {
        return "异常上报";
    }

    @Override
    protected String getHexData() {
        String errType = MessageUtils.addZero(errorType, 4);
        d("异常类型：" + errType);

        String errState = MessageUtils.addZero(errorStatus, 2);
        d("异常状态：" + errState);

        String errHex = ConvertUtils.bytes2HexString(errorContent.getBytes(Charset.forName("GBK")));

        String length = MessageUtils.getLength(errHex);
        d("异常信息长度：" + length);

        d("异常信息：" + errHex);

        return errType + errState + length + errHex;
    }
}
