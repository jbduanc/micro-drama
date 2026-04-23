package com.tcm.rx.vo.patient.request;

import com.alibaba.fastjson2.JSONArray;
import com.tcm.common.vo.PageVO;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * <p>
 * 诊疗开方系统--患者就诊流水表
 * </p>
 *
 * @author xph
 * @since 2025-07-23
 */
@Data
public class PatientVisitQueryVO extends PageVO implements Serializable {

    private static final long serialVersionUID = -1482461020324263798L;

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
     * 患者id
     */
    private String patientId;

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 患者证件号码（身份证号码）
     */
    private String idNumber;

    /**
     * 患者联系人手机号
     */
    private String phone;

    /**
     * 患者病区编码
     */
    private String wardCode;

    /**
     * 主治医生id（诊疗系统用户id）json值
     */
    private List<Long> attendingDoctorIds;

    /**
     * 状态(0:禁用,1:启用)
     */
    private Integer status;

    /**
     * 创建时间 yyyy-MM-dd
     */
    private String createTimeStr;

}
