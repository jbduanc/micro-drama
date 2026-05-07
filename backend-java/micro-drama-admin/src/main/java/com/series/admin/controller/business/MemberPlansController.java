package com.series.admin.controller.business;


import com.github.pagehelper.Page;
import com.series.admin.dto.business.MemberPlanDTO;
import com.series.admin.entity.business.MemberPlans;
import com.series.admin.service.business.IMemberPlansService;
import com.series.common.entity.Result;
import com.series.common.entity.TablePageInfo;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

import static com.github.pagehelper.page.PageMethod.startPage;

/**
 * <p>
 * 会员套餐表 前端控制器
 * </p>
 *
 * @author djbo
 * @since 2026-04-13
 */
@RestController
@RequestMapping("/memberPlans")
public class MemberPlansController {

    @Resource
    private IMemberPlansService memberPlansService;

    /**
     * 分页查询会员计划
     */
    @PostMapping("/pageList")
    public TablePageInfo<MemberPlans> pageList(@RequestBody MemberPlanDTO queryVO) {
        Page page = startPage(queryVO.getPage(),queryVO.getSize());
        List<MemberPlans> resultList = memberPlansService.list(queryVO);
        return new TablePageInfo(resultList, Math.toIntExact(page.getTotal()));
    }

    /**
     * 新增/编辑会员套餐（根据planId判断：null=新增，非null=编辑）
     */
    @PostMapping("/saveOrUpdate")
    public Result<Boolean> saveOrUpdate(@RequestBody MemberPlanDTO dto) {
       return Result.ok(memberPlansService.saveOrUpdateMemberPlan(dto));
    }

    /**
     * 根据ID查询会员套餐
     */
    @GetMapping("/getById/{planId}")
    public Result<MemberPlans> getById(@PathVariable Long planId) {
        return Result.ok(memberPlansService.getMemberPlanById(planId));
    }

    /**
     * 根据ID删除会员套餐
     */
    @PostMapping("/delete/{planId}")
    public Result<Boolean> delete(@PathVariable Long planId) {
        return Result.ok(memberPlansService.removeMemberPlan(planId));
    }
}

