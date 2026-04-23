package com.tcm.rx.service.patient.impl;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson2.JSONArray;
import com.google.common.collect.Lists;
import com.tcm.common.core.UserContextHolder;
import com.tcm.common.entity.BaseUser;
import com.tcm.common.enums.BooleanEnum;
import com.tcm.common.exception.ServiceException;
import com.tcm.rx.entity.auth.CUser;
import com.tcm.rx.entity.patient.PatientVisit;
import com.tcm.rx.mapper.patient.PatientVisitMapper;
import com.tcm.rx.service.auth.ICUserService;
import com.tcm.rx.service.patient.IPatientVisitService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tcm.rx.vo.patient.request.PatientVisitQueryVO;
import com.tcm.rx.vo.patient.response.PatientVisitVO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>
 * 诊疗开方系统--患者就诊流水表 服务实现类
 * </p>
 *
 * @author xph
 * @since 2025-07-23
 */
@Service
public class PatientVisitServiceImpl extends ServiceImpl<PatientVisitMapper, PatientVisit>
        implements IPatientVisitService {
    @Resource
    private ICUserService userService;

    @Override
    public List<PatientVisitVO> patientVisitList(PatientVisitQueryVO queryVO) {
        if ((Objects.isNull(queryVO.getMedicalGroupId()) || queryVO.getMedicalGroupId() < 1)
                && StringUtils.isBlank(queryVO.getMedicalGroupCode())) {
            BaseUser loginUser = UserContextHolder.getUserInfoContext();
            queryVO.setMedicalGroupId(loginUser.getMedicalGroupId());
            queryVO.setMedicalGroupCode(loginUser.getMedicalGroupCode());
        }
        List<PatientVisitVO> patientVisitVOS = baseMapper.patientVisitList(queryVO);
        List<Long> doctorIdList = patientVisitVOS.stream()
                .map(PatientVisitVO::getAttendingDoctorId)
                .filter(Objects::nonNull)
                .filter(attendingDoctorId -> !attendingDoctorId.trim().isEmpty())
                .map(JSONArray::parseArray)
                .flatMap(jsonArray -> jsonArray.stream()
                        .map(obj -> Long.valueOf(obj.toString())))
                .collect(Collectors.toList());
        if (ObjectUtil.isEmpty(doctorIdList)) {
            doctorIdList.add(-1L);
        }
        Map<Long, String> userMap = userService.listByIds(doctorIdList)
                .stream().collect(Collectors.toMap(CUser::getId, CUser::getRealName));
        // 设置医生姓名
        patientVisitVOS.forEach(visit -> {
            String attendingDoctorId = visit.getAttendingDoctorId();
            if (attendingDoctorId == null || attendingDoctorId.trim().isEmpty()) {
                visit.setDoctorName(null);
                return;
            }
            try {
                JSONArray doctorIdArray = JSONArray.parseArray(attendingDoctorId);
                if (doctorIdArray != null && !doctorIdArray.isEmpty()) {
                    // 拼接多个医生姓名（用逗号分隔）
                    String doctorNames = doctorIdArray.stream()
                            .filter(Objects::nonNull)
                            .map(obj -> Long.valueOf(obj.toString()))
                            .map(userMap::get)
                            .filter(Objects::nonNull) // 过滤不存在的医生姓名
                            .collect(Collectors.joining(","));
                    visit.setDoctorName(doctorNames);
                }
            } catch (Exception e) {
                visit.setDoctorName(null);
            }
        });
        return patientVisitVOS;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long insertPatientVisit(PatientVisitVO patientVisitVO) {
        if (Objects.isNull(patientVisitVO)) {
            throw new ServiceException("参数不能为空");
        }

        if (Objects.isNull(patientVisitVO.getMedicalGroupId()) || patientVisitVO.getMedicalGroupId() < 1
                || StringUtils.isBlank(patientVisitVO.getMedicalGroupCode())) {
            throw new ServiceException("医联体参数不能为空");
        }
        if (Objects.isNull(patientVisitVO.getHspId()) || patientVisitVO.getHspId() < 1
                || StringUtils.isBlank(patientVisitVO.getHspCode())) {
            throw new ServiceException("医疗机构参数不能为空");
        }
        if (StringUtils.isBlank(patientVisitVO.getPatientId())) {
            throw new ServiceException("HIS患者id参数不能为空");
        }
        if (StringUtils.isBlank(patientVisitVO.getClinicCode())) {
            throw new ServiceException("就诊卡号参数不能为空");
        }
        if (StringUtils.isBlank(patientVisitVO.getIdNumber())) {
            throw new ServiceException("患者身份证号码参数不能为空");
        }
        if (StringUtils.isBlank(patientVisitVO.getPhone())) {
            throw new ServiceException("患者手机号参数不能为空");
        }

        List<PatientVisit> dbPatientVisitList = this.lambdaQuery()
                .eq(Objects.nonNull(patientVisitVO.getMedicalGroupId()) && patientVisitVO.getMedicalGroupId() > 0,
                        PatientVisit::getMedicalGroupId, patientVisitVO.getMedicalGroupId())
                .eq(StringUtils.isNotBlank(patientVisitVO.getMedicalGroupCode()),
                        PatientVisit::getMedicalGroupCode, patientVisitVO.getMedicalGroupCode())
                .eq(Objects.nonNull(patientVisitVO.getHspId()) && patientVisitVO.getHspId() > 0,
                        PatientVisit::getHspId, patientVisitVO.getHspId())
                .eq(StringUtils.isNotBlank(patientVisitVO.getHspCode()),
                        PatientVisit::getHspCode, patientVisitVO.getHspCode())
                .eq(PatientVisit::getPatientId, patientVisitVO.getPatientId())
                .eq(PatientVisit::getClinicCode, patientVisitVO.getClinicCode())
                .eq(PatientVisit::getIdNumber, patientVisitVO.getIdNumber())
                .eq(PatientVisit::getPhone, patientVisitVO.getPhone())
                .eq(PatientVisit::getDelFlag, BooleanEnum.FALSE.getNum())
                .list();
        if (CollectionUtils.isNotEmpty(dbPatientVisitList)) {
            throw new ServiceException("患者就诊流水，已存在");
        }

        PatientVisit patientVisit = new PatientVisit();
        BeanUtils.copyProperties(patientVisitVO, patientVisit);

        BaseUser loginUser = UserContextHolder.getUserInfoContext();
        if (Objects.nonNull(loginUser)) {
            patientVisit.setCreateBy(loginUser.getId().toString());
            patientVisit.setUpdateBy(loginUser.getId().toString());
        } else {
            patientVisit.setCreateBy("0");
            patientVisit.setUpdateBy("0");
        }

        Date now = new Date();
        patientVisit.setCreateTime(now);
        patientVisit.setUpdateTime(now);

        int insertResult = this.getBaseMapper().insert(patientVisit);
        return insertResult > 0 ? patientVisit.getId() : null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long updatePatientVisit(PatientVisitVO patientVisitVO) {
        if (Objects.isNull(patientVisitVO) || Objects.isNull(patientVisitVO.getId())) {
            throw new ServiceException("参数不能为空");
        }

        List<PatientVisit> dbPatientVisitList = this.lambdaQuery()
                .ne(PatientVisit::getId, patientVisitVO.getId())
                .eq(Objects.nonNull(patientVisitVO.getMedicalGroupId()) && patientVisitVO.getMedicalGroupId() > 0,
                        PatientVisit::getMedicalGroupId, patientVisitVO.getMedicalGroupId())
                .eq(StringUtils.isNotBlank(patientVisitVO.getMedicalGroupCode()),
                        PatientVisit::getMedicalGroupCode, patientVisitVO.getMedicalGroupCode())
                .eq(Objects.nonNull(patientVisitVO.getHspId()) && patientVisitVO.getHspId() > 0,
                        PatientVisit::getHspId, patientVisitVO.getHspId())
                .eq(StringUtils.isNotBlank(patientVisitVO.getHspCode()),
                        PatientVisit::getHspCode, patientVisitVO.getHspCode())
                .eq(PatientVisit::getPatientId, patientVisitVO.getPatientId())
                .eq(PatientVisit::getInpatientCode, patientVisitVO.getInpatientCode())
                .eq(PatientVisit::getInpatientNumber, patientVisitVO.getInpatientNumber())
                .eq(PatientVisit::getClinicCode, patientVisitVO.getClinicCode())
                .eq(PatientVisit::getIdNumber, patientVisitVO.getIdNumber())
                .eq(PatientVisit::getPhone, patientVisitVO.getPhone())
                .eq(PatientVisit::getDelFlag, BooleanEnum.FALSE.getNum())
                .list();
        if (CollectionUtils.isNotEmpty(dbPatientVisitList)) {
            if(ObjectUtil.isEmpty(patientVisitVO.getId())
                    || patientVisitVO.getId().longValue() !=  dbPatientVisitList.get(0).getId().longValue()) {
                throw new ServiceException("患者就诊流水，已存在");
            }
        }

        PatientVisit patientVisit = new PatientVisit();
        BeanUtils.copyProperties(patientVisitVO, patientVisit);

        BaseUser loginUser = UserContextHolder.getUserInfoContext();
        if (Objects.nonNull(loginUser)) {
            patientVisit.setUpdateBy(loginUser.getId().toString());
        } else {
            patientVisit.setUpdateBy("0");
        }

        patientVisit.setUpdateTime(new Date());
        return this.saveOrUpdate(patientVisit) ? patientVisit.getId() : null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePatientVisit(Long id) {
        if (Objects.isNull(id) || id <= 0) {
            throw new ServiceException("id参数不能为空");
        }

        PatientVisit patientVisit = this.getById(id);
        if (ObjectUtils.isEmpty(patientVisit)) {
            throw new ServiceException("数据不存在");
        }

        this.getBaseMapper().deleteById(id);
    }

}
