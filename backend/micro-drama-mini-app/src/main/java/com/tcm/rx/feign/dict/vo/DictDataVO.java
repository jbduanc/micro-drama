package com.tcm.rx.feign.dict.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 集团（医联体）系统--字典数据表
 * </p>
 *
 * @author xph
 * @since 2025-07-18
 */
@Data
public class DictDataVO implements Serializable {

    private static final long serialVersionUID = 8157398788605057727L;

    /**
     * 字典编码
     */
    private Long dictCode;

    /**
     * 所属医联体的id
     */
    private Long medicalGroupId;

    /**
     * 所属医联体的code
     */
    private String medicalGroupCode;

    /**
     * 字典标签
     */
    private String dictLabel;

    /**
     * 字典键值
     */
    private String dictValue;

    /**
     * 字典类型
     */
    private String dictType;

    /**
     * 样式属性（其他样式扩展）
     */
    private String cssClass;

    /**
     * 表格回显样式
     */
    private String listClass;

    /**
     * 是否默认（Y是 N否）
     */
    private String isDefault;

    /**
     * 字典排序
     */
    private Integer sort;

    /**
     * 备注
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
     * 字典名称
     */
    private String dictName;

    /**
     * 煎药中心系统权限：0.无权限，1.有权限
     */
    private Integer decoctPerms;

    /**
     * 诊疗系统权限：0.无权限，1.有权限
     */
    private Integer rxPerms;

}
