package com.tcm.rx.service.patient.impl;

import com.alibaba.fastjson2.JSONArray;
import com.tcm.common.enums.BooleanEnum;
import com.tcm.common.exception.ServiceException;
import com.tcm.common.patient.request.HisInfoUploadDTO;
import com.tcm.common.patient.response.PatientHisRespDTO;
import com.tcm.common.utils.RsaUtils;
import com.tcm.rx.entity.auth.CRole;
import com.tcm.rx.entity.auth.CUser;
import com.tcm.rx.entity.auth.CUserDept;
import com.tcm.rx.entity.auth.CUserRole;
import com.tcm.rx.entity.dept.Dept;
import com.tcm.rx.entity.hsp.Hsp;
import com.tcm.rx.entity.patient.PatientBase;
import com.tcm.rx.entity.patient.PatientVisit;
import com.tcm.rx.service.auth.ICRoleService;
import com.tcm.rx.service.auth.ICUserDeptService;
import com.tcm.rx.service.auth.ICUserRoleService;
import com.tcm.rx.service.auth.ICUserService;
import com.tcm.rx.service.dept.IDeptService;
import com.tcm.rx.service.hsp.IHspService;
import com.tcm.rx.service.patient.IPatientBaseService;
import com.tcm.rx.service.patient.IPatientVisitService;
import com.tcm.rx.service.patient.PatientService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Objects;

/**
 * @Description 诊疗开方系统--跳转中药诊疗系统的患者和医生数据 对接接口
 * @Author xph
 * @Date 2025/7/23 17:09
 */
@Service
public class PatientServiceImpl implements PatientService {

    @Resource
    private IPatientBaseService patientBaseService;

    @Resource
    private IPatientVisitService patientVisitService;

    @Resource
    private IHspService hspService;

    @Resource
    private IDeptService deptService;

    @Resource
    private ICUserService userService;

    @Resource
    private ICRoleService roleService;

    @Resource
    private ICUserRoleService cUserRoleService;

    @Resource
    private ICUserDeptService cUserDeptService;

    // his上传患者和医生信息跳转，默认的角色code
    @Value("${his.infoUpload.roleCode}")
    private String hisInfoUploadRoleCode;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PatientHisRespDTO patientInfoUpload(HisInfoUploadDTO hisInfoUploadDTO) throws Exception {
        // 校验入参
        this.checkReqParams(hisInfoUploadDTO);

        // 查询医疗机构的信息
        Hsp hsp = hspService.lambdaQuery()
                .eq(Hsp::getHspCode, hisInfoUploadDTO.getHspCode())
                .eq(Hsp::getMedicalGroupCode, hisInfoUploadDTO.getMedicalGroupCode())
                .eq(Hsp::getDelFlag, BooleanEnum.FALSE.getNum())
                .orderByDesc(Hsp::getId)
                .last("limit 1")
                .one();
        if (ObjectUtils.isEmpty(hsp)) {
            throw new ServiceException("请输入正确的医联体编码和医疗机构编码");
        }

        // 构建输出结果对象
        PatientHisRespDTO result = new PatientHisRespDTO();
        hisInfoUploadDTO.getDoctorUserDTO()
                .setUserAccount(hsp.getHspCode() + "_" + hisInfoUploadDTO.getDoctorUserDTO().getUserAccount());

        // 处理医生用户的数据
        CUser user = this.handleDoctorUserInfo(hisInfoUploadDTO, hsp);
        // rsa加密参数值
        result.setUserId(RsaUtils.encrypt(user.getId().toString(),
                RsaUtils.getPublicKey(RsaUtils.getRsaPublicKey())));
        result.setUserAccount(RsaUtils.encrypt(user.getUserAccount(),
                RsaUtils.getPublicKey(RsaUtils.getRsaPublicKey())));

        // 处理患者信息的数据
        PatientVisit patientVisit = this.handlePatientInfo(hisInfoUploadDTO, hsp, user);
        result.setPatientVisitId(patientVisit.getId());
        return result;
    }

    /**
     * 校验入参
     */
    private void checkReqParams(HisInfoUploadDTO hisInfoUploadDTO) {
        if (Objects.isNull(hisInfoUploadDTO)) {
            throw new ServiceException("入参对象不能为空");
        }
        if (StringUtils.isBlank(hisInfoUploadDTO.getMedicalGroupCode())) {
            throw new ServiceException("医联体编码参数不能为空");
        }
        if (StringUtils.isBlank(hisInfoUploadDTO.getHspCode())) {
            throw new ServiceException("医疗机构编码参数不能为空");
        }
        if (Objects.isNull(hisInfoUploadDTO.getDoctorUserDTO())){
            throw new ServiceException("医生用户对象参数不能为空");
        }
        // 加好前缀的账号，His前缀+His用户名，His前缀由我们给他们分配前缀。
        if (StringUtils.isBlank(hisInfoUploadDTO.getDoctorUserDTO().getUserAccount())){
            throw new ServiceException("医生用户账号参数不能为空");
        }
        if (Objects.isNull(hisInfoUploadDTO.getPatientInfoDTO())){
            throw new ServiceException("患者信息对象不能为空");
        }
        if (Objects.isNull(hisInfoUploadDTO.getPatientInfoDTO().getTreatmentType())){
            throw new ServiceException("就诊类型参数不能为空");
        }
        if (StringUtils.isBlank(hisInfoUploadDTO.getPatientInfoDTO().getInpatientCode())){
            throw new ServiceException("就诊流水号参数不能为空");
        }
        if (StringUtils.isBlank(hisInfoUploadDTO.getPatientInfoDTO().getPatientId())){
            throw new ServiceException("HIS患者id参数不能为空");
        }
        if (StringUtils.isBlank(hisInfoUploadDTO.getPatientInfoDTO().getPatientName())){
            throw new ServiceException("患者姓名参数不能为空");
        }
    }

    /**
     * 处理医生用户的数据
     */
    private CUser handleDoctorUserInfo(HisInfoUploadDTO hisInfoUploadDTO, Hsp hsp) {
        // 查询用户表中的医生用户信息
        CUser dbUser = userService.lambdaQuery()
                .eq(CUser::getMedicalGroupCode, hisInfoUploadDTO.getMedicalGroupCode())
                .eq(CUser::getHspCode, hisInfoUploadDTO.getHspCode())
                .eq(CUser::getUserAccount, hisInfoUploadDTO.getDoctorUserDTO().getUserAccount())
                .eq(CUser::getDelFlag, BooleanEnum.FALSE.getNum())
                .orderByDesc(CUser::getId)
                .last("limit 1")
                .one();
        if (ObjectUtils.isEmpty(dbUser)){
            // 构建医生用户对象
            CUser user = this.buildCUser(hisInfoUploadDTO, hsp);
            // 新增医生用户信息
            userService.save(user);

            // 查询角色ids的数据
            CRole role = roleService.lambdaQuery()
                    .eq(CRole::getMedicalGroupCode, hisInfoUploadDTO.getMedicalGroupCode())
                    .eq(CRole::getRoleCode, hisInfoUploadRoleCode)
                    .eq(CRole::getDelFlag, BooleanEnum.FALSE.getNum())
                    .last("limit 1")
                    .one();
            if (!ObjectUtils.isEmpty(role)){
                // 保存医生用户关联角色数据
                cUserRoleService.save(new CUserRole(){{
                    setRoleId(role.getId());
                    setUserId(user.getId());
                }});
            } else {
                throw new ServiceException("请先初始化"+ hisInfoUploadRoleCode + "角色");
            }

            // 查询科室ids的数据
            if (StringUtils.isNotBlank(hisInfoUploadDTO.getDoctorUserDTO().getHisDoctorDeptName())){
                Dept dept = deptService.lambdaQuery()
                        .eq(Dept::getMedicalGroupCode, hisInfoUploadDTO.getMedicalGroupCode())
                        .eq(Dept::getHspCode, hisInfoUploadDTO.getHspCode())
                        .eq(Dept::getDeptName, hisInfoUploadDTO.getDoctorUserDTO().getHisDoctorDeptName())
                        .eq(Dept::getDelFlag, BooleanEnum.FALSE.getNum())
                        .last("limit 1")
                        .one();
                if (!ObjectUtils.isEmpty(dept)) {
                    // 保存医生用户关联科室数据
                    cUserDeptService.save(new CUserDept(){{
                        setDeptId(dept.getId());
                        setUserId(user.getId());
                    }});
                } else {
                    // 构建科室对象
                    Dept deptInsert = this.buildDept(hisInfoUploadDTO, hsp, user);
                    // 新增科室
                    deptService.save(deptInsert);
                }
            }
            dbUser = user;
        }
        return dbUser;
    }

    /**
     * 构建医生用户对象
     */
    private CUser buildCUser(HisInfoUploadDTO hisInfoUploadDTO, Hsp hsp) {
        Date now = new Date();
        return new CUser(){{
            setMedicalGroupId(hsp.getMedicalGroupId());
            setMedicalGroupCode(hisInfoUploadDTO.getMedicalGroupCode());
            setHspId(hsp.getId());
            setHspCode(hisInfoUploadDTO.getHspCode());
            setUserAccount(hisInfoUploadDTO.getDoctorUserDTO().getUserAccount());
            if (StringUtils.isNotBlank(hisInfoUploadDTO.getDoctorUserDTO().getRealName())){
                setRealName(hisInfoUploadDTO.getDoctorUserDTO().getRealName());
            } else {
                setRealName(hisInfoUploadDTO.getDoctorUserDTO().getUserAccount());
            }
            if (StringUtils.isNotBlank(hisInfoUploadDTO.getDoctorUserDTO().getStaffCode())){
                setStaffCode(hisInfoUploadDTO.getDoctorUserDTO().getStaffCode());
            } else {
                setStaffCode(hisInfoUploadDTO.getDoctorUserDTO().getUserAccount());
            }
            if (StringUtils.isNotBlank(hisInfoUploadDTO.getDoctorUserDTO().getHisDoctorCode())){
                setHisDoctorCode(hisInfoUploadDTO.getDoctorUserDTO().getHisDoctorCode());
            } else {
                setHisDoctorCode(hisInfoUploadDTO.getDoctorUserDTO().getUserAccount());
            }
            setPassword(new BCryptPasswordEncoder().encode("666666"));
            setHisDoctorDeptName(hisInfoUploadDTO.getDoctorUserDTO().getHisDoctorDeptName());
            setHisDoctorDeptCode(hisInfoUploadDTO.getDoctorUserDTO().getHisDoctorDeptCode());
            setCreateBy("0");
            setUpdateBy("0");
            setCreateTime(now);
            setUpdateTime(now);
        }};
    }

    /**
     * 构建科室对象
     */
    private Dept buildDept(HisInfoUploadDTO hisInfoUploadDTO, Hsp hsp, CUser user){
        Date now = new Date();
        return new Dept(){{
            setMedicalGroupId(hsp.getMedicalGroupId());
            setMedicalGroupCode(hisInfoUploadDTO.getMedicalGroupCode());
            setHspId(hsp.getId());
            setHspCode(hisInfoUploadDTO.getHspCode());
            setDeptName(hisInfoUploadDTO.getDoctorUserDTO().getHisDoctorDeptName());
            setDeptCode(hisInfoUploadDTO.getDoctorUserDTO().getHisDoctorDeptCode());
            setCreateBy(user.getId().toString());
            setUpdateBy(user.getId().toString());
            setCreateTime(now);
            setUpdateTime(now);
        }};
    }

    /**
     * 处理患者信息的数据
     */
    private PatientVisit handlePatientInfo(HisInfoUploadDTO hisInfoUploadDTO, Hsp hsp, CUser user) {
        // 根据就诊类型和患者的流水号，查询患者流水数据
        PatientVisit dbPatientVisit = patientVisitService.lambdaQuery()
                .eq(PatientVisit::getDelFlag, BooleanEnum.FALSE.getNum())
                .eq(PatientVisit::getMedicalGroupCode, hisInfoUploadDTO.getMedicalGroupCode())
                .eq(PatientVisit::getHspCode, hisInfoUploadDTO.getHspCode())
                .eq(PatientVisit::getTreatmentType, hisInfoUploadDTO.getPatientInfoDTO().getTreatmentType())
                .eq(PatientVisit::getInpatientCode, hisInfoUploadDTO.getPatientInfoDTO().getInpatientCode())
                .orderByDesc(PatientVisit::getId)
                .last("limit 1")
                .one();
        if (ObjectUtils.isEmpty(dbPatientVisit)){
            // 先查询患者档案信息是否存在
            PatientBase dbPatientBase = this.getPatientBaseByRxPatient(hisInfoUploadDTO);
            if (Objects.isNull(dbPatientBase)){
                // 构建患者档案信息对象
                PatientBase patientBase = this.buildPatientBase(hisInfoUploadDTO, hsp, user);
                // 新增患者档案信息
                if (patientBaseService.save(patientBase)){
                    dbPatientBase = patientBase;
                }
            }

            // 构建患者就诊流水对象
            PatientVisit patientVisit =  this.buildPatientVisit(hisInfoUploadDTO, hsp, user, true);
            // 设置患者档案信息id（rx_c_patient_base）
            if (Objects.nonNull(dbPatientBase)){
                patientVisit.setPatientBaseId(dbPatientBase.getId());
            }
            // 新增患者流水数据
            patientVisitService.save(patientVisit);
            dbPatientVisit = patientVisit;
        } else {
            // 构建患者就诊流水对象
            PatientVisit patientVisit = this.buildPatientVisit(hisInfoUploadDTO, hsp, user, false);
            patientVisit.setId(dbPatientVisit.getId());
            // 更新患者流水中的主治医生id数据
            JSONArray attendingDoctorIds = dbPatientVisit.getAttendingDoctorId();
            if (!attendingDoctorIds.contains(user.getId().intValue())){
                attendingDoctorIds.add(user.getId());
                patientVisit.setAttendingDoctorId(attendingDoctorIds);
            }
            // 更新患者信息
            patientVisitService.updateById(patientVisit);
            dbPatientVisit = patientVisit;
        }
        return dbPatientVisit;
    }

    /**
     * 根据上传的患者信息，查询患者档案信息
     */
    private PatientBase getPatientBaseByRxPatient(HisInfoUploadDTO hisInfoUploadDTO){
        // 根据患者id查询
        if (StringUtils.isNotBlank(hisInfoUploadDTO.getPatientInfoDTO().getPatientId())){
            PatientBase patientBase = patientBaseService.lambdaQuery()
                    .eq(PatientBase::getDelFlag, BooleanEnum.FALSE.getNum())
                    .eq(PatientBase::getMedicalGroupCode, hisInfoUploadDTO.getMedicalGroupCode())
                    .eq(PatientBase::getHspCode, hisInfoUploadDTO.getHspCode())
                    .eq(PatientBase::getPatientId, hisInfoUploadDTO.getPatientInfoDTO().getPatientId())
                    .orderByDesc(PatientBase::getId)
                    .last("limit 1")
                    .one();
            if (!ObjectUtils.isEmpty(patientBase)){
                return patientBase;
            }
        }
        // 根据患者身份证号码查询
        if (StringUtils.isNotBlank(hisInfoUploadDTO.getPatientInfoDTO().getIdNumber())){
            PatientBase patientBase = patientBaseService.lambdaQuery()
                    .eq(PatientBase::getDelFlag, BooleanEnum.FALSE.getNum())
                    .eq(PatientBase::getMedicalGroupCode, hisInfoUploadDTO.getMedicalGroupCode())
                    .eq(PatientBase::getHspCode, hisInfoUploadDTO.getHspCode())
                    .eq(PatientBase::getIdNumber, hisInfoUploadDTO.getPatientInfoDTO().getIdNumber())
                    .orderByDesc(PatientBase::getId)
                    .last("limit 1")
                    .one();
            if (!ObjectUtils.isEmpty(patientBase)){
                return patientBase;
            }
        }
        // 根据患者就诊卡号查询
        if (StringUtils.isNotBlank(hisInfoUploadDTO.getPatientInfoDTO().getClinicCode())){
            PatientBase patientBase = patientBaseService.lambdaQuery()
                    .eq(PatientBase::getDelFlag, BooleanEnum.FALSE.getNum())
                    .eq(PatientBase::getMedicalGroupCode, hisInfoUploadDTO.getMedicalGroupCode())
                    .eq(PatientBase::getHspCode, hisInfoUploadDTO.getHspCode())
                    .eq(PatientBase::getClinicCode, hisInfoUploadDTO.getPatientInfoDTO().getClinicCode())
                    .orderByDesc(PatientBase::getId)
                    .last("limit 1")
                    .one();
            if (!ObjectUtils.isEmpty(patientBase)){
                return patientBase;
            }
        }
        // 根据患者住院号查询
        if (StringUtils.isNotBlank(hisInfoUploadDTO.getPatientInfoDTO().getInpatientNumber())){
            PatientBase patientBase = patientBaseService.lambdaQuery()
                    .eq(PatientBase::getDelFlag, BooleanEnum.FALSE.getNum())
                    .eq(PatientBase::getMedicalGroupCode, hisInfoUploadDTO.getMedicalGroupCode())
                    .eq(PatientBase::getHspCode, hisInfoUploadDTO.getHspCode())
                    .eq(PatientBase::getInpatientNumber, hisInfoUploadDTO.getPatientInfoDTO().getInpatientNumber())
                    .orderByDesc(PatientBase::getId)
                    .last("limit 1")
                    .one();
            if (!ObjectUtils.isEmpty(patientBase)){
                return patientBase;
            }
        }
        // 根据患者手机号查询
        if (StringUtils.isNotBlank(hisInfoUploadDTO.getPatientInfoDTO().getPhone())){
            PatientBase patientBase = patientBaseService.lambdaQuery()
                    .eq(PatientBase::getDelFlag, BooleanEnum.FALSE.getNum())
                    .eq(PatientBase::getMedicalGroupCode, hisInfoUploadDTO.getMedicalGroupCode())
                    .eq(PatientBase::getHspCode, hisInfoUploadDTO.getHspCode())
                    .eq(PatientBase::getPhone, hisInfoUploadDTO.getPatientInfoDTO().getPhone())
                    .orderByDesc(PatientBase::getId)
                    .last("limit 1")
                    .one();
            if (!ObjectUtils.isEmpty(patientBase)){
                return patientBase;
            }
        }
        return null;
    }

    /**
     * 构建患者档案信息对象
     */
    private PatientBase buildPatientBase(HisInfoUploadDTO hisInfoUploadDTO, Hsp hsp, CUser user){
        Date now = new Date();
        return new PatientBase(){{
            setMedicalGroupId(hsp.getMedicalGroupId());
            setMedicalGroupCode(hisInfoUploadDTO.getMedicalGroupCode());
            setHspId(hsp.getId());
            setHspCode(hsp.getHspCode());
            setClinicCode(hisInfoUploadDTO.getPatientInfoDTO().getClinicCode());
            setInpatientNumber(hisInfoUploadDTO.getPatientInfoDTO().getInpatientNumber());
            setPatientId(hisInfoUploadDTO.getPatientInfoDTO().getPatientId());
            setPatientName(hisInfoUploadDTO.getPatientInfoDTO().getPatientName());
            setSex(hisInfoUploadDTO.getPatientInfoDTO().getSex());
            setAge(hisInfoUploadDTO.getPatientInfoDTO().getAge());
            setBirthDate(hisInfoUploadDTO.getPatientInfoDTO().getBirthDate());
            setIdNumber(hisInfoUploadDTO.getPatientInfoDTO().getIdNumber());
            setPhone(hisInfoUploadDTO.getPatientInfoDTO().getPhone());
            setRegion(hisInfoUploadDTO.getPatientInfoDTO().getRegion());
            setRegionIds(hisInfoUploadDTO.getPatientInfoDTO().getRegionIds());
            setAddress(hisInfoUploadDTO.getPatientInfoDTO().getAddress());
            setCreateBy(user.getId().toString());
            setUpdateBy(user.getId().toString());
            setCreateTime(now);
            setUpdateTime(now);
        }};
    }

    /**
     * 构建患者就诊流水对象
     */
    private PatientVisit buildPatientVisit(HisInfoUploadDTO hisInfoUploadDTO, Hsp hsp, CUser user, boolean isInsert){
        Date now = new Date();
        return new PatientVisit(){{
            setMedicalRecordNo(hisInfoUploadDTO.getPatientInfoDTO().getMedicalRecordNo());
            setTreatmentType(hisInfoUploadDTO.getPatientInfoDTO().getTreatmentType());
            setInpatientNumber(hisInfoUploadDTO.getPatientInfoDTO().getInpatientNumber());
            setInpatientCode(hisInfoUploadDTO.getPatientInfoDTO().getInpatientCode());
            setClinicCode(hisInfoUploadDTO.getPatientInfoDTO().getClinicCode());
            setPatientId(hisInfoUploadDTO.getPatientInfoDTO().getPatientId());
            setPatientName(hisInfoUploadDTO.getPatientInfoDTO().getPatientName());
            setSex(hisInfoUploadDTO.getPatientInfoDTO().getSex());
            setAge(hisInfoUploadDTO.getPatientInfoDTO().getAge());
            setBirthDate(hisInfoUploadDTO.getPatientInfoDTO().getBirthDate());
            setIdNumber(hisInfoUploadDTO.getPatientInfoDTO().getIdNumber());
            setPhone(hisInfoUploadDTO.getPatientInfoDTO().getPhone());
            setRegion(hisInfoUploadDTO.getPatientInfoDTO().getRegion());
            setRegionIds(hisInfoUploadDTO.getPatientInfoDTO().getRegionIds());
            setAddress(hisInfoUploadDTO.getPatientInfoDTO().getAddress());
            setWardName(hisInfoUploadDTO.getPatientInfoDTO().getWardName());
            setWardCode(hisInfoUploadDTO.getPatientInfoDTO().getWardCode());
            setDeptName(hisInfoUploadDTO.getPatientInfoDTO().getDeptName());
            setDeptCode(hisInfoUploadDTO.getPatientInfoDTO().getDeptCode());
            setBedNo(hisInfoUploadDTO.getPatientInfoDTO().getBedNo());
            setChiefComplaint(hisInfoUploadDTO.getPatientInfoDTO().getChiefComplaint());
            setPresentIllnessHistory(hisInfoUploadDTO.getPatientInfoDTO().getPresentIllnessHistory());
            setPreviousHistory(hisInfoUploadDTO.getPatientInfoDTO().getPreviousHistory());
            setPersonalHistory(hisInfoUploadDTO.getPatientInfoDTO().getPersonalHistory());
            setMaritalHistory(hisInfoUploadDTO.getPatientInfoDTO().getMaritalHistory());
            setFamilyHistory(hisInfoUploadDTO.getPatientInfoDTO().getFamilyHistory());
            setDisName(hisInfoUploadDTO.getPatientInfoDTO().getDisName());
            setDisCode(hisInfoUploadDTO.getPatientInfoDTO().getDisCode());
            setDisDesc(hisInfoUploadDTO.getPatientInfoDTO().getDisDesc());
            setUpdateBy(user.getId().toString());
            setUpdateTime(now);
            if (isInsert){
                setMedicalGroupId(hsp.getMedicalGroupId());
                setMedicalGroupCode(hisInfoUploadDTO.getMedicalGroupCode());
                setHspId(hsp.getId());
                setHspCode(hsp.getHspCode());
                setCreateBy(user.getId().toString());
                setCreateTime(now);
                // 新增患者流水中的主治医生id数据
                JSONArray attendingDoctorIds = new JSONArray();
                attendingDoctorIds.add(user.getId());
                setAttendingDoctorId(attendingDoctorIds);
            }
        }};
    }

}
