package com.tcm.rx.entity.patient;

import com.alibaba.fastjson2.JSONArray;
import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import java.math.BigDecimal;
import java.util.Date;

import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

/**
 * <p>
 * 诊疗开方系统--患者就诊流水表
 * </p>
 *
 * @author xph
 * @since 2025-07-23
 */
@Data
@TableName(value = "rx_patient_visit", autoResultMap = true)
public class PatientVisit extends Model<PatientVisit> {

    private static final long serialVersionUID = 7526382724751402056L;

    /**
     * 主键id
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
     * 病历编号
     */
    private String medicalRecordNo;

    /**
     * 患者档案信息id（rx_c_patient_base）
     */
    private Long patientBaseId;

    /**
     * 就诊类型(0:住院,1:门诊)
     */
    private Integer treatmentType;

    /**
     * 住院号
     */
    private String inpatientNumber;

    /**
     * (住院/门诊)就诊流水号
     */
    private String inpatientCode;

    /**
     * 就诊卡号
     */
    private String clinicCode;

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
     * 患者病区名称
     */
    private String wardName;

    /**
     * 患者病区编码
     */
    private String wardCode;

    /**
     * 患者科室名称
     */
    private String deptName;

    /**
     * 患者科室code/患者科室id（多个用,隔开）
     */
    private String deptCode;

    /**
     * 患者床号
     */
    private String bedNo;

    /**
     * 主诉
     */
    private String chiefComplaint;

    /**
     * 现病史
     */
    private String presentIllnessHistory;

    /**
     * 既往史
     */
    private String previousHistory;

    /**
     * 个人史
     */
    private String personalHistory;

    /**
     * 婚育史
     */
    private String maritalHistory;

    /**
     * 家族史
     */
    private String familyHistory;

    /**
     * 诊断名称
     */
    private String disName;

    /**
     * 诊断code/诊断id（多个用,隔开）
     */
    private String disCode;

    /**
     * 诊断描述
     */
    private String disDesc;

    /**
     * 主治医生id（诊疗系统用户id）json值
     */
    @TableField(value = "attending_doctor_id",typeHandler = JacksonTypeHandler.class)
    private JSONArray attendingDoctorId;

    /**
     * 状态(0:禁用,1:启用)
     */
    private Integer status;

    /**
     * 是否怀孕
     */
    private String isPregnant;

    /**
     * 体温
     */
    private BigDecimal bodyTemp;

    /**
     * 脉搏
     */
    private Integer pulse;

    /**
     * 呼吸
     */
    private Integer resp;

    /**
     * 舒张压
     */
    private BigDecimal diastPress;

    /**
     * 收缩压
     */
    private BigDecimal systPress;

    /**
     * 手术史
     */
    private String surgeryHistory;

    /**
     * 其他病史
     */
    private String otherMedicalHistory;

    /**
     * 体格检查
     */
    private String physicalExamination;

    /**
     * 中医四诊
     */
    private String tcmFourDiagnostics;

    /**
     * 中医疾病，多个逗号分割
     */
    private String tcmDisease;

    /**
     * 中医证候，多个逗号分割
     */
    private String tcmPattern;

    /**
     * 西医疾病Id，多个逗号分割
     */
    private String diagnosisIds;

    /**
     * 中医疾病ids，多个逗号分割
     */
    private String tcmDiseaseIds;

    /**
     * 中医证候ids，多个逗号分割
     */
    private String tcmPatternIds;

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

}
