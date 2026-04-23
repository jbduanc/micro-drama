package com.tcm.rx.vo.hsp.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 诊疗开方系统--医疗机构表
 * </p>
 *
 * @author xph
 * @since 2025-07-10
 */
@Data
public class HspVO implements Serializable {

    private static final long serialVersionUID = -889752907580389665L;

    /**
     * 主键id
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
     * 医疗机构的名称
     */
    private String hspName;

    /**
     * 医疗机构的code
     */
    private String hspCode;

    /**
     * his系统的名称
     */
    private String hisSysName;

    /**
     * his医院的编码
     */
    private String hisHspCode;

    /**
     * 对应his科室的编码
     */
    private String hisDeptCode;

    /**
     * 联系电话（座机号）
     */
    private String tel;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 地址
     */
    private String address;

    /**
     * 是否接收会诊：0.否,1.是
     */
    private Integer isConsult;

    /**
     * 是否快递：0.否,1.是
     */
    private Integer isExpress;

    /**
     * 简介
     */
    private String remark;

    /**
     * 状态(0:禁用,1:启用)
     */
    private Integer status;

    /**
     * 是否删除:0.未删除，1.已删除
     */
    private Integer delFlag;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
    private Date createTime;

    /**
     * 修改人
     */
    private String updateBy;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
    private Date updateTime;

    /**
     * 序号
     */
    private Integer rowNo;

    /**
     * 校验数据错误的提示
     */
    private String errMsg;

}
