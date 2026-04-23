package com.tcm.rx.enums;

/**
 * 饮片单位
 */
public enum HerbUnitEnum {

    UNIT_G("g"),
    PACKAGE("包"),
    BAG("袋"),
    LITER("升"),
    STRIP("条"),
    BOTTLE("瓶"),
    PAIR("对"),
    PIECE("个"),
    MILLILITER("ml"),
    GRAM("克");

    private final String unit;

    HerbUnitEnum(String unit) {
        this.unit = unit;
    }

    public String getUnit() {
        return unit;
    }
}
