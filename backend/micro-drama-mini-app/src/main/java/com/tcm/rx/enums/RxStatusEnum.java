package com.tcm.rx.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 处方状态枚举
 * 对应数据库字段：status int(11)
 * 状态说明：0-待审核 1-待缴费 2-已缴费 3-已上传 4-已执行 5-已作废 6-已退费
 */
@Getter
@AllArgsConstructor
public enum RxStatusEnum {

    PENDING_REVIEW(0, "待审核"),
    PENDING_PAYMENT(1, "待缴费"),
    PAID(2, "已缴费"),
    UPLOADED(3, "已上传"),
    EXECUTED(4, "已执行"),
    INVALIDATED(5, "已作废"),
    REFUNDED(6, "已退费");

    /**
     * 状态编码（与数据库存储值一致）
     */
    private final int code;

    /**
     * 状态描述（用于前端展示或日志说明）
     */
    private final String desc;

    public static final Map<Integer, String> CODE_MAP = new HashMap<>();
    public static final Map<String, Integer> NAME_MAP = new HashMap<>();
    public static final Map<Integer, RxStatusEnum> CODE_MAP1 = new HashMap<>();

    static {
        for (RxStatusEnum e : RxStatusEnum.values()) {
            CODE_MAP.put(e.getCode(), e.getDesc());
            NAME_MAP.put(e.getDesc(), e.getCode());
            CODE_MAP1.put(e.getCode(), e);
        }
    }
}