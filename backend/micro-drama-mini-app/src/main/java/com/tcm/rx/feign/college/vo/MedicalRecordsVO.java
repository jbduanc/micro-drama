package com.tcm.rx.feign.college.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.tcm.common.vo.PageVO;
import lombok.Data;

import java.util.Date;

@Data
public class MedicalRecordsVO extends PageVO {

    private Long id;

    /**
     * 医案名称
     */
    private String name;

    /**
     * 出处
     */
    private String source;

    /**
     * 医生
     */
    private String doctorName;

    /**
     * 患者
     */
    private String patientName;

    /**
     * 患者年龄
     */
    private String patientAge;

    /**
     * 患者性别
     */
    private String patientSex;

    /**
     * 就诊时间
     */
    private Date visitTime;

    /**
     * 主诉
     */
    private String chiefComplaint;

    /**
     * 现病史
     */
    private String presentIllness;

    /**
     * 刻下症
     */
    private String instantSyndrome;

    /**
     * 西医诊断
     */
    private String diagnosis;

    /**
     * 舌质
     */
    private String tongueQuality;

    /**
     * 舌苔
     */
    private String tongueCoat;

    /**
     * 脉象
     */
    private String pulseCondition;

    /**
     * 中医诊断
     */
    private String tcmDisease;

    /**
     * 中医证候
     */
    private String tcmPattern;

    /**
     * 治则治法
     */
    private String treatMethod;

    /**
     * 方名
     */
    private String formulaName;

    /**
     * 用法
     */
    @TableField(value = "`usage`")
    private String usage;

    /**
     * 方剂组成
     */
    private String prescriptionComposition;

    /**
     * 医嘱
     */
    private String advice;

    /**
     * 预后
     */
    private String prognosis;

    /**
     * 按语
     */
    private String notes;

    private String errorMsg;

}
