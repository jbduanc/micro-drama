package com.tcm.rx.vo.auth.response;

import com.fasterxml.jackson.annotation.JsonFormat;
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
public class CUserVO implements Serializable {

    private static final long serialVersionUID = 7763767901558999951L;

    /**
     * 用户id
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
     * 用户类型(admin:超级管理员,user:普通用户)
     */
    private String userType;

    /**
     * 用户账号
     */
    private String userAccount;

    /**
     * 密码
     */
    private String password;

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
     * 邮箱
     */
    private String email;

    /**
     * 用户性别：0.男，1.女，2.未知
     */
    private Integer sex;

    /**
     * 生日/出生日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone="GMT+8")
    private Date birthday;

    /**
     * 头像地址
     */
    private String avatar;

    /**
     * 备注
     */
    private String remark;

    /**
     * his医生编码
     */
    private String hisDoctorCode;

    /**
     * his医生科室名称
     */
    private String hisDoctorDeptName;

    /**
     * his医生科室编码
     */
    private String hisDoctorDeptCode;

    /**
     * 状态(0:无效,1:正常,2:离职)
     */
    private Integer status;

    /**
     * 是否接收会诊：0.否,1.是
     */
    private Integer isConsult;

    /**
     * 离职日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone="GMT+8")
    private Date quitDate;

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
     * 所属医联体的名称
     */
    private String medicalGroupName;

    /**
     * 医疗机构的名称
     */
    private String hspName;

    /**
     * 角色ids
     */
    private List<Long> roleIds;

    /**
     * 角色数据
     */
    private List<CRoleVO> roleList;

    /**
     * 角色名称（聚合数据，用","分隔）
     */
    private String roleNames;

    /**
     * 科室ids
     */
    private List<Long> deptIds;

    /**
     * 科室名称（聚合数据，用","分隔）
     */
    private String deptNames;

    /**
     * 原密码
     */
    private String oldPassword;

    /**
     * 校验数据错误的提示
     */
    private String errMsg;

}
