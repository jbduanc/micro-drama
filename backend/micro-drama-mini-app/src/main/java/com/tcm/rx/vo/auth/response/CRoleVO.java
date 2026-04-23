package com.tcm.rx.vo.auth.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 诊疗开方系统--角色表
 * </p>
 *
 * @author xph
 * @since 2025-07-08
 */
@Data
public class CRoleVO implements Serializable {

    private static final long serialVersionUID = -9190221097988972105L;

    /**
     * 角色id
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

//    /**
//     * 医疗机构的id
//     */
//    private Long hspId;
//
//    /**
//     * 医疗机构的code
//     */
//    private String hspCode;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 角色编码/权限字符
     */
    private String roleCode;

    /**
     * 角色描述
     */
    private String description;

    /**
     * 排序
     */
    private Integer sort;

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
     * 菜单ids
     */
    private List<Long> menuIds;

    /**
     * 菜单数据
     */
    private List<CMenuVO> menuList;

}
