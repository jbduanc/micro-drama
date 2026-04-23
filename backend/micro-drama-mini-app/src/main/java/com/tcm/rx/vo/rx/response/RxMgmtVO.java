package com.tcm.rx.vo.rx.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

import java.io.Serializable;

/**
 * <p>
 * 诊疗开方系统--处方主表
 * </p>
 *
 * @author shouhan
 * @since 2025-07-21
 */
@Data
public class RxMgmtVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 方剂ID
     */
    private Long id;

    /**
     * 所属医联体的id
     */
    private Long medicalGroupId;

    /**
     * 所属医联体的code
     */
    private String medicalGroupCode;

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
     * 主治功效
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
    private String usage;

    /**
     * 用法名称
     */
    private String usageName;

    /**
     * 方剂组成
     */
    private String formula;

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

    /**
     * 处方表
     */
    private String herbNames;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 创建人名称
     */
    private String createByName;

    /**
     * 创建时间
     */
    private String createTimeStr;

    /**
     * 修改人
     */
    private String updateBy;

    /**
     * 修改人名称
     */
    private String updateByName;

    /**
     * 更新时间
     */
    private String updateTimeStr;

    /**
     * 库存是否充足
     */
    private Boolean isStockSuff = true;

    /**
     * 单剂总金额
     */
    private BigDecimal doseTotalMoney;

    /**
     * 饮片详情
     */
    private List<RxMgmtDetailVO> rxMgmtDetailVOList;
}
