package com.tcm.rx.entity.rx;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableId;
import com.tcm.common.entity.RxBaseEntity;
import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 诊疗开方系统--处方明细表
 * </p>
 *
 * @author shouhan
 * @since 2025-07-21
 */
@Data
@TableName("rx_rx_detail")
public class RxDetail extends RxBaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 明细ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 处方主表关联ID
     */
    private String rxId;

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
    @TableField(value = "`usage`")
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
