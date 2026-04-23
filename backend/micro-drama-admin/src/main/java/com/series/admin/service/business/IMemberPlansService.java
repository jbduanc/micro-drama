package com.series.admin.service.business;

import com.baomidou.mybatisplus.extension.service.IService;
import com.series.admin.dto.business.MemberPlanDTO;
import com.series.admin.entity.business.MemberPlans;

import java.util.List;

/**
 * <p>
 * 会员套餐表 服务类
 * </p>
 *
 * @author djbo
 * @since 2026-04-13
 */
public interface IMemberPlansService extends IService<MemberPlans> {

    List<MemberPlans> list(MemberPlanDTO queryVO);

    /**
     * 新增/编辑会员套餐
     */
    boolean saveOrUpdateMemberPlan(MemberPlanDTO dto);

    /**
     * 根据ID删除会员套餐
     */
    boolean removeMemberPlan(Long planId);

    /**
     * 根据ID查询会员套餐
     */
    MemberPlans getMemberPlanById(Long planId);
}
