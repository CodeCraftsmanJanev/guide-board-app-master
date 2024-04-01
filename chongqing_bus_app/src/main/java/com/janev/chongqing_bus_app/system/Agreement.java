package com.janev.chongqing_bus_app.system;

import java.util.ArrayList;
import java.util.List;

public enum Agreement{
    CHONGQING_V1("重庆公交V1","/dev/ttyS2",9600,8,1),
    CHONGQING_V2("重庆公交V2","/dev/ttyS2",9600,8,1),
    CHONGQING_V1_1("重庆公交V1_串口3","/dev/ttyS3",9600,8,1);
    private final String name;
    private final String portPath;
    private final int baudRate;
    private final int dataBits;
    private final int stopBits;
    Agreement(String name,String portPath,int baudRate,int dataBits,int stopBits) {
        this.name = name;
        this.portPath = portPath;
        this.baudRate = baudRate;
        this.dataBits = dataBits;
        this.stopBits = stopBits;
    }

    public String getName() {
        return name;
    }

    public String getPortPath() {
        return portPath;
    }

    public int getBaudRate() {
        return baudRate;
    }

    public int getDataBits() {
        return dataBits;
    }

    public int getStopBits() {
        return stopBits;
    }

    public static List<String> toNameList(){
        List<String> list = new ArrayList<>();
        Agreement[] values = Agreement.values();
        for (Agreement value : values) {
            list.add(value.name);
        }
        return list;
    }

    public static Agreement get(int ordinal){
        Agreement[] values = Agreement.values();
        for (Agreement value : values) {
            if (value.ordinal() == ordinal) {
                return value;
            }
        }
        return null;
    }
}