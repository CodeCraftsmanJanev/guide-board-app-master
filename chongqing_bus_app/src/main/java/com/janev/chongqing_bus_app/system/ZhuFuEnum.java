package com.janev.chongqing_bus_app.system;

import java.util.ArrayList;
import java.util.List;

public enum ZhuFuEnum {
    zhu("主屏"),
    fu("副屏");
    private final String name;

    public String getName() {
        return name;
    }
    ZhuFuEnum(String name) {
        this.name = name;
    }

    public static List<String> toNameList(){
        List<String> list = new ArrayList<>();
        ZhuFuEnum[] values = ZhuFuEnum.values();
        for (ZhuFuEnum value : values) {
            list.add(value.name);
        }
        return list;
    }
    public static ZhuFuEnum get(int ordinal){
        ZhuFuEnum[] values = ZhuFuEnum.values();
        for (ZhuFuEnum value : values) {
            if (value.ordinal() == ordinal) {
                return value;
            }
        }
        return null;
    }
}
