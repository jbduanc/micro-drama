package com.tcm.rx.mapper.basicData;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tcm.rx.entity.basicData.ProcessingFee;
import com.tcm.rx.vo.basicData.ProcessingFeeVo.ProcessingFeeEditVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ProcessingFeeMapper extends BaseMapper<ProcessingFee> {
    List<ProcessingFeeEditVO> selProcessingFee(@Param("id") Long id, @Param("decoctRequire") String decoctRequire,@Param("medicalGroupId") Long medicalGroupId,@Param("medicalGroupCode") String medicalGroupCode);
}
