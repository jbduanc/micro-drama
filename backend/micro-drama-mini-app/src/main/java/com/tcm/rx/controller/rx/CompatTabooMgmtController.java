package com.tcm.rx.controller.rx;


import com.github.pagehelper.Page;
import com.tcm.common.entity.Result;
import com.tcm.common.entity.TablePageInfo;
import com.tcm.common.utils.EasyExcelUtil;
import com.tcm.rx.entity.basicData.Herb;
import com.tcm.rx.service.rx.ICompatTabooMgmtService;
import com.tcm.rx.vo.rx.request.CompatTabCheckReqVO;
import com.tcm.rx.vo.rx.request.CompatTabImportVO;
import com.tcm.rx.vo.rx.request.CompatTabooMgmtQueryVO;
import com.tcm.rx.vo.rx.request.CompatTabooMgmtSaveVO;
import com.tcm.rx.vo.rx.response.CompatTabCheckResVO;
import com.tcm.rx.vo.rx.response.CompatTabooMgmtVO;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.github.pagehelper.page.PageMethod.startPage;

/**
 * <p>
 * 诊疗开方系统--配伍禁忌管理表 前端控制器
 * </p>
 *
 * @author djbo
 * @since 2025-09-10
 */
@RestController
@RequestMapping("/compatTabooMgmt")
public class CompatTabooMgmtController {

    @Resource
    private ICompatTabooMgmtService compatTabooMgmtService;

    /**
     * 分页查询
     */
    @PostMapping("/pageList")
    public TablePageInfo<CompatTabooMgmtVO> pageList(@RequestBody CompatTabooMgmtQueryVO queryVO) {
        Page<Herb> page = startPage(queryVO.getPage(), queryVO.getSize());
        List<CompatTabooMgmtVO> compatTabooMgmtVOS = compatTabooMgmtService.pageList(queryVO);
        return new TablePageInfo<>(compatTabooMgmtVOS, Math.toIntExact(page.getTotal()));
    }

    /**
     * 查看详情
     */
    @GetMapping("/getInfo/{id}")
    public Result<CompatTabooMgmtVO> getInfo(@PathVariable Long id) {
        return Result.ok(compatTabooMgmtService.getInfo(id));
    }

    /**
     * 新增配伍禁忌
     */
    @PostMapping("/save")
    public Result<Long> saveRxMgmt(@RequestBody CompatTabooMgmtSaveVO compatTabooMgmtSaveVO) {
        return Result.ok(compatTabooMgmtService.saveCompatTab(compatTabooMgmtSaveVO));
    }

    /**
     * 删除配伍禁忌
     */
    @PostMapping("/delete/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.ok(compatTabooMgmtService.delete(id));
    }

    /**
     * 批量新增处方管理
     */
    @PostMapping(value = "/batchInsert")
    public Boolean batchInsertCompatTab(@RequestBody List<CompatTabImportVO> compatTabImportVOS) {
        return compatTabooMgmtService.batchInsertCompatTab(compatTabImportVOS);
    }

    /**
     * 配伍禁忌管理下载模板
     */
    @PostMapping("/template")
    public void template(HttpServletResponse response) throws IOException {
        List<CompatTabImportVO> templateList = new ArrayList<>();
        EasyExcelUtil.exportService(response, "配伍禁忌导入模板",
                "配伍禁忌导入模板", CompatTabImportVO.class, templateList);
    }

    /**
     * 配伍禁忌导入
     */
    @PostMapping("/compatTabImport")
    public List<CompatTabImportVO> compatTabImport(@RequestParam("file") MultipartFile file) throws Exception {
        return compatTabooMgmtService.compatTabImport(file);
    }

    /**
     * 配伍禁忌检查
     */
    @PostMapping("/compatTabCheck")
    public List<CompatTabCheckResVO> compatTabCheck(@RequestBody CompatTabCheckReqVO compatTabCheckVO) {
        return compatTabooMgmtService.compatTabCheck(compatTabCheckVO);
    }
}