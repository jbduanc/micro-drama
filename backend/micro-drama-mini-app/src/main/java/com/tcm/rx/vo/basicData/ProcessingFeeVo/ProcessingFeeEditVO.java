package com.tcm.rx.vo.basicData.ProcessingFeeVo;

import lombok.Data;

import java.util.Date;

/**
 * 诊疗开方系统--加工费管理表(ProcessingFee)实体类
 *
 * @author duanqiyuan
 * @since 2025-07-17 15:09:46
 */
@Data
public class ProcessingFeeEditVO {

    private static final long serialVersionUID = 890865583497599611L;

    /**
     * ID
     */
    private Long id;

    /**
     * 煎药要求
     */
    private String decoctRequire;

    /**
     * 计价方式
     */
    private String pricingMethod;

    /**
     * 加工费（元）
     */
    private Double fee;

    /**
     * 对应收费项目编码（关联收费项目表）
     */
    private String chargeItemCode;

}

