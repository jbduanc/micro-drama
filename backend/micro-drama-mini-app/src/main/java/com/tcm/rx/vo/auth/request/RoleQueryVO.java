package com.tcm.rx.vo.auth.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tcm.common.vo.PageVO;
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
public class RoleQueryVO extends PageVO implements Serializable {

    private static final long serialVersionUID = -5267584498485215936L;

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
     * 状态(0:禁用,1:启用)
     */
    private Integer status;

    /**
     * 开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
    private Date startTime;

    /**
     * 结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
    private Date endTime;

    /**
     * 主键id
     */
    private Long roleId;

    /**
     * 主键ids
     */
    private List<Long> roleIds;

    /**
     * 用户ids
     */
    private List<Long> userIds;

}
