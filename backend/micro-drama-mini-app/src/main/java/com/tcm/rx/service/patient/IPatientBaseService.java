package com.tcm.rx.service.patient;

import com.tcm.rx.entity.patient.PatientBase;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tcm.rx.vo.patient.request.PatientBaseQueryVO;
import com.tcm.rx.vo.patient.response.PatientBaseVO;

import java.util.List;

/**
 * <p>
 * 诊疗开方系统--患者档案信息表 服务类
 * </p>
 *
 * @author xph
 * @since 2025-07-23
 */
public interface IPatientBaseService extends IService<PatientBase> {

    /**
     * 查询患者档案信息数据
     */
    List<PatientBaseVO> patientBaseList(PatientBaseQueryVO queryVO);

    /**
     * 根据条件（患者流水信息）查询患者档案信息数据
     */
    List<PatientBaseVO> patientBaseListByCondition(PatientBaseQueryVO queryVO);

    /**
     * 新增患者档案信息
     */
    Long insertPatientBase(PatientBaseVO patientBaseVO);

    /**
     * 更新患者档案信息
     */
    Long updatePatientBase(PatientBaseVO patientBaseVO);

    /**
     *  删除患者档案信息
     */
    void deletePatientBase(Long id);

}
