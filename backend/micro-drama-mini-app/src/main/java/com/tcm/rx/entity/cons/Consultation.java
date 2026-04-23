package com.tcm.rx.entity.cons;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.tcm.common.entity.RxBaseEntity;
import lombok.Data;

import java.util.Date;

/**
 * <p>
 * 诊疗开方系统--会诊表
 * </p>
 *
 * @author djbo
 * @since 2025-09-04
 */
@Data
@TableName("rx_consultation")
public class Consultation extends RxBaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 状态 0-草稿 1-待会诊 2-会诊结束
     */
    private Integer status;

    /**
     * 流水id
     */
    private Long visitId;

    /**
     * 患者id
     */
    private String patientId;

    /**
     * 患者姓名
     */
    private String patientName;

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
     * 西医疾病，多个逗号分割
     */
    private String diagnosis;

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
     * 中医疾病Id，多个逗号分割
     */
    private String tcmDiseaseIds;

    /**
     * 中医证候Id，多个逗号分割
     */
    private String tcmPatternIds;

    /**
     * 处方id
     */
    private String rxId;

    /**
     * 处方JSON
     */
    private String rxJson;

    /**
     * 会诊发起时间
     */
    private Date consInitTime;

    /**
     * 会诊医疗机构的id
     */
    private Long consInitHspId;

    /**
     * 会诊用户id
     */
    private Long consInitUserId;

    /**
     * 会诊目的
     */
    private String consPurpose;

    /**
     * 拟邀请会诊医疗机构的id
     */
    private Long propinvConsHspId;

    /**
     * 拟邀请会诊科室id
     */
    private Long propinvConsDeptId;

    /**
     * 拟邀请会诊用户id
     */
    private Long propinvConsUserId;

    /**
     * 会诊结束时间
     */
    private Date consEndTime;

    /**
     * 会诊类型 0-远程会诊 1-处方会诊
     */
    private Integer consType;

    /**
     * 会诊结果
     */
    private String consResult;

    /**
     * 是否怀孕
     */
    private String isPregnant;

    /**
     * 会诊医生手机号
     */
    private String propinvConsUserPhone;
}
