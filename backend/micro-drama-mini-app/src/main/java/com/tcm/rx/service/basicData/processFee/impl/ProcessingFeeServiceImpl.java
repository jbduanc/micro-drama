package com.tcm.rx.service.basicData.processFee.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.github.pagehelper.Page;
import com.tcm.common.core.UserContextHolder;
import com.tcm.common.entity.TablePageInfo;
import com.tcm.common.enums.BooleanEnum;
import com.tcm.common.exception.ServiceException;
import com.tcm.rx.entity.basicData.ProcessingFee;
import com.tcm.rx.mapper.basicData.ProcessingFeeMapper;
import com.tcm.rx.service.basicData.processFee.ProcessingFeeService;
import com.tcm.rx.vo.basicData.ProcessingFeeVo.ProcessingFeeEditVO;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;

import java.util.Date;
import java.util.List;

import static com.github.pagehelper.page.PageMethod.startPage;


/**
 * 诊疗开方系统--加工费管理表(ProcessingFee)表服务实现类
 *
 * @author duanqiyuan
 * @since 2025-07-17 14:58:54
 */
@Service("processingFeeService")
@RequiredArgsConstructor
public class ProcessingFeeServiceImpl extends ServiceImpl<ProcessingFeeMapper, ProcessingFee> implements ProcessingFeeService {

    private final ProcessingFeeMapper processingFeeMapper;

    /**
     * 加工费管理-新增加工费
     *
     * @param processingFeeAddVO
     * @return
     */
    @Override
    public Long saveProcessingFee(ProcessingFeeEditVO processingFeeAddVO) {
        Long medicalGroupId = UserContextHolder.getUserInfoContext().getMedicalGroupId();
        String medicalGroupCode = UserContextHolder.getUserInfoContext().getMedicalGroupCode();
        Integer count = this.lambdaQuery().eq(ProcessingFee::getDecoctRequire, processingFeeAddVO.getDecoctRequire())
                .eq(ProcessingFee::getMedicalGroupId, medicalGroupId)
                .eq(ProcessingFee::getMedicalGroupCode, medicalGroupCode)
                .eq(ProcessingFee::getDelFlag, BooleanEnum.FALSE.getNum())
                .count();
        if (count > 0) {
            throw new ServiceException("该煎药要求已存在");
        }
        ProcessingFee processingFee = new ProcessingFee();
        processingFee.setMedicalGroupId(medicalGroupId);
        processingFee.setMedicalGroupCode(medicalGroupCode);
        processingFee.setDecoctRequire(processingFeeAddVO.getDecoctRequire());
        processingFee.setPricingMethod(processingFeeAddVO.getPricingMethod());
        processingFee.setFee(processingFeeAddVO.getFee());
        processingFee.setChargeItemCode(processingFeeAddVO.getChargeItemCode());
        processingFee.setDelFlag(BooleanEnum.FALSE.getNum());
        processingFee.setCreateBy(String.valueOf(UserContextHolder.getUserInfoContext().getId()));
        processingFee.setUpdateBy(String.valueOf(UserContextHolder.getUserInfoContext().getId()));
        this.baseMapper.insert(processingFee);
        return processingFee.getId();
    }

    /**
     * 加工费管理-编辑加工费
     *
     * @param processingFeeAddVO
     * @return
     */
    @Override
    public Long updateProcessingFee(ProcessingFeeEditVO processingFeeAddVO) {
        Long medicalGroupId = UserContextHolder.getUserInfoContext().getMedicalGroupId();
        String medicalGroupCode = UserContextHolder.getUserInfoContext().getMedicalGroupCode();
        ProcessingFee oldPro = this.lambdaQuery().eq(ProcessingFee::getDecoctRequire, processingFeeAddVO.getDecoctRequire())
                .eq(ProcessingFee::getMedicalGroupId, medicalGroupId)
                .eq(ProcessingFee::getMedicalGroupCode, medicalGroupCode)
                .eq(ProcessingFee::getDelFlag, BooleanEnum.FALSE.getNum())
                .one();
        if (ObjectUtil.isNotEmpty(oldPro)) {
            if (oldPro.getId() != processingFeeAddVO.getId()) {//表明这个煎药要求已经被使用了
                throw new ServiceException("该煎药要求已存在");
            }
        }
        ProcessingFee oldProcessingFee = this.getById(processingFeeAddVO.getId());
        oldProcessingFee.setDecoctRequire(processingFeeAddVO.getDecoctRequire());
        oldProcessingFee.setPricingMethod(processingFeeAddVO.getPricingMethod());
        oldProcessingFee.setFee(processingFeeAddVO.getFee());
        oldProcessingFee.setChargeItemCode(processingFeeAddVO.getChargeItemCode());
        oldProcessingFee.setUpdateBy(String.valueOf(UserContextHolder.getUserInfoContext().getId()));
        oldProcessingFee.setUpdateTime(new Date());
        this.baseMapper.updateById(oldProcessingFee);
        return oldProcessingFee.getId();
    }

    /**
     * 加工费管理-删除加工费
     *
     * @param id
     * @return
     */
    @DeleteMapping
    @Override
    public Boolean delProcessingFee(Long id) {
        ProcessingFee processingFee = this.getById(id);
        processingFee.setDelFlag(BooleanEnum.TRUE.getNum());
        processingFee.setUpdateBy(String.valueOf(UserContextHolder.getUserInfoContext().getId()));
        processingFee.setUpdateTime(new Date());
        this.baseMapper.updateById(processingFee);
        return BooleanEnum.TRUE.getBool();
    }

    @Override
    public TablePageInfo<ProcessingFeeEditVO> selProcessingFee(Long id, String decoctRequire, Integer page, Integer size) {
        Page pageObj = startPage(page, size);
        Long medicalGroupId = UserContextHolder.getUserInfoContext().getMedicalGroupId();
        String medicalGroupCode = UserContextHolder.getUserInfoContext().getMedicalGroupCode();
        List<ProcessingFeeEditVO> processingFeeEditVOS = processingFeeMapper.selProcessingFee(id, decoctRequire,medicalGroupId,medicalGroupCode);
        return new TablePageInfo<>(processingFeeEditVOS, Math.toIntExact(pageObj.getTotal()));
    }
}

