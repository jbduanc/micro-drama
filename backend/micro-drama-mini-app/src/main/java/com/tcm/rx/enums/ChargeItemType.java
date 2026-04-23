package com.tcm.rx.enums;

import java.util.HashMap;
import java.util.Map;

public enum ChargeItemType {
    APPROPRIATE_TECHNOLOGY(0, "适宜技术"),
    DECOCTION_FEE(1, "煎药费");

    private final int code;
    private final String description;

    ChargeItemType(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static final Map<Integer, String> CODE_MAP = new HashMap<>();
    public static final Map<String, Integer> NAME_MAP = new HashMap<>();

    static {
        for (ChargeItemType e : ChargeItemType.values()) {
            CODE_MAP.put(e.getCode(), e.getDescription());
            NAME_MAP.put(e.getDescription(), e.getCode());
        }
    }
}
