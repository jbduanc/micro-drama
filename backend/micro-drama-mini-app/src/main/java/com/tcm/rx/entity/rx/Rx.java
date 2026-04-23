package com.tcm.rx.entity.rx;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.tcm.common.entity.RxBaseEntity;
import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 诊疗开方系统--处方主表
 * </p>
 *
 * @author shouhan
 * @since 2025-07-21
 */
@Data
@TableName("rx_rx")
public class Rx extends RxBaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 处方ID
     */
    @TableId(value = "id")
    private String id;

    /**
     * 医疗机构的id
     */
    private Long hspId;

    /**
     * 医疗机构的code
     */
    private String hspCode;

    /**
     * 流水id
     */
    private Long visitId;

    /**
     * 病历编号
     */
    private String medicalRecordNo;

    /**
     * 处方来源
     */
    private Integer source;

    /**
     * his处方编号
     */
    private String hisRxId;

    /**
     * 就诊类型0-住院 1-门诊
     */
    private String treatmentType;

    /**
     * 方剂类型
     */
    private String formulaType;

    /**
     * 是否需要审核 0-是 1-否
     */
    private Boolean needReview;

    /**
     * 状态 0-待审核 1-待缴费 2-已缴费 3-已上传 4-已执行 5-已作废 6-已退费
     */
    private Integer status;

    /**
     * 是否怀孕
     */
    private String isPregnant;

    /**
     * 西医诊断
     */
    private String diagnosis;

    /**
     * 中医病名
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
     * 是否快递
     */
    private String isExpress;

    /**
     * 收货人
     */
    private String consignee;

    /**
     * 收货联系电话
     */
    private String consigneePhone;

    /**
     * 收货地区
     */
    private String receiptRegion;

    /**
     * 收货地区id
     */
    @TableField("receipt_region_Ids")
    private String receiptRegionIds;

    /**
     * 收货地址
     */
    private String receiptAddress;

    /**
     * 患者id
     */
    private String patientId;

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 患者性别
     */
    private String patientGender;

    /**
     * 患者年龄
     */
    private String patientAge;

    /**
     * 床位号
     */
    private String bedNo;

    /**
     * 就诊卡号
     */
    private String clinicCode;

    /**
     * 住院号
     */
    private String inpatientNumber;

    /**
     * 科室id
     */
    private Long deptId;

    /**
     * 科室
     */
    private String department;

    /**
     * 开方人id
     */
    private Long prescriberId;

    /**
     * 开方人名称
     */
    private String prescriberName;

    /**
     * 开方时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
    private Date prescriptionTime;

    /**
     * 审核人id
     */
    private Long auditorId;

    /**
     * 审核人名称
     */
    private Long auditorName;

    /**
     * 审核时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
    private Date auditTime;

    /**
     * 审核意见
     */
    private String auditComments;

    /**
     * 费别
     */
    private String feeType;

    /**
     * 发票号
     */
    private String invoiceNumber;

    /**
     * 收费员
     */
    private String cashier;

    /**
     * 发药人id
     */
    private Long dispenserId;

    /**
     * 发药人名称
     */
    private String dispenserName;

    /**
     * 上传时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
    private Date dispenserTime;

    /**
     * 处方总金额
     */
    private BigDecimal totalMoney;

    /**
     * 药品费用
     */
    private BigDecimal drugCost;

    /**
     * 加工费用
     */
    private BigDecimal processingFee;

    /**
     * 适宜技术费用
     */
    private BigDecimal shiyiJishuFee;

    /**
     * 单剂金额
     */
    private BigDecimal singleDoseAmount;

    /**
     * 药品总金额
     */
    private BigDecimal totalDrugAmount;

    /**
     * 会诊机构
     */
    private String consultationOrg;

    /**
     * 会诊医生id
     */
    private String consultationUserId;

    /**
     * 会诊医生Name
     */
    private String consultationUserName;

    /**
     * 会诊时间
     */
    private Date consultationTime;

    /**
     * 缴费时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
    private Date paymentTime;

    /**
     * 执行时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
    private Date executionTime;

    /**
     * 退费时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
    private Date refundTime;

    /**
     * 作废时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
    private Date invalidTime;

    /**
     * 作废用户id
     */
    private String invalidUserId;

    /**
     * 作废用户Name
     */
    private String invalidUserName;

    /**
     * 备注
     */
    private String remark;
}
