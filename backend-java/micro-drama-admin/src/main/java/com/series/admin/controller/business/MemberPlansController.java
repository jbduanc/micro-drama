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

@RestController
@RequestMapping("/memberPlans")
public class MemberPlansController {

    @Resource
    private IMemberPlansService memberPlansService;

    @PostMapping("/pageList")
    public TablePageInfo<MemberPlans> pageList(@RequestBody MemberPlanDTO queryVO) {
        Page page = startPage(queryVO.getPage(), queryVO.getSize());
        List<MemberPlans> resultList = memberPlansService.list(queryVO);
        return new TablePageInfo(resultList, Math.toIntExact(page.getTotal()));
    }

    @PostMapping("/saveOrUpdate")
    public Result<Boolean> saveOrUpdate(@RequestBody MemberPlanDTO dto) {
        return Result.ok(memberPlansService.saveOrUpdateMemberPlan(dto));
    }

    @GetMapping("/getById/{id}")
    public Result<MemberPlans> getById(@PathVariable String id) {
        return Result.ok(memberPlansService.getMemberPlanById(id));
    }

    @PostMapping("/delete/{id}")
    public Result<Boolean> delete(@PathVariable String id) {
        return Result.ok(memberPlansService.removeMemberPlan(id));
    }
}
