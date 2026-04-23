package com.tcm.rx.vo.auth.request;

import com.tcm.common.vo.PageVO;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * <p>
 * 诊疗开方系统--菜单表
 * </p>
 *
 * @author xph
 * @since 2025-07-08
 */
@Data
public class MenuQueryVO extends PageVO implements Serializable {

    private static final long serialVersionUID = -6105046958512742583L;

//    /**
//     * 所属医联体的id
//     */
//    private Long medicalGroupId;
//
//    /**
//     * 所属医联体的code
//     */
//    private String medicalGroupCode;
//
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
     * 菜单名称
     */
    private String menuName;

    /**
     * 状态(0:禁用,1:启用)
     */
    private Integer status;

    /**
     * 主键ids
     */
    private List<Long> menuIds;

    /**
     * 菜单类型：M.目录，C.菜单，F.按钮
     */
    private List<String> menuTypes;

    /**
     * 角色ids
     */
    private List<Long> roleIds;

    /**
     * 用户id
     */
    private Long userId;

}
