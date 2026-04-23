package com.tcm.rx.vo.dept.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 诊疗开方系统--科室表
 * </p>
 *
 * @author xph
 * @since 2025-07-10
 */
@Data
public class DeptVO implements Serializable {

    private static final long serialVersionUID = -3162351549695130885L;

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
     * 医疗机构的名称
     */
    private String hspName;

    /**
     * 校验数据错误的提示
     */
    private String errMsg;

}
