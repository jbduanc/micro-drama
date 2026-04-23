package com.tcm.rx.service.cons.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tcm.common.core.UserContextHolder;
import com.tcm.common.entity.BaseUser;
import com.tcm.common.enums.BooleanEnum;
import com.tcm.common.exception.ServiceException;
import com.tcm.common.utils.CommonUtil;
import com.tcm.rx.entity.auth.CUser;
import com.tcm.rx.entity.cons.Consultation;
import com.tcm.rx.entity.dept.Dept;
import com.tcm.rx.entity.hsp.Hsp;
import com.tcm.rx.entity.msg.Msg;
import com.tcm.rx.entity.rx.Rx;
import com.tcm.rx.enums.*;
import com.tcm.rx.mapper.cons.ConsultationMapper;
import com.tcm.rx.service.auth.ICUserService;
import com.tcm.rx.service.cons.IConsultationService;
import com.tcm.rx.service.dept.IDeptService;
import com.tcm.rx.service.hsp.IHspService;
import com.tcm.rx.service.msg.IMsgService;
import com.tcm.rx.service.rx.IRxService;
import com.tcm.rx.vo.cons.request.ConsAddVO;
import com.tcm.rx.vo.cons.request.ConsQueryVO;
import com.tcm.rx.vo.cons.response.ConsVO;
import com.tcm.rx.vo.rx.request.RxSaveVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>
 * 诊疗开方系统--会诊表 服务实现类
 * </p>
 *
 * @author djbo
 * @since 2025-09-04
 */
@Service
public class ConsultationServiceImpl extends ServiceImpl<ConsultationMapper, Consultation> implements IConsultationService {
    @Resource
    private IHspService hspService;
    @Resource
    private ICUserService userService;
    @Resource
    private IDeptService deptService;
    @Resource
    private IMsgService msgService;
    @Resource
    private IRxService rxService;

    /**
     * 分页查询会诊列表（含数据隔离、关联信息转换）
     */
    @Override
    public List<ConsVO> queryPage(ConsQueryVO queryVO) {
        // 1. 填充当前用户的医联体信息（数据隔离）
        BaseUser currentUser = UserContextHolder.getUserInfoContext();
        queryVO.setMedicalGroupId(currentUser.getMedicalGroupId());
        queryVO.setCurrentUserId(currentUser.getId());
        if (ObjectUtil.isNotEmpty(queryVO.getConsInitStartTime())) {
            queryVO.setConsInitStartTime(CommonUtil.getDayStartTime(queryVO.getConsInitStartTime()));
        }
        if (ObjectUtil.isNotEmpty(queryVO.getConsInitEndTime())) {
            queryVO.setConsInitEndTime(CommonUtil.getDayEndTime(queryVO.getConsInitEndTime()));
        }
        List<ConsVO> consVOS = baseMapper.selectByQuery(queryVO);
        setConsVOInfo(consVOS);
        return consVOS;
    }

    /**
     * setConsVOInfo
     */
    private List<ConsVO> setConsVOInfo(List<ConsVO> consVOS) {
        if (CollectionUtils.isEmpty(consVOS)) {
            return new ArrayList<>();
        }
        List<Long> userIdList = consVOS.stream()
                .flatMap(record -> Stream.of(record.getConsInitUserId(),
                                record.getPropinvConsUserId(),
                                Long.parseLong(Optional.ofNullable(record.getCreateBy()).orElse("-1")),
                                Long.parseLong(Optional.ofNullable(record.getUpdateBy()).orElse("-1"))
                        )
                        .filter(Objects::nonNull)).collect(Collectors.toList());
        Map<Long, String> userMap = userService.listByIds(userIdList)
                .stream().collect(Collectors.toMap(CUser::getId, CUser::getRealName));
        List<Long> hspIdList = consVOS.stream()
                .flatMap(record -> Stream.of(Optional.ofNullable(record.getConsInitHspId()).orElse(-1L),
                                Optional.ofNullable(record.getPropinvConsHspId()).orElse(-1L))
                        .filter(Objects::nonNull)).collect(Collectors.toList());
        Map<Long, String> hspMap = hspService.listByIds(hspIdList)
                .stream().collect(Collectors.toMap(Hsp::getId, Hsp::getHspName));
        List<Long> deptIdList = consVOS.stream()
                .flatMap(record -> Stream.of(Optional.ofNullable(record.getPropinvConsDeptId()).orElse(-1L))
                        .filter(Objects::nonNull)).collect(Collectors.toList());
        Map<Long, String> deptMap = deptService.listByIds(deptIdList)
                .stream().collect(Collectors.toMap(Dept::getId, Dept::getDeptName));
        List<String> rxIdList = consVOS.stream().map(conVO ->
                Optional.ofNullable(conVO.getRxId()).orElse("-1")).collect(Collectors.toList());
        Map<String,Rx> rxMap = rxService.lambdaQuery().in(Rx::getId, rxIdList).list()
                .stream().collect(Collectors.toMap(Rx::getId, R -> R));
        for (ConsVO consVO : consVOS) {
            consVO.setStatusName(ConsStatusEnum.CODE_MAP.get(consVO.getStatus()));
            consVO.setSexName(SexEnum.CODE_MAP.get(consVO.getSex()));
            if (StringUtils.isNotBlank(consVO.getRxJson())) {
                consVO.setRxSaveVO(JSON.parseObject(consVO.getRxJson()));
            }
            consVO.setConsInitHspName(hspMap.get(consVO.getConsInitHspId()));
            consVO.setConsInitUserName(userMap.get(consVO.getConsInitUserId()));
            consVO.setPropinvConsHspName(hspMap.get(consVO.getPropinvConsHspId()));
            consVO.setPropinvConsDeptName(deptMap.get(consVO.getPropinvConsDeptId()));
            consVO.setPropinvConsUserName(userMap.get(consVO.getPropinvConsUserId()));
            consVO.setConsTypeName(ConsTypeEnum.CODE_MAP.get(consVO.getConsType()));
            if (ObjectUtil.isNotEmpty(consVO.getCreateBy())) {
                consVO.setCreateByName(userMap.get(Long.parseLong(consVO.getCreateBy())));
            }
            if (ObjectUtil.isNotEmpty(consVO.getUpdateBy())) {
                consVO.setUpdateByName(userMap.get(Long.parseLong(consVO.getUpdateBy())));
            }
            if (ObjectUtil.isNotEmpty(rxMap.get(consVO.getRxId()))
                    && Optional.ofNullable(rxMap.get(consVO.getRxId()).getStatus()).orElse(0) >= RxStatusEnum.PAID.getCode()) {
                consVO.setIsPaid(true);
            }
        }
        return consVOS;
    }

    public ConsVO getConsInfo(Long id) {
        BaseUser currentUser = UserContextHolder.getUserInfoContext();
        ConsQueryVO queryVO = new ConsQueryVO();
        queryVO.setConsId(id);
        queryVO.setMedicalGroupId(currentUser.getMedicalGroupId());
        List<ConsVO> consVOS = baseMapper.selectByQuery(queryVO);
        if (CollectionUtils.isEmpty(consVOS)) {
            return new ConsVO();
        }
        return setConsVOInfo(consVOS).get(0);
    }

    /**
     * 保存会诊
     *
     * @param addVO
     * @return
     */
    public Long saveCons(ConsAddVO addVO) {
        BaseUser currentUser = UserContextHolder.getUserInfoContext();
        Consultation consultation = new Consultation();
        if(ObjectUtil.isNotEmpty(addVO.getId())){
            consultation = this.getById(addVO.getId());
        }
        CopyOptions copyOptions = new CopyOptions();
        copyOptions.setIgnoreNullValue(true);
        BeanUtil.copyProperties(addVO, consultation, copyOptions);
        if (ObjectUtil.isEmpty(consultation.getStatus())) {
            consultation.setStatus(ConsStatusEnum.DRAFT.getCode());
        }
        if (ConsStatusEnum.PENDING.getCode().intValue() == consultation.getStatus()
                || ConsStatusEnum.DRAFT.getCode().intValue() == consultation.getStatus()) {
            consultation.setConsInitUserId(currentUser.getId());
            consultation.setConsInitHspId(currentUser.getHspId());
            consultation.setConsInitTime(new Date());
        } else if (ConsStatusEnum.COMPLETED.getCode().intValue() == consultation.getStatus()) {
            consultation.setConsEndTime(new Date());
        }
        if (ObjectUtil.isEmpty(consultation.getId())) {
            CommonUtil.setRxBaseEntity(consultation, currentUser);
        } else {
            consultation.setUpdateBy(currentUser.getId().toString());
            consultation.setUpdateTime(new Date());
        }
        this.saveOrUpdate(consultation);
        if (ConsStatusEnum.PENDING.getCode().intValue() == consultation.getStatus()) {
            sendMsg(MsgTypeEnum.RECEIVE_CONSULTATION.getCode(), consultation.getId(), addVO.getPropinvConsUserId());
        } else if (ConsStatusEnum.COMPLETED.getCode().intValue() == consultation.getStatus()) {
            sendMsg(MsgTypeEnum.CONSULTATION_RESULT.getCode(), consultation.getId(), consultation.getConsInitUserId());
        }
        return consultation.getId();
    }

    /**
     * 发送消息
     *
     * @param msgType
     * @param businessId
     * @param targetUserId
     */
    public void sendMsg(String msgType, Long businessId, Long targetUserId) {
        BaseUser currentUser = UserContextHolder.getUserInfoContext();
        Hsp hsp = Optional.ofNullable(hspService.getById(Optional.ofNullable(currentUser.getHspId()).orElse(0L))).orElse(new Hsp());
        Msg msg = new Msg();
        msg.setType(msgType);
        msg.setUserId(targetUserId);
        // 设置为未读状态
        msg.setStatus(BooleanEnum.FALSE.getNum());

        if (MsgTypeEnum.RECEIVE_CONSULTATION.getCode().equals(msgType)) {
            msg.setTitle(String.format("%s%s发起的会诊", Optional.ofNullable(hsp.getHspName()).orElse(""), currentUser.getRealName()));
        } else {
            msg.setTitle(String.format("%s%s受理的会诊", Optional.ofNullable(hsp.getHspName()).orElse(""), currentUser.getRealName()));
        }
        msg.setBusinessId(businessId);
        CommonUtil.setRxBaseEntity(msg, currentUser);
        // 调用修改后的save方法，会自动保存到Redis
        msgService.save(msg);
    }

    /**
     * 删除会诊
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(Long id) {
        BaseUser currentUser = UserContextHolder.getUserInfoContext();
        Consultation consultation = this.getById(id);
        if (ObjectUtil.isEmpty(consultation)) {
            throw new ServiceException("会诊不存在");
        }
        // 4. 逻辑删除
        consultation.setDelFlag(BooleanEnum.TRUE.getNum());
        consultation.setUpdateBy(currentUser.getId().toString());
        consultation.setUpdateTime(new Date());
        this.updateById(consultation);
        return true;
    }
}
