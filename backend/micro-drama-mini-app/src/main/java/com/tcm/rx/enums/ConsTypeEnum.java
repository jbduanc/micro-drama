package com.tcm.rx.enums;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public enum ConsTypeEnum {
    REMOTE(0, "远程会诊"),
    RX(1, "处方会诊");

    private final Integer code;
    private final String desc;
    public static final Map<Integer, String> CODE_MAP = new HashMap<>();

    static {
        for (ConsTypeEnum e : ConsTypeEnum.values()) {
            CODE_MAP.put(e.getCode(), e.getDesc());
        }
    }

    ConsTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
