package com.tcm.rx.enums;

/**
 * 饮片类型
 */
public enum HerbTypeEnum {

    TRACEABILITY("溯源"),
    SMALL_PACKAGE("小包装"),
    SELECTED_GOODS("选货"),
    CHINESE_HERBAL_MEDICINE("中草药");

    private final String description;

    HerbTypeEnum(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
