package com.tcm.rx.vo.patient.response;

import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 诊疗开方系统--病历模版管理表
 * </p>
 *
 * @author djbo
 * @since 2025-09-11
 */
@Data
public class MedrecTplMgmtVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    private Long id;

    /**
     * 所属医联体ID
     */
    private Long medicalGroupId;

    /**
     * 所属医联体编码
     */
    private String medicalGroupCode;

    /**
     * 模版类型:0.通用,1.个人
     */
    private Integer tplType;

    /**
     * 模版名称
     */
    private String tplName;

    /**
     * 归属人id
     */
    private Long ownerId;

    /**
     * 归属人姓名
     */
    private String ownerName;

    /**
     * 状态:0.启用,1.禁用
     */
    private Integer status;

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
     * 诊断意见
     */
    private String diagnosticOpinions;

    /**
     * 创建人（用户名/工号）
     */
    private String createBy;

    /**
     * 创建人（用户名/工号）
     */
    private String createByName;

    /**
     * 创建时间（自动填充当前时间）
     */
    private String createTimeStr;

    /**
     * 最后修改人（用户名/工号）
     */
    private String updateBy;

    /**
     * 最后修改人（用户名/工号）
     */
    private String updateByName;

    /**
     * 最后更新时间（修改时自动更新）
     */
    private String updateTimeStr;
}
