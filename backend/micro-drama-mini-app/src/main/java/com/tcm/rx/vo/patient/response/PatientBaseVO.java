package com.tcm.rx.vo.patient.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 诊疗开方系统--患者档案信息表
 * </p>
 *
 * @author xph
 * @since 2025-07-23
 */
@Data
public class PatientBaseVO implements Serializable {

    private static final long serialVersionUID = -7349090264399819179L;

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
     * 就诊卡号
     */
    private String clinicCode;

    /**
     * 住院号
     */
    private String inpatientNumber;

    /**
     * 患者id（his患者id）
     */
    private String patientId;

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 患者性别(0:男 1:女 2:未知)
     */
    private Integer sex;

    /**
     * 患者年龄(xx岁)
     */
    private String age;

    /**
     * 患者出生日期(yyyy-mm-dd)
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone="GMT+8")
    private Date birthDate;

    /**
     * 患者身高(cm)
     */
    private Double height;

    /**
     * 患者体重(kg)
     */
    private Double weight;

    /**
     * 患者证件号码（身份证号码）
     */
    private String idNumber;

    /**
     * 患者联系人手机号
     */
    private String phone;

    /**
     * 患者地区(多个值进行‘-’拼接)
     */
    private String region;

    /**
     * 患者地区ids(多个值进行‘,’拼接)
     */
    private String regionIds;

    /**
     * 患者地址
     */
    private String address;

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

}
