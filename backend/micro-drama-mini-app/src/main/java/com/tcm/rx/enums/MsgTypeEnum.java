package com.tcm.rx.enums;

import lombok.Data;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public enum MsgTypeEnum {
    RECEIVE_CONSULTATION("1", "接收会诊"),
    CONSULTATION_RESULT("2", "会诊结果");

    // 类型值
    private final String code;
    // 类型描述
    private final String desc;

    // 构造方法
    MsgTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static final Map<String, String> CODE_MAP = new HashMap<>();
    static {
        for (MsgTypeEnum e : MsgTypeEnum.values()) {
            CODE_MAP.put(e.getCode(), e.getDesc());
        }
    }
}
