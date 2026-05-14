package com.series.admin.service.business.impl;

import com.series.admin.dto.business.MemberPlanDTO;
import com.series.admin.entity.business.MemberPlans;
import com.series.admin.mapper.business.MemberPlansMapper;
import com.series.admin.service.business.IMemberPlansService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;

@Service
public class MemberPlansServiceImpl extends ServiceImpl<MemberPlansMapper, MemberPlans> implements IMemberPlansService {

    @Override
    public List<MemberPlans> list(MemberPlanDTO queryVO) {
        return this.lambdaQuery().orderByDesc(MemberPlans::getCreateTime).list();
    }

    @Override
    public boolean saveOrUpdateMemberPlan(MemberPlanDTO dto) {
        MemberPlans memberPlans = new MemberPlans();
        BeanUtils.copyProperties(dto, memberPlans);
        if (!StringUtils.hasText(dto.getId())) {
            memberPlans.setId(null);
            memberPlans.setCreateTime(new Date());
        }
        return this.saveOrUpdate(memberPlans);
    }

    @Override
    public boolean removeMemberPlan(String id) {
        return this.removeById(id);
    }

    @Override
    public MemberPlans getMemberPlanById(String id) {
        return this.getById(id);
    }
}
