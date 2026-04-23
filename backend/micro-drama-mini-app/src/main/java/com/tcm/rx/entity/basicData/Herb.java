package com.tcm.rx.entity.basicData;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.tcm.common.entity.RxBaseEntity;
import lombok.Data;

import java.math.BigDecimal;

/**
 * <p>
 * 诊疗开方系统--饮片管理表
 * </p>
 *
 * @author xph
 * @since 2025-07-17
 */
@Data
@TableName("rx_herb")
public class Herb extends RxBaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 饮片ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 医疗机构的id多个逗号分割
     */
    private String hspIds;

    /**
     * 医疗机构的code多个逗号分割
     */
    private String hspCodes;

    /**
     * 饮片编码
     */
    private String herbCode;

    /**
     * 饮片名称
     */
    private String herbName;

    /**
     * 饮片规格
     */
    private String spec;

    /**
     * 饮片类型
     */
    private String type;

    /**
     * 单位
     */
    private String unit;

    /**
     * 成本价
     */
    private BigDecimal costPrice;

    /**
     * 毛利率
     */
    private BigDecimal grossMargin;

    /**
     * 零售价
     */
    private BigDecimal retailPrice;

    /**
     * 助记码
     */
    private String helpCode;

    /**
     * 是否支持医保 0-是 1-否 2-单独使用不予支付
     */
    private Integer medicalInsurance;

    /**
     * 状态 0-启用 1-禁用
     */
    private Integer status;

    /**
     * 医保编码
     */
    private String medicalCode;

    /**
     * 单位重量
     */
    private String unitWeight;

    /**
     * 性味
     */
    private String propertyFlavor;

    /**
     * 归经
     */
    private String meridianTropism;

    /**
     * 主治功能
     */
    private String therapFunction;

    /**
     * 使用禁忌
     */
    private String usageForbidden;

    /**
     * 存储条件
     */
    private String storageCondition;

    /**
     * HIS饮片ID（对接HIS系统）
     */
    private String hisHerbId;
}
