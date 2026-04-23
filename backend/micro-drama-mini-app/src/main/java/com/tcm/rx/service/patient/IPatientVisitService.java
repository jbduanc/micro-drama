package com.tcm.rx.service.patient;

import com.tcm.rx.entity.patient.PatientVisit;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tcm.rx.vo.patient.request.PatientVisitQueryVO;
import com.tcm.rx.vo.patient.response.PatientVisitVO;

import java.util.List;

/**
 * <p>
 * 诊疗开方系统--患者就诊流水表 服务类
 * </p>
 *
 * @author xph
 * @since 2025-07-23
 */
public interface IPatientVisitService extends IService<PatientVisit> {

    /**
     * 查询患者就诊流水数据
     */
    List<PatientVisitVO> patientVisitList(PatientVisitQueryVO queryVO);

    /**
     * 新增患者就诊流水
     */
    Long insertPatientVisit(PatientVisitVO patientVisitVO);

    /**
     * 更新患者就诊流水
     */
    Long updatePatientVisit(PatientVisitVO patientVisitVO);

    /**
     *  删除患者就诊流水
     */
    void deletePatientVisit(Long id);

}
