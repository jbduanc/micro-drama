package com.tcm.rx.controller.cons;

import com.github.pagehelper.Page;
import com.tcm.common.entity.Result;
import com.tcm.common.entity.TablePageInfo;
import com.tcm.rx.entity.basicData.Herb;
import com.tcm.rx.vo.cons.request.ConsQueryVO;
import com.tcm.rx.vo.cons.request.ConsUpdateVO;
import com.tcm.rx.vo.cons.request.EndConsVO;
import com.tcm.rx.vo.cons.response.ConsVO;
import org.springframework.web.bind.annotation.*;
import com.tcm.rx.vo.cons.request.ConsAddVO;

import javax.annotation.Resource;
import com.tcm.rx.service.cons.IConsultationService;

import java.util.List;

import static com.github.pagehelper.page.PageMethod.startPage;

/**
 * <p>
 * 诊疗开方系统--会诊表 前端控制器
 * </p>
 *
 * @author djbo
 * @since 2025-09-04
 */
@RestController
@RequestMapping("/cons")
public class ConsultationController {
    @Resource
    private IConsultationService consultationService;

    /**
     * 分页查询会诊列表（支持发起的/接收的会诊筛选）
     */
    @PostMapping("/pageList")
    public TablePageInfo<ConsVO> pageList(@RequestBody ConsQueryVO queryVO) {
        Page<Herb> page = startPage(queryVO.getPage(), queryVO.getSize());
        List<ConsVO> consVOS = consultationService.queryPage(queryVO);
        return new TablePageInfo<>(consVOS, Math.toIntExact(page.getTotal()));
    }

    /**
     * 查看会诊详情
     */
    @GetMapping("/getConsInfo/{id}")
    public Result<ConsVO> getConsInfo(@PathVariable Long id) {
        return Result.ok(consultationService.getConsInfo(id));
    }

    /**
     * 新增会诊（发起会诊，初始状态为草稿）
     */
    @PostMapping("/save")
    public Result<Long> saveCons(@RequestBody ConsAddVO addVO) {
        Long id = consultationService.saveCons(addVO);
        return Result.ok(id);
    }

    /**
     * 删除会诊
     */
    @PostMapping("/delete/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.ok(consultationService.delete(id));
    }
}

