package com.tcm.rx.mapper.patient;

import com.tcm.rx.entity.patient.PatientBase;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tcm.rx.vo.patient.request.PatientBaseQueryVO;
import com.tcm.rx.vo.patient.response.PatientBaseVO;

import java.util.List;

/**
 * <p>
 * 诊疗开方系统--患者档案信息表 Mapper 接口
 * </p>
 *
 * @author xph
 * @since 2025-07-23
 */
public interface PatientBaseMapper extends BaseMapper<PatientBase> {

    /**
     * 查询患者档案信息数据
     */
    List<PatientBaseVO> patientBaseList(PatientBaseQueryVO queryVO);

    /**
     * 根据条件（患者流水信息）查询患者档案信息数据
     */
    List<PatientBaseVO> patientBaseListByCondition(PatientBaseQueryVO queryVO);

}
