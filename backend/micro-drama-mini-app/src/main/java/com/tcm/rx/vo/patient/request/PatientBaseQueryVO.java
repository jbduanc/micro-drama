package com.tcm.rx.vo.patient.request;

import com.tcm.common.vo.PageVO;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * <p>
 * 诊疗开方系统--患者档案信息表
 * </p>
 *
 * @author xph
 * @since 2025-07-23
 */
@Data
public class PatientBaseQueryVO extends PageVO implements Serializable {

    private static final long serialVersionUID = 2148713154921734234L;

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
     * 就诊卡号
     */
    private String clinicCode;

    /**
     * 住院号
     */
    private String inpatientNumber;

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
     * 状态(0:禁用,1:启用)
     */
    private Integer status;

    /**
     * 诊疗系统中患者流水表的主键id
     */
    private Long patientVisitId;

    /**
     * 主治医生id（诊疗系统用户id）json值
     */
    private List<Long> attendingDoctorIds;

}
