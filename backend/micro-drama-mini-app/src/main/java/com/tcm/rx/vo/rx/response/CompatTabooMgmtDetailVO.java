package com.tcm.rx.vo.rx.response;

import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 诊疗开方系统--配伍禁忌管理明细表
 * </p>
 *
 * @author djbo
 * @since 2025-09-10
 */
@Data
public class CompatTabooMgmtDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 明细ID
     */
    private Long id;

    /**
     * 配伍禁忌关联ID
     */
    private Long tabooMgmtId;

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
     * 饮片类型
     */
    private String type;
}
