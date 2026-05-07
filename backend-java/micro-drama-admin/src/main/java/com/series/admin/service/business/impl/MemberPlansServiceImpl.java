package com.series.admin.service.business.impl;

import com.series.admin.dto.business.MemberPlanDTO;
import com.series.admin.entity.business.MemberPlans;
import com.series.admin.mapper.business.MemberPlansMapper;
import com.series.admin.service.business.IMemberPlansService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * <p>
 * 会员套餐表 服务实现类
 * </p>
 *
 * @author djbo
 * @since 2026-04-13
 */
@Service
public class MemberPlansServiceImpl extends ServiceImpl<MemberPlansMapper, MemberPlans> implements IMemberPlansService {


    public List<MemberPlans> list(MemberPlanDTO queryVO) {
        return this.lambdaQuery().orderByDesc(MemberPlans::getCreateTime).list();
    }

    @Override
    public boolean saveOrUpdateMemberPlan(MemberPlanDTO dto) {
        MemberPlans memberPlans = new MemberPlans();
        BeanUtils.copyProperties(dto, memberPlans);

        if (dto.getPlanId() == null) {
            // 新增：设置创建时间
            memberPlans.setCreateTime(new Date());
        }
        // 编辑：MyBatis-Plus的saveOrUpdate会根据ID自动判断新增/更新
        return this.saveOrUpdate(memberPlans);
    }

    @Override
    public boolean removeMemberPlan(Long planId) {
        // 逻辑删除可替换为 update 状态，物理删除用removeById
        return this.removeById(planId);
    }

    @Override
    public MemberPlans getMemberPlanById(Long planId) {
        return this.getById(planId);
    }
}
