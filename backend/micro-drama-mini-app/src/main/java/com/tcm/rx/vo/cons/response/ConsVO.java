package com.tcm.rx.vo.cons.response;

import com.alibaba.fastjson2.JSONObject;
import com.tcm.rx.vo.rx.request.RxSaveVO;
import lombok.Data;

@Data
public class ConsVO {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 医疗集团ID
     */
    private Long medicalGroupId;

    /**
     * 医疗集团编码
     */
    private String medicalGroupCode;

    /**
     * 会诊状态
     * 0: 未开始, 1: 进行中, 2: 已完成, 3: 已取消
     */
    private Integer status;

    /**
     * 状态名称
     */
    private String statusName;

    /**
     * 就诊ID，关联到就诊记录
     */
    private Long visitId;

    /**
     * 患者ID，关联到患者信息表
     */
    private Long patientId;

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 诊所编码
     */
    private String clinicCode;

    /**
     * 患者性别
     * 'M': 男, 'F': 女, 'U': 未知
     */
    private Integer sex;

    /**
     * 性别名称
     */
    private String sexName;

    /**
     * 患者手机号
     */
    private String phone;

    /**
     * 会诊医生手机号
     */
    private String propinvConsUserPhone;

    /**
     * 患者年龄
     */
    private String age;

    /**
     * 患者主诉
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
     * 西医疾病
     */
    private String diagnosis;

    /**
     * 中医疾病诊断
     */
    private String tcmDisease;

    /**
     * 中医证型
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
     * 处方ID，关联到处方表
     */
    private String rxId;

    /**
     * 处方JSON数据，存储处方详细信息
     */
    private String rxJson;

    /**
     * 处方VO
     */
    private JSONObject rxSaveVO;

    /**
     * 会诊开始时间字符串
     * 格式：yyyy-MM-dd HH:mm:ss
     */
    private String consInitTimeStr;

    /**
     * 会诊发起医院ID
     */
    private Long consInitHspId;

    /**
     * 会诊发起医院Name
     */
    private String consInitHspName;

    /**
     * 会诊发起人ID，通常为医生ID
     */
    private Long consInitUserId;

    /**
     * 会诊发起人ID，通常为医生Name
     */
    private String consInitUserName;

    /**
     * 会诊目的
     */
    private String consPurpose;

    /**
     * 受邀会诊医院ID
     */
    private Long propinvConsHspId;

    /**
     * 受邀会诊医院Name
     */
    private String propinvConsHspName;

    /**
     * 受邀会诊科室ID
     */
    private Long propinvConsDeptId;

    /**
     * 受邀会诊科室Name
     */
    private String propinvConsDeptName;

    /**
     * 受邀会诊医生ID
     */
    private Long propinvConsUserId;

    /**
     * 受邀会诊医生Name
     */
    private String propinvConsUserName;

    /**
     * 会诊结束时间字符串
     * 格式：yyyy-MM-dd HH:mm:ss
     */
    private String consEndTimeStr;

    /**
     * 会诊类型
     * 1: 远程会诊, 2: 院内会诊, 3: 多学科会诊(MDT)
     */
    private Integer consType;

    /**
     * 会诊类型Name
     * 1: 远程会诊, 2: 院内会诊, 3: 多学科会诊(MDT)
     */
    private String consTypeName;

    /**
     * 会诊结果
     */
    private String consResult;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 创建人Name
     */
    private String createByName;

    /**
     * 创建时间字符串
     * 格式：yyyy-MM-dd HH:mm:ss
     */
    private String createTimeStr;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 更新人Name
     */
    private String updateByName;

    /**
     * 更新时间字符串
     * 格式：yyyy-MM-dd HH:mm:ss
     */
    private String updateTimeStr;

    /**
     * 是否缴费
     */
    private Boolean isPaid = false;

    /**
     * 是否怀孕
     */
    private String isPregnant;
}
