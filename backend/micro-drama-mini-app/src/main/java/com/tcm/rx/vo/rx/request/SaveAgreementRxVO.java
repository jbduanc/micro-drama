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
public class SaveAgreementRxVO {

    private static final long serialVersionUID = 1L;

    /**
     * 协定方ID
     */
    private Long id;

    /**
     * 医疗机构的id
     */
    private Long hspId;

    /**
     * 医疗机构的code
     */
    private String hspCode;

    /**
     * 医疗机构名称
     */
    private String hspName;

    /**
     * 处方名称
     */
    private String rxName;

    /**
     * 协定方类型 0-个人 1-通用 2-科室
     */
    private Integer type;

    /**
     * 科室ID (多个科室用逗号隔开)
     */
    private String deptId;

    /**
     * 科室名称 (多个科室用逗号隔开)
     */
    private String deptName;

    /**
     * 归属人账号
     */
    private Long ownerAccount;

    /**
     * 归属人账号名称
     */
    private String ownerAccountName;

    /**
     * 状态 0-启用 1-禁用
     */
    private Integer status;

    /**
     * 剂数
     */
    private Integer doseCount;

    /**
     * 主要功效
     */
    private String mainEfficacy;

    /**
     * 适应症
     */
    private String adaptation;

    /**
     * 处方总金额
     */
    private BigDecimal totalMoney;

    /**
     * 单剂总金额
     */
    private BigDecimal doseTotalMoney;

    /**
     * 明细字段
     */
    private List<SaveAgreementRxItemVO> itemList;


}
