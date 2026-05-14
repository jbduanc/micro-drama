package com.series.admin.service.business;

import com.baomidou.mybatisplus.extension.service.IService;
import com.series.admin.dto.business.MemberPlanDTO;
import com.series.admin.entity.business.MemberPlans;

import java.util.List;

public interface IMemberPlansService extends IService<MemberPlans> {

    List<MemberPlans> list(MemberPlanDTO queryVO);

    boolean saveOrUpdateMemberPlan(MemberPlanDTO dto);

    boolean removeMemberPlan(String id);

    MemberPlans getMemberPlanById(String id);
}
