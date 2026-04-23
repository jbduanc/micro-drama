package com.tcm.rx.entity.auth;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

/**
 * <p>
 * 诊疗开方系统--用户表
 * </p>
 *
 * @author xph
 * @since 2025-07-07
 */
@Data
@TableName("rx_c_user")
public class CUser extends Model<CUser> {

    private static final long serialVersionUID = -1105166631885828937L;

    /**
     * 用户id
     */
    @TableId(value = "id", type = IdType.AUTO)
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
    @TableField(fill = FieldFill.UPDATE)
    private String updateBy;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
    private Date updateTime;

    /**
     * 角色ids
     */
    @TableField(exist = false)
    private List<Long> roleIds;

    /**
     * 科室ids
     */
    @TableField(exist = false)
    private List<Long> deptIds;

}
