package com.tcm.rx.feign.herb.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * 煎药中心系统--煎药饮片管理表
 * </p>
 *
 * @author shouhan
 * @since 2025-06-23
 */
@Data
public class CustomerHerbVO implements Serializable {

    private static final long serialVersionUID = -2210353265333238197L;

    /**
     * 所属医联体的code
     */
    private String medicalGroupCode;

    /**
     * 客户编码
     */
    private String customerCode;

    /**
     * 客户饮片
     */
    private String customerHerbCode;

    /**
     * 煎药饮片
     */
    private String herbCode;

    /**
     * 换算系数
     */
    private BigDecimal conversionFactor;

    /**
     * 可用库存
     */
    private BigDecimal usableStock;

    /**
     * 配方间编码
     */
    private String roomCode;


}
