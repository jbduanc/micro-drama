package com.tcm.rx.mapper.patient;

import com.tcm.rx.entity.patient.MedrecTplMgmt;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tcm.rx.vo.patient.request.MedrecTplMgmtQueryVO;
import com.tcm.rx.vo.patient.response.MedrecTplMgmtVO;

import java.util.List;

/**
 * <p>
 * 诊疗开方系统--病历模版管理表 Mapper 接口
 * </p>
 *
 * @author djbo
 * @since 2025-09-11
 */
public interface MedrecTplMgmtMapper extends BaseMapper<MedrecTplMgmt> {

   List<MedrecTplMgmtVO> selectByQuery(MedrecTplMgmtQueryVO queryVO);
}
