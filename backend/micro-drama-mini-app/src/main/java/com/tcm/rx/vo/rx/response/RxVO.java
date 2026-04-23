package com.tcm.rx.vo.rx.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tcm.rx.entity.rx.RxDetail;
import com.tcm.rx.entity.rx.RxFee;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 诊疗开方系统--处方主表
 * </p>
 *
 * @author shouhan
 * @since 2025-07-21
 */
@Data
public class RxVO {

    private static final long serialVersionUID = 1L;

    /**
     * 处方ID
     */
    private String id;

    /**
     * 医联体id
     */
    private Long medicalGroupId;

    /**
     * 医联体code
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
     * 医疗机构的Name
     */
    private String hspName;

    /**
     * 就诊流水号
     */
    private String inpatientCode;

    /**
     * 病历编号
     */
    private String medicalRecordNo;

    /**
     * 处方来源
     */
    private Integer source;

    /**
     * 处方来源
     */
    private String sourceName;

    /**
     * his处方编号
     */
    private String hisRxId;

    /**
     * 就诊类型0-住院 1-门诊
     */
    private String treatmentType;

    /**
     * 就诊类型0-住院 1-门诊
     */
    private String treatmentTypeName;

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
     * 状态名称
     */
    private String statusName;

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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
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
     * 缴费时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date paymentTime;

    /**
     * 执行时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date executionTime;

    /**
     * 退费时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date refundTime;

    /**
     * 作废时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
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
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date createTime;

    /**
     * 修改时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date updateTime;

    /**
     * 创建人id
     */
    private String createBy;

    /**
     * 创建人Name
     */
    private String createByName;

    /**
     * 修改人id
     */
    private String updateBy;

    /**
     * 修改人Name
     */
    private String updateByName;

    /**
     * 备注
     */
    private String remark;

    /**
     * 处方表
     */
    private String rxHerbNames;

    /**
     * 处方明细表信息
     */
    private List<RxDetail> rxDetails;

    /**
     * 处方费用表信息
     */
    private List<RxFeeVO> rxFees;
}
