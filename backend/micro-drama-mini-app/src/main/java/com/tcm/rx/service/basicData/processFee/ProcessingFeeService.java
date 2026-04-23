package com.tcm.rx.service.basicData.processFee;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tcm.common.entity.TablePageInfo;
import com.tcm.rx.entity.basicData.ProcessingFee;
import com.tcm.rx.vo.basicData.ProcessingFeeVo.ProcessingFeeEditVO;

import java.util.List;


/**
 * 诊疗开方系统--加工费管理表(ProcessingFee)表服务接口
 *
 * @author duanqiyuan
 * @since 2025-07-17 14:58:54
 */
public interface ProcessingFeeService extends IService<ProcessingFee> {

    /**
     * 加工费管理-新增加工费
     *
     * @param processingFeeAddVO
     * @return
     */
    Long saveProcessingFee(ProcessingFeeEditVO processingFeeAddVO);

    /**
     * 加工费管理-编辑加工费
     *
     * @param processingFeeAddVO
     * @return
     */
    Long updateProcessingFee(ProcessingFeeEditVO processingFeeAddVO);

    /**
     * 加工费管理-删除加工费
     *
     * @param processingFeeAddVO
     * @return
     */
    Boolean delProcessingFee(Long id);

    /**
     * 加工费管理-查找加工费
     *
     * @param id
     * @param decoctRequire
     * @return
     */
    TablePageInfo<ProcessingFeeEditVO> selProcessingFee(Long id, String decoctRequire, Integer page, Integer size);
}

