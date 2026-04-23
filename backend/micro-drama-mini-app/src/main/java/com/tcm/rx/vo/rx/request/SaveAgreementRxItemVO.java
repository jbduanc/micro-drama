package com.tcm.rx.vo.rx.request;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * 诊疗开方系统--协定方主表
 * </p>
 *
 * @author shouhan
 * @since 2025-07-17
 */
@Data
public class SaveAgreementRxItemVO {

    private static final long serialVersionUID = 1L;

    /**
     * 明细ID
     */
    private Long id;

    /**
     * 协定方ID
     */
    private Long agreementRxId;

    /**
     * 序号
     */
    private Integer serialNum;

    /**
     * 饮片编码
     */
    private String herbCode;

    /**
     * 饮片名称
     */
    private String herbName;

    /**
     * 规格
     */
    private String spec;

    /**
     * 饮品类型
     */
    private String type;

    /**
     * 饮片用量
     */
    private BigDecimal herbDosage;

    /**
     * 单位
     */
    private String unit;

    /**
     * 单价
     */
    private BigDecimal price;

    /**
     * 单剂金额
     */
    private BigDecimal money;

    /**
     * 调配要求
     */
    private String adjustRequire;

    /**
     * 剂数
     */
    private Integer doseCount;

    /**
     * 一剂/包
     */
    private Integer oneDose;

    /**
     * 每包/ml
     */
    private Integer perPackVolume;

    /**
     * 用法
     */
    private String usage;

    /**
     * 煎药要求
     */
    private String decoctRequire;

    /**
     * 用药频次
     */
    private String dosageFrequency;

}
