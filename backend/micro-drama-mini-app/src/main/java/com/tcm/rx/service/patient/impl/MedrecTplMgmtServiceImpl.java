package com.tcm.rx.service.patient.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tcm.common.core.UserContextHolder;
import com.tcm.common.entity.BaseUser;
import com.tcm.common.exception.ServiceException;
import com.tcm.common.utils.AssertUtil;
import com.tcm.common.utils.CommonUtil;
import com.tcm.rx.entity.auth.CUser;
import com.tcm.rx.entity.patient.MedrecTplMgmt;
import com.tcm.rx.entity.rx.RxMgmt;
import com.tcm.rx.entity.rx.RxMgmtDetail;
import com.tcm.rx.mapper.patient.MedrecTplMgmtMapper;
import com.tcm.rx.service.auth.ICUserService;
import com.tcm.rx.service.patient.IMedrecTplMgmtService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tcm.rx.vo.patient.request.MedrecTplMgmtQueryVO;
import com.tcm.rx.vo.patient.response.MedrecTplMgmtVO;
import com.tcm.rx.vo.rx.response.RxMgmtVO;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>
 * 诊疗开方系统--病历模版管理表 服务实现类
 * </p>
 *
 * @author djbo
 * @since 2025-09-11
 */
@Service
public class MedrecTplMgmtServiceImpl extends ServiceImpl<MedrecTplMgmtMapper, MedrecTplMgmt> implements IMedrecTplMgmtService {

    @Resource
    private ICUserService userService;

    /**
     * 分页查询
     */
    public List<MedrecTplMgmtVO> pageList(MedrecTplMgmtQueryVO queryVO) {
        BaseUser currentUser = UserContextHolder.getUserInfoContext();
        queryVO.setMedicalGroupId(currentUser.getMedicalGroupId());
        return setMedrecTplInfo(this.getBaseMapper().selectByQuery(queryVO));
    }

    /**
     * 查看详情
     */
    public MedrecTplMgmtVO getInfo(Long id) {
        BaseUser currentUser = UserContextHolder.getUserInfoContext();
        MedrecTplMgmtQueryVO queryVO = new MedrecTplMgmtQueryVO();
        queryVO.setMedicalGroupId(currentUser.getMedicalGroupId());
        queryVO.setId(id);
        List<MedrecTplMgmtVO> medrecTplMgmtVOS = setMedrecTplInfo(this.getBaseMapper().selectByQuery(queryVO));
        if(ObjectUtil.isEmpty(medrecTplMgmtVOS)) {
            return new MedrecTplMgmtVO();
        }
        return medrecTplMgmtVOS.get(0);
    }

    /**
     * setMedrecTplInfo
     */
    private List<MedrecTplMgmtVO> setMedrecTplInfo(List<MedrecTplMgmtVO> mgmtVOS) {
        if (CollectionUtils.isEmpty(mgmtVOS)) {
            return new ArrayList<>();
        }
        List<Long> userIdList = mgmtVOS.stream()
                .flatMap(record -> Stream.of(Optional.ofNullable(record.getCreateBy()).orElse("-1"),
                                record.getUpdateBy(), Optional.ofNullable(record.getOwnerId()).orElse(-1L).toString())
                        .filter(Objects::nonNull))
                .map(record -> Long.parseLong(record)).collect(Collectors.toList());
        Map<Long, String> userMap = userService.listByIds(userIdList)
                .stream().collect(Collectors.toMap(CUser::getId, CUser::getRealName));
        for (MedrecTplMgmtVO medrecTplMgmtVO : mgmtVOS) {
            if (ObjectUtil.isNotEmpty(medrecTplMgmtVO.getCreateBy())) {
                medrecTplMgmtVO.setCreateByName(userMap.get(Long.parseLong(medrecTplMgmtVO.getCreateBy())));
            }
            if (ObjectUtil.isNotEmpty(medrecTplMgmtVO.getUpdateBy())) {
                medrecTplMgmtVO.setUpdateByName(userMap.get(Long.parseLong(medrecTplMgmtVO.getUpdateBy())));
            }
            if (ObjectUtil.isNotEmpty(medrecTplMgmtVO.getOwnerId())) {
                medrecTplMgmtVO.setOwnerName(userMap.get(medrecTplMgmtVO.getOwnerId()));
            }
        }
        return mgmtVOS;
    }

    /**
     * 新增病历模版
     */
    public Long saveMedrecTpl(MedrecTplMgmt medrecTplMgmt) {
        AssertUtil.assertArgs()
                .notEmpty(medrecTplMgmt.getTplName(), "模版名称")
                .check();
        BaseUser currentUser = UserContextHolder.getUserInfoContext();
        if (ObjectUtil.isEmpty(medrecTplMgmt.getId())) {
            CommonUtil.setRxBaseEntity(medrecTplMgmt, currentUser);
            Optional<MedrecTplMgmt> optionalMedrecTpl = this.lambdaQuery().eq(MedrecTplMgmt::getMedicalGroupId, currentUser.getMedicalGroupId())
                    .eq(MedrecTplMgmt::getTplName, medrecTplMgmt.getTplName()).list().stream().findFirst();
            if (optionalMedrecTpl.isPresent()) {
                throw new ServiceException("模版名称已存在");
            }
            this.save(medrecTplMgmt);
        } else {
            Optional<MedrecTplMgmt> optionalMedrecTpl = this.lambdaQuery().eq(MedrecTplMgmt::getMedicalGroupId, currentUser.getMedicalGroupId())
                    .eq(MedrecTplMgmt::getTplName, medrecTplMgmt.getTplName()).list().stream().findFirst();
            if (optionalMedrecTpl.isPresent() && !(optionalMedrecTpl.get().getId().longValue() == medrecTplMgmt.getId())) {
                throw new ServiceException("模版名称已存在");
            }
            medrecTplMgmt.setUpdateBy(currentUser.getId().toString());
            medrecTplMgmt.setUpdateTime(new Date());
            this.updateById(medrecTplMgmt);
        }
        return medrecTplMgmt.getId();
    }

    /**
     * 删除管理处方
     */
    public Boolean delete(Long id) {
        if (ObjectUtil.isEmpty(this.getById(id))) {
            throw new ServiceException("模版不存在");
        }
        this.removeById(id);
        return true;
    }
}
