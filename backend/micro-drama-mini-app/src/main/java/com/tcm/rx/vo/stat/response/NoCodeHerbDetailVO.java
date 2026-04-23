package com.tcm.rx.vo.stat.response;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 无编码饮片明细VO
 */
@Data
public class NoCodeHerbDetailVO implements Serializable {

    private static final long serialVersionUID = -4026001812129319630L;

    /**
     * 方剂名称
     */
    private String name;

    /**
     * 方剂类型
     */
    private Integer type;

    /**
     * 饮片名称
     */
    private String herbName;

    /**
     * 饮片用量
     */
    private BigDecimal herbDosage;
}
