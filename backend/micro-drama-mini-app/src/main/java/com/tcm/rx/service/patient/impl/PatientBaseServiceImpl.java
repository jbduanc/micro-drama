package com.tcm.rx.service.patient.impl;

import com.tcm.common.core.UserContextHolder;
import com.tcm.common.entity.BaseUser;
import com.tcm.common.enums.BooleanEnum;
import com.tcm.common.exception.ServiceException;
import com.tcm.rx.entity.patient.PatientBase;
import com.tcm.rx.mapper.patient.PatientBaseMapper;
import com.tcm.rx.service.patient.IPatientBaseService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tcm.rx.vo.patient.request.PatientBaseQueryVO;
import com.tcm.rx.vo.patient.response.PatientBaseVO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>
 * 诊疗开方系统--患者档案信息表 服务实现类
 * </p>
 *
 * @author xph
 * @since 2025-07-23
 */
@Service
public class PatientBaseServiceImpl extends ServiceImpl<PatientBaseMapper, PatientBase>
        implements IPatientBaseService {

    @Override
    public List<PatientBaseVO> patientBaseList(PatientBaseQueryVO queryVO) {
        if ((Objects.isNull(queryVO.getMedicalGroupId()) || queryVO.getMedicalGroupId() < 1)
                && StringUtils.isBlank(queryVO.getMedicalGroupCode())) {
            BaseUser loginUser = UserContextHolder.getUserInfoContext();
            queryVO.setMedicalGroupId(loginUser.getMedicalGroupId());
            queryVO.setMedicalGroupCode(loginUser.getMedicalGroupCode());
        }
        return this.baseMapper.patientBaseList(queryVO);
    }

    @Override
    public List<PatientBaseVO> patientBaseListByCondition(PatientBaseQueryVO queryVO) {
        if ((Objects.isNull(queryVO.getMedicalGroupId()) || queryVO.getMedicalGroupId() < 1)
                && StringUtils.isBlank(queryVO.getMedicalGroupCode())) {
            BaseUser loginUser = UserContextHolder.getUserInfoContext();
            queryVO.setMedicalGroupId(loginUser.getMedicalGroupId());
            queryVO.setMedicalGroupCode(loginUser.getMedicalGroupCode());
        }
        return this.baseMapper.patientBaseListByCondition(queryVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long insertPatientBase(PatientBaseVO patientBaseVO) {
        if (Objects.isNull(patientBaseVO)) {
            throw new ServiceException("参数不能为空");
        }

        if (Objects.isNull(patientBaseVO.getMedicalGroupId()) || patientBaseVO.getMedicalGroupId() < 1
                || StringUtils.isBlank(patientBaseVO.getMedicalGroupCode())) {
            throw new ServiceException("医联体参数不能为空");
        }
        if (Objects.isNull(patientBaseVO.getHspId()) || patientBaseVO.getHspId() < 1
                || StringUtils.isBlank(patientBaseVO.getHspCode())) {
            throw new ServiceException("医疗机构参数不能为空");
        }
        if (StringUtils.isBlank(patientBaseVO.getPatientId())) {
            throw new ServiceException("HIS患者id参数不能为空");
        }
        if (StringUtils.isBlank(patientBaseVO.getClinicCode())) {
            throw new ServiceException("就诊卡号参数不能为空");
        }
        if (StringUtils.isBlank(patientBaseVO.getIdNumber())) {
            throw new ServiceException("患者身份证号码参数不能为空");
        }
        if (StringUtils.isBlank(patientBaseVO.getPhone())) {
            throw new ServiceException("患者手机号参数不能为空");
        }

        List<PatientBase> dbPatientBaseList = this.lambdaQuery()
                .eq(Objects.nonNull(patientBaseVO.getMedicalGroupId()) && patientBaseVO.getMedicalGroupId() > 0,
                        PatientBase::getMedicalGroupId, patientBaseVO.getMedicalGroupId())
                .eq(StringUtils.isNotBlank(patientBaseVO.getMedicalGroupCode()),
                        PatientBase::getMedicalGroupCode, patientBaseVO.getMedicalGroupCode())
                .eq(Objects.nonNull(patientBaseVO.getHspId()) && patientBaseVO.getHspId() > 0,
                        PatientBase::getHspId, patientBaseVO.getHspId())
                .eq(StringUtils.isNotBlank(patientBaseVO.getHspCode()),
                        PatientBase::getHspCode, patientBaseVO.getHspCode())
                .eq(PatientBase::getDelFlag, BooleanEnum.FALSE.getNum())
                .and(sql -> sql.eq(PatientBase::getPatientId, patientBaseVO.getPatientId())
                        .or().eq(PatientBase::getClinicCode, patientBaseVO.getClinicCode())
                        .or().eq(PatientBase::getIdNumber, patientBaseVO.getIdNumber())
                        .or().eq(PatientBase::getPhone, patientBaseVO.getPhone())
                )
                .list();
        if (CollectionUtils.isNotEmpty(dbPatientBaseList)) {
            List<PatientBase> patientIdList = dbPatientBaseList.stream()
                    .filter(o -> StringUtils.isNotBlank(o.getPatientId())
                            && patientBaseVO.getPatientId().equals(o.getPatientId()))
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(patientIdList)) {
                throw new ServiceException("HIS患者id，已存在");
            }
            List<PatientBase> clinicCodeList = dbPatientBaseList.stream()
                    .filter(o -> StringUtils.isNotBlank(o.getClinicCode())
                            && patientBaseVO.getClinicCode().equals(o.getClinicCode()))
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(clinicCodeList)) {
                throw new ServiceException("就诊卡号，已存在");
            }
            List<PatientBase> idNumberList = dbPatientBaseList.stream()
                    .filter(o -> StringUtils.isNotBlank(o.getIdNumber())
                            && patientBaseVO.getIdNumber().equals(o.getIdNumber()))
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(idNumberList)) {
                throw new ServiceException("患者身份证号码，已存在");
            }
            List<PatientBase> phoneList = dbPatientBaseList.stream()
                    .filter(o -> StringUtils.isNotBlank(o.getPhone())
                            && patientBaseVO.getPhone().equals(o.getPhone()))
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(phoneList)) {
                throw new ServiceException("患者手机号，已存在");
            }
        }

        PatientBase patientBase = new PatientBase();
        BeanUtils.copyProperties(patientBaseVO, patientBase);

        BaseUser loginUser = UserContextHolder.getUserInfoContext();
        if (Objects.nonNull(loginUser)) {
            patientBase.setCreateBy(loginUser.getId().toString());
            patientBase.setUpdateBy(loginUser.getId().toString());
        } else {
            patientBase.setCreateBy("0");
            patientBase.setUpdateBy("0");
        }

        Date now = new Date();
        patientBase.setCreateTime(now);
        patientBase.setUpdateTime(now);

        int insertResult = this.getBaseMapper().insert(patientBase);
        return insertResult > 0 ? patientBase.getId() : null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long updatePatientBase(PatientBaseVO patientBaseVO) {
        if (Objects.isNull(patientBaseVO) || Objects.isNull(patientBaseVO.getId())) {
            throw new ServiceException("参数不能为空");
        }

        List<PatientBase> dbPatientBaseList = this.lambdaQuery()
                .ne(PatientBase::getId, patientBaseVO.getId())
                .eq(Objects.nonNull(patientBaseVO.getMedicalGroupId()) && patientBaseVO.getMedicalGroupId() > 0,
                        PatientBase::getMedicalGroupId, patientBaseVO.getMedicalGroupId())
                .eq(StringUtils.isNotBlank(patientBaseVO.getMedicalGroupCode()),
                        PatientBase::getMedicalGroupCode, patientBaseVO.getMedicalGroupCode())
                .eq(Objects.nonNull(patientBaseVO.getHspId()) && patientBaseVO.getHspId() > 0,
                        PatientBase::getHspId, patientBaseVO.getHspId())
                .eq(StringUtils.isNotBlank(patientBaseVO.getHspCode()),
                        PatientBase::getHspCode, patientBaseVO.getHspCode())
                .eq(PatientBase::getDelFlag, BooleanEnum.FALSE.getNum())
                .and(sql -> sql.eq(PatientBase::getPatientId, patientBaseVO.getPatientId())
                        .or().eq(PatientBase::getClinicCode, patientBaseVO.getClinicCode())
                        .or().eq(PatientBase::getIdNumber, patientBaseVO.getIdNumber())
                        .or().eq(PatientBase::getPhone, patientBaseVO.getPhone())
                )
                .list();
        if (CollectionUtils.isNotEmpty(dbPatientBaseList)) {
            List<PatientBase> patientIdList = dbPatientBaseList.stream()
                    .filter(o -> StringUtils.isNotBlank(o.getPatientId())
                            && patientBaseVO.getPatientId().equals(o.getPatientId()))
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(patientIdList)) {
                throw new ServiceException("HIS患者id，已存在");
            }
            List<PatientBase> clinicCodeList = dbPatientBaseList.stream()
                    .filter(o -> StringUtils.isNotBlank(o.getClinicCode())
                            && patientBaseVO.getClinicCode().equals(o.getClinicCode()))
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(clinicCodeList)) {
                throw new ServiceException("就诊卡号，已存在");
            }
            List<PatientBase> idNumberList = dbPatientBaseList.stream()
                    .filter(o -> StringUtils.isNotBlank(o.getIdNumber())
                            && patientBaseVO.getIdNumber().equals(o.getIdNumber()))
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(idNumberList)) {
                throw new ServiceException("患者身份证号码，已存在");
            }
            List<PatientBase> phoneList = dbPatientBaseList.stream()
                    .filter(o -> StringUtils.isNotBlank(o.getPhone())
                            && patientBaseVO.getPhone().equals(o.getPhone()))
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(phoneList)) {
                throw new ServiceException("患者手机号，已存在");
            }
        }

        PatientBase patientBase = new PatientBase();
        BeanUtils.copyProperties(patientBaseVO, patientBase);

        BaseUser loginUser = UserContextHolder.getUserInfoContext();
        if (Objects.nonNull(loginUser)) {
            patientBase.setUpdateBy(loginUser.getId().toString());
        } else {
            patientBase.setUpdateBy("0");
        }

        patientBase.setUpdateTime(new Date());
        return this.saveOrUpdate(patientBase) ? patientBase.getId() : null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePatientBase(Long id) {
        if (Objects.isNull(id) || id <= 0){
            throw new ServiceException("id参数不能为空");
        }

        PatientBase patientBase = this.getById(id);
        if (ObjectUtils.isEmpty(patientBase)){
            throw new ServiceException("数据不存在");
        }

        this.getBaseMapper().deleteById(id);
    }

}
