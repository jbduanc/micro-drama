package com.tcm.rx.entity.rx;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.tcm.common.entity.RxBaseEntity;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * 诊疗开方系统--方剂管理
 * </p>
 *
 * @author djbo
 * @since 2025-09-08
 */
@Data
@TableName("rx_rx_mgmt")
public class RxMgmt extends RxBaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 方剂ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 方剂类型:1.经古名方,2.专病专方,3.辩证实施
     */
    private Integer type;

    /**
     * 科室ID
     */
    private String deptIds;

    /**
     * 科室名称
     */
    private String deptNames;

    /**
     * 方剂名称
     */
    private String name;

    /**
     * 简称
     */
    private String alias;

    /**
     * 助记码
     */
    private String helpCode;

    /**
     * 治疗疾病
     */
    private String treatmentDisease;

    /**
     * 主治功效/症状
     */
    private String mainEfficacy;

    /**
     * 西医疾病，多个逗号分割
     */
    private String diagnosis;

    /**
     * 中医疾病，多个逗号分割
     */
    private String tcmDisease;

    /**
     * 中医证候，多个逗号分割
     */
    private String tcmPattern;

    /**
     * 西医疾病Id，多个逗号分割
     */
    private String diagnosisIds;

    /**
     * 中医疾病Id，多个逗号分割
     */
    private String tcmDiseaseIds;

    /**
     * 中医证候Id，多个逗号分割
     */
    private String tcmPatternIds;

    /**
     * 西医疾病助记码，多个逗号分割
     */
    private String diagnosisHelpCode;

    /**
     * 中医疾病助记码，多个逗号分割
     */
    private String tcmDiseaseHelpCode;

    /**
     * 方剂来源
     */
    private String source;

    /**
     * 用法
     */
    @TableField(value = "`usage`")
    private String usage;

    /**
     * 方剂组成
     */
    private String formula;

    /**
     * 单剂总金额
     */
    private BigDecimal doseTotalMoney;

    /**
     * 状态:0.禁用,1.启用
     */
    private Integer status;

    /**
     * 禁忌
     */
    private String taboo;

    /**
     * 熟煮方法
     */
    private String cookingMethod;

    /**
     * 同类中成药
     */
    private String similarChinesePatent;

    /**
     * 方解
     */
    private String rxExplanation;

    /**
     * 按语
     */
    private String remark;
}
