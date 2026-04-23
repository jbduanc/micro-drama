package com.tcm.rx.mapper.cons;

import com.tcm.rx.entity.cons.Consultation;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tcm.rx.vo.cons.request.ConsQueryVO;
import com.tcm.rx.vo.cons.response.ConsVO;

import java.util.List;

/**
 * <p>
 * 诊疗开方系统--会诊表 Mapper 接口
 * </p>
 *
 * @author djbo
 * @since 2025-09-04
 */
public interface ConsultationMapper extends BaseMapper<Consultation> {

    /**
     * 分页查询会诊列表（带条件）
     */
    List<ConsVO> selectByQuery(ConsQueryVO queryVO);
}
