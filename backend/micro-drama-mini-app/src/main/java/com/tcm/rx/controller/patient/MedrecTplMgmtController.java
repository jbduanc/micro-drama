package com.tcm.rx.controller.patient;


import com.github.pagehelper.Page;
import com.tcm.common.entity.Result;
import com.tcm.common.entity.TablePageInfo;
import com.tcm.rx.entity.patient.MedrecTplMgmt;
import com.tcm.rx.service.patient.IMedrecTplMgmtService;
import com.tcm.rx.vo.patient.request.MedrecTplMgmtQueryVO;
import com.tcm.rx.vo.patient.response.MedrecTplMgmtVO;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

import static com.github.pagehelper.page.PageMethod.startPage;

/**
 * <p>
 * 诊疗开方系统--病历模版管理表 前端控制器
 * </p>
 *
 * @author djbo
 * @since 2025-09-11
 */
@RestController
@RequestMapping("/medrecTplMgmt")
public class MedrecTplMgmtController {

    @Resource
    private IMedrecTplMgmtService medrecTplMgmtService;

    /**
     * 分页查询
     */
    @PostMapping("/pageList")
    public TablePageInfo<MedrecTplMgmtVO> pageList(@RequestBody MedrecTplMgmtQueryVO queryVO) {
        Page<MedrecTplMgmtVO> page = startPage(queryVO.getPage(), queryVO.getSize());
        List<MedrecTplMgmtVO> rxMgmtVOS = medrecTplMgmtService.pageList(queryVO);
        return new TablePageInfo<>(rxMgmtVOS, Math.toIntExact(page.getTotal()));
    }

    /**
     * 查看详情
     */
    @GetMapping("/getInfo/{id}")
    public Result<MedrecTplMgmtVO> getInfo(@PathVariable Long id) {
        return Result.ok(medrecTplMgmtService.getInfo(id));
    }

    /**
     * 新增病历模版
     */
    @PostMapping("/save")
    public Result<Long> saveMedrecTpl(@RequestBody MedrecTplMgmt medrecTplMgmt) {
        return Result.ok(medrecTplMgmtService.saveMedrecTpl(medrecTplMgmt));
    }

    /**
     * 删除病历模版
     */
    @PostMapping("/delete/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.ok(medrecTplMgmtService.delete(id));
    }

}

