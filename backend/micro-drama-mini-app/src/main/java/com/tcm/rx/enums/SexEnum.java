package com.tcm.rx.enums;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public enum SexEnum {
    // 男，对应编码0
    MALE(0,"男"),
    // 女，对应编码1
    FEMALE(1,"女"),
    // 未知，对应编码2
    UNKNOWN(2,"未知");

    // 性别对应的编码值
    private final int code;

    private final String name;

    SexEnum(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public static final Map<Integer, String> CODE_MAP = new HashMap<>();
    static {
        for (SexEnum e : SexEnum.values()) {
            CODE_MAP.put(e.getCode(), e.getName());
        }
    }
}
