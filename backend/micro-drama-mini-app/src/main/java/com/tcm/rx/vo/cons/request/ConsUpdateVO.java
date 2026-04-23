package com.tcm.rx.vo.cons.request;

import lombok.Data;

@Data
public class ConsUpdateVO {
    /**
     * 会诊ID（必填）
     */
    private Long id;

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
     * 西医诊断
     */
    private String diagnosis;

    /**
     * 中医疾病
     */
    private String tcmDisease;

    /**
     * 中医证候
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
     * 处方JSON（更新处方明细）
     */
    private String rxJson;

    /**
     * 会诊目的
     */
    private String consPurpose;

    /**
     * 拟邀请会诊医疗机构ID
     */
    private Long propinvConsHspId;

    /**
     * 拟邀请会诊科室ID
     */
    private Long propinvConsDeptId;

    /**
     * 拟邀请会诊医生ID
     */
    private Long propinvConsUserId;
}
