package com.tcm.rx.controller.basicData;

import com.github.pagehelper.Page;
import com.tcm.common.entity.TablePageInfo;
import com.tcm.common.utils.EasyExcelUtil;
import com.tcm.rx.entity.basicData.ChargeItem;
import com.tcm.rx.service.basicData.IChargeItemService;
import com.tcm.rx.vo.basicData.request.ChargeItemImportVO;
import com.tcm.rx.vo.basicData.request.ChargeItemQueryVO;
import com.tcm.rx.vo.basicData.request.HerbImportVO;
import com.tcm.rx.vo.basicData.response.ChargeItemVO;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.github.pagehelper.page.PageMethod.startPage;

/**
 * 诊疗系统-收费项目管理
 */
@RestController
@RequestMapping("/basicData/chargeItem")
public class ChargeItemController {

    @Resource
    private IChargeItemService chargeItemService;

    /**
     * 分页查询
     */
    @PostMapping("/pageList")
    public TablePageInfo<ChargeItemVO> pageList(@RequestBody ChargeItemQueryVO queryVO) {
        Page<ChargeItem> page = startPage(queryVO.getPage(), queryVO.getSize());
        List<ChargeItemVO> list = chargeItemService.queryPage(queryVO);
        return new TablePageInfo<>(list, Math.toIntExact(page.getTotal()));
    }

    /**
     * 新增
     */
    @PostMapping("/add")
    public Long add(@RequestBody ChargeItemVO chargeItemVO) {
        return chargeItemService.add(chargeItemVO);
    }

    /**
     * 编辑
     */
    @PostMapping("/update")
    public Long update(@RequestBody ChargeItemVO chargeItemVO) {
        return chargeItemService.update(chargeItemVO);
    }

    /**
     * 删除（逻辑删除）
     */
    @PostMapping("/delete/{id}")
    public void delete(@PathVariable Long id) {
        chargeItemService.delete(id);
    }

    /**
     * 导入
     */
    @PostMapping("/import")
    public List<ChargeItemImportVO> importData(@RequestParam("file") MultipartFile file) throws Exception {
        return chargeItemService.importData(file);
    }

    @PostMapping(value = "/batchInsert")
    public Boolean batchInsert(@RequestBody List<ChargeItemImportVO> importVOS){
        return chargeItemService.batchInsert(importVOS);
    }

    /**
     * 下载导入模板
     */
    @PostMapping("/downloadTemplate")
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        List<ChargeItemImportVO> templateList = new ArrayList<>();
        // 示例数据
        EasyExcelUtil.exportService(response, "收费项目导入模板", "项目信息",
                ChargeItemImportVO.class, templateList);
    }
}