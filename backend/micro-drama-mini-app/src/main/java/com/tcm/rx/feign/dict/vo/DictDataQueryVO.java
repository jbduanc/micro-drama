package com.tcm.rx.feign.dict.vo;

import com.tcm.common.vo.PageVO;
import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 集团（医联体）系统--字典数据表
 * </p>
 *
 * @author xph
 * @since 2025-07-18
 */
@Data
public class DictDataQueryVO extends PageVO implements Serializable {

    private static final long serialVersionUID = 3478319165232883702L;

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
