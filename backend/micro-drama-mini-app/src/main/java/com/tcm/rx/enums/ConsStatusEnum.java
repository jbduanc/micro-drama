package com.tcm.rx.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Getter
public enum ConsStatusEnum {
    DRAFT(0, "草稿"),
    PENDING(1, "待会诊"),
    COMPLETED(2, "会诊结束");

    private final Integer code;
    private final String desc;

    // 编码→描述映射（用于VO转换）
    public static final Map<Integer, String> CODE_MAP = new HashMap<>();

    static {
        Arrays.stream(values()).forEach(enumItem -> CODE_MAP.put(enumItem.code, enumItem.desc));
    }

    ConsStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
