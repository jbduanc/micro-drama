package com.tcm.rx.controller.basicData;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.github.pagehelper.Page;
import com.tcm.common.core.UserContextHolder;
import com.tcm.common.entity.BaseUser;
import com.tcm.common.entity.Result;
import com.tcm.common.entity.TablePageInfo;
import com.tcm.common.utils.EasyExcelUtil;
import com.tcm.rx.entity.basicData.Herb;
import com.tcm.rx.feign.herb.HerbStockFeignClient;
import com.tcm.rx.feign.herb.vo.CustomerHerbVO;
import com.tcm.rx.service.basicData.IHerbService;
import com.tcm.rx.vo.basicData.request.HerbImportVO;
import com.tcm.rx.vo.basicData.request.HerbQueryVO;
import com.tcm.rx.vo.basicData.response.HerbVO;
import com.tcm.rx.vo.herb.request.HerbSyncVO;
import com.tcm.rx.vo.herb.response.HerbSyncDataVO;
import com.tcm.rx.vo.herb.response.HerbSyncDetailVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.github.pagehelper.page.PageMethod.startPage;

/**
 * 诊疗系统-饮片管理
 *
 * @author duanqiyuan
 * @since 2025-07-17 14:58:47
 */
@Slf4j
@RestController
@RequestMapping("/herb")
public class HerbController {

    @Resource
    private IHerbService rxHerbService;

    @Resource
    private HerbStockFeignClient herbStockFeignClient;

    /**
     * 查询煎药饮片库存列表
     */
    @PostMapping("/queryCustomerHerbStock")
    public Result<List<CustomerHerbVO>> queryCustomerHerbStock(@RequestBody List<CustomerHerbVO> customerHerbVOS) {
        BaseUser user = UserContextHolder.getUserInfoContext();
        customerHerbVOS.forEach(record -> {
            record.setMedicalGroupCode(user.getMedicalGroupCode());
            if (ObjectUtil.isNotEmpty(record.getCustomerCode())
                    && record.getCustomerCode().split(",").length > 1
                    && ObjectUtil.isNotEmpty(user.getHspCode())
                    && !"0".equals(user.getHspCode())) {
                record.setCustomerCode(user.getHspCode());
            }
        });
        Result result = herbStockFeignClient.queryCustomerHerbStock(customerHerbVOS);
        log.info("远程调用库存结果为" + JSONObject.toJSONString(result));
        return result;
    }

    /**
     * 分页查询
     */
    @PostMapping("/pageList")
    public TablePageInfo<HerbVO> pageList(@RequestBody HerbQueryVO queryVO) {
        Page<Herb> page = startPage(queryVO.getPage(), queryVO.getSize());
        List<HerbVO> list = rxHerbService.queryPage(queryVO);
        return new TablePageInfo<>(list, Math.toIntExact(page.getTotal()));
    }

    /**
     * 导出
     */
    @PostMapping("/export")
    public void export(@RequestBody HerbQueryVO queryVO, HttpServletResponse response) throws IOException {
        rxHerbService.export(queryVO, response);
    }

    /**
     * 批量新增饮片
     */
    @PostMapping(value = "/batchInsertHerb")
    public Boolean batchInsertHerb(@RequestBody List<HerbImportVO> herbVOList) {
        return rxHerbService.batchInsertHerb(herbVOList);
    }

    /**
     * 下载导入模板
     */
    @PostMapping("/template")
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        List<HerbImportVO> templateList = new ArrayList<>();
        // 创建一个示例数据行
        // 导出模板
        EasyExcelUtil.exportService(response, "饮片导入模板", "饮片信息", HerbImportVO.class, templateList);
    }

    /**
     * 导入
     */
    @PostMapping("/import")
    public List<HerbImportVO> importData(@RequestParam("file") MultipartFile file) throws Exception {
        return rxHerbService.importData(file);
    }

    /**
     * 新增
     */
    @PostMapping("/add")
    public Long add(@RequestBody HerbVO herbVO) {
        return rxHerbService.add(herbVO);
    }

    /**
     * 编辑
     */
    @PostMapping("/update")
    public Long update(@RequestBody Herb herb) {
        return rxHerbService.update(herb);
    }

    /**
     * 删除
     */
    @PostMapping("/delete/{id}")
    public void delete(@PathVariable Long id) {
        rxHerbService.delete(id);
    }

    /**
     * 同步饮片数据列表
     */
    @PostMapping("/syncHerbList")
    public TablePageInfo<HerbSyncDataVO> syncHerb(@RequestBody HerbSyncVO herbSyncVO)  {
        return rxHerbService.syncHerbList(herbSyncVO);
    }

    /**
     * 同步饮片入库
     */
    @PostMapping("/syncHerb")
    public Boolean syncHerb(@RequestBody HerbSyncDataVO syncDataVO) {
        return rxHerbService.syncHerb(syncDataVO);
    }

    /**
     * 采购单下载
     */
    @PostMapping("/purchaseDownload")
    public void purchaseDownload(@RequestBody HerbSyncDataVO syncDataVO, HttpServletResponse response) throws IOException {
        rxHerbService.purchaseDownload(syncDataVO, response);
    }

    /**
     * 饮片名称匹配
     *
     * @param queryVO
     * @return
     */
    @PostMapping("/herbNameMatch")
    public List<Herb> herbNameMatch(HerbQueryVO queryVO) {
        return rxHerbService.herbNameMatch(queryVO);
    };
}
