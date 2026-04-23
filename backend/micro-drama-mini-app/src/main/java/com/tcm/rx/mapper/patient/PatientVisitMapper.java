package com.tcm.rx.mapper.patient;

import com.tcm.rx.entity.patient.PatientVisit;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tcm.rx.vo.patient.request.PatientVisitQueryVO;
import com.tcm.rx.vo.patient.response.PatientVisitVO;

import java.util.List;

/**
 * <p>
 * 诊疗开方系统--患者就诊流水表 Mapper 接口
 * </p>
 *
 * @author xph
 * @since 2025-07-23
 */
public interface PatientVisitMapper extends BaseMapper<PatientVisit> {

    /**
     * 查询患者就诊流水数据
     */
    List<PatientVisitVO> patientVisitList(PatientVisitQueryVO queryVO);

}
