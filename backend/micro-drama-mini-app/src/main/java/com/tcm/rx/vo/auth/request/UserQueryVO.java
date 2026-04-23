package com.tcm.rx.vo.auth.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tcm.common.vo.PageVO;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 诊疗开方系统--用户表
 * </p>
 *
 * @author xph
 * @since 2025-07-07
 */
@Data
public class UserQueryVO extends PageVO implements Serializable {

    private static final long serialVersionUID = -3821740719263601031L;

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
     * 用户类型(admin:超级管理员,user:普通用户)
     */
    private String userType;

    /**
     * 用户账号
     */
    private String userAccount;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 职工编码code/工号
     */
    private String staffCode;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 状态(0:无效,1:正常,2:离职)
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
     * 主键ids
     */
    private List<Long> userIds;

    /**
     * 主键id
     */
    private Long userId;

    /**
     * 角色名称
     */
    private List<String> roleNameList;

    /**
     * 科室名称
     */
    private List<String> deptNameList;

}
