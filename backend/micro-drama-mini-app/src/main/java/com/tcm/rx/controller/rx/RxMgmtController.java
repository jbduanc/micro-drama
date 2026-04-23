package com.tcm.rx.controller.rx;


import com.github.pagehelper.Page;
import com.tcm.common.entity.Result;
import com.tcm.common.entity.TablePageInfo;
import com.tcm.common.utils.EasyExcelUtil;
import com.tcm.rx.entity.rx.RxMgmtDetail;
import com.tcm.rx.service.rx.IRxMgmtService;
import com.tcm.rx.vo.rx.request.*;
import com.tcm.rx.vo.rx.response.RxMgmtDetailVO;
import com.tcm.rx.vo.rx.response.RxMgmtVO;
import org.apache.ibatis.annotations.Param;
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
 * 诊疗开方系统--方剂管理 前端控制器
 * </p>
 *
 * @author djbo
 * @since 2025-09-08
 */
@RestController
@RequestMapping("/rxMgmt")
public class RxMgmtController {

    @Resource
    private IRxMgmtService rxMgmtService;

    /**
     * 分页查询
     */
    @PostMapping("/pageList")
    public TablePageInfo<RxMgmtVO> pageList(@RequestBody RxMgmtQueryVO queryVO) {
        Page<RxMgmtVO> page = startPage(queryVO.getPage(), queryVO.getSize());
        List<RxMgmtVO> rxMgmtVOS = rxMgmtService.queryRxMgmtPage(queryVO);
        return new TablePageInfo<>(rxMgmtVOS, Math.toIntExact(page.getTotal()));
    }

    /**
     * 查看详情
     */
    @GetMapping("/getInfo/{id}")
    public Result<RxMgmtVO> getInfo(@PathVariable Long id) {
        return Result.ok(rxMgmtService.getInfo(id));
    }

    /**
     * 专病专方疾病列表
     */
    @GetMapping("/getTreatDis")
    public List<String> getTreatDis(@Param("treatDis") String treatDis) {
        return rxMgmtService.getTreatDis(treatDis);
    }

    /**
     * 新增管理处方
     */
    @PostMapping("/save")
    public Result<Long> saveRxMgmt(@RequestBody RxMgmtSaveVO rxMgmtSaveVO) {
        return Result.ok(rxMgmtService.saveRxMgmt(rxMgmtSaveVO));
    }

    /**
     * 删除管理处方
     */
    @PostMapping("/delete/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.ok(rxMgmtService.delete(id));
    }

    /**
     * 批量新增处方管理
     */
    @PostMapping(value = "/batchInsert")
    public Boolean batchInsertRxMgmt(@RequestBody RxMgmtImportVO rxMgmts) {
        return rxMgmtService.batchInsertRxMgmt(rxMgmts);
    }

    /**
     * 处方管理下载模板
     */
    @GetMapping("/rxMgmtTemplate/{type}")
    public void rxMgmtTemplate(@PathVariable Integer type, HttpServletResponse response) throws IOException {
        List<AncProvRxImportVO> templateList = new ArrayList<>();
        if (type == 1) {
            EasyExcelUtil.exportService(response, "经古名方导入模板", "经古名方导入模板", AncProvRxImportVO.class, templateList);
        } else if (type == 2) {
            EasyExcelUtil.exportService(response, "专病专方导入模板", "专病专方导入模板", SpecDisRxImportVO.class, templateList);
        } else if (type == 3) {
            EasyExcelUtil.exportService(response, "辩证施治导入模板", "辩证施治导入模板", SynDiffImportVO.class, templateList);
        }
    }

    /**
     * 处方管理导入
     */
    @PostMapping("/rxMgmtImport")
    public List ancProvRxImport(@RequestParam("type") Integer type, @RequestParam("file") MultipartFile file) throws Exception {
        return rxMgmtService.rxMgmtImport(type, file);
    }

    /**
     * 获取转化后的饮片详情
     */
    @PostMapping("/getTransDetails/{id}")
    public List<RxMgmtDetail> getTransDetails(@PathVariable Long id) {
        return rxMgmtService.getTransDetails(id);
    }
}

