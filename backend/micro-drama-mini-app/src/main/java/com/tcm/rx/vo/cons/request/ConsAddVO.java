package com.tcm.rx.vo.cons.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class ConsAddVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
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


