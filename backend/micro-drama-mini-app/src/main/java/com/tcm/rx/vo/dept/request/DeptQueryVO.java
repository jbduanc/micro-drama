package com.tcm.rx.vo.dept.request;

import com.tcm.common.vo.PageVO;
import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 诊疗开方系统--科室表
 * </p>
 *
 * @author xph
 * @since 2025-07-10
 */
@Data
public class DeptQueryVO extends PageVO implements Serializable {

    private static final long serialVersionUID = -4148807019301696966L;

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
     * 医疗机构的id
     */
    private Long hspId;

    /**
     * 医疗机构的code
     */
    private String hspCode;

    /**
     * 科室的名称
     */
    private String deptName;

    /**
     * 科室的code
     */
    private String deptCode;

    /**
     * 科室名称首拼
     */
    private String deptNameInitial;

    /**
     * 状态(0:禁用,1:启用)
     */
    private Integer status;

}
