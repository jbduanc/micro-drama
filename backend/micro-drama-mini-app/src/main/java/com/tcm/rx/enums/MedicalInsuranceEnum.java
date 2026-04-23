package com.tcm.rx.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * 是否支持医保
 */
public enum MedicalInsuranceEnum {
    SUPPORTED(0, "是"),
    UNSUPPORTED(1, "否"),
    NOT_PAYABLE_ALONE(2, "单独使用不予支付");

    private final int code;
    private final String description;

    MedicalInsuranceEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }

    // Getter方法
    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }


    public static final Map<Integer, String> CODE_MAP = new HashMap<>();
    public static final Map<String, Integer> NAME_MAP = new HashMap<>();

    static {
        for (MedicalInsuranceEnum e : MedicalInsuranceEnum.values()) {
            CODE_MAP.put(e.getCode(), e.getDescription());
            NAME_MAP.put(e.getDescription(), e.getCode());
        }
    }

}
