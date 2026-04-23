package com.tcm.rx.vo.rx.request;

import com.tcm.common.vo.PageVO;
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
public class AgreementRxQueryVO extends PageVO {

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
     * 归属人账号
     */
    private String ownerAccountName;
    /**
     * 适应症
     */
    private String adaptation;

    /**
     * 状态 0-启用 1-禁用
     */
    private Integer status;

    /**
     * 列表应用场景  0.个人协定方管理列表和协定方管理列表   1. 开方时选择可用协定方
     */
    private int listScene = 0;

}
