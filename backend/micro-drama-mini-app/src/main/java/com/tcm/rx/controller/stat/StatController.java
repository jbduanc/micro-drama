package com.tcm.rx.controller.stat;

import com.github.pagehelper.Page;
import com.tcm.common.core.UserContextHolder;
import com.tcm.common.entity.BaseUser;
import com.tcm.common.entity.TablePageInfo;
import com.tcm.rx.service.stat.IStatService;
import com.tcm.rx.vo.rx.response.RxVO;
import com.tcm.rx.vo.stat.request.HerbStorageReqVO;
import com.tcm.rx.vo.stat.request.StatRequestVO;
import com.tcm.rx.vo.stat.response.*;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.github.pagehelper.page.PageMethod.startPage;

/**
 * <p>
 * 综合查询接口
 * </p>
 *
 * @author xph
 * @since 2025-07-10
 */
@RestController
@RequestMapping("/stat")
public class StatController {

    @Resource
    private IStatService statService;

    /**
     * 医院数据统计
     */
    @PostMapping("/hspRxList")
    public TablePageInfo<HspRxExportVO> hspRxList(@RequestBody StatRequestVO statRequestVO) {
        BaseUser user = UserContextHolder.getUserInfoContext();
        statRequestVO.setMedicalGroupId(user.getMedicalGroupId());
        Page<RxVO> page = startPage(statRequestVO.getPage(), statRequestVO.getSize());
        List<HspRxExportVO> resultList = statService.hspRxList(statRequestVO);
        return new TablePageInfo(resultList, Math.toIntExact(page.getTotal()));
    }

    /**
     * 医院数据统计导出
     */
    @PostMapping("/hspRxListExport")
    public void hspRxListExport(@RequestBody StatRequestVO statRequestVO, HttpServletResponse response) throws IOException {
        BaseUser user = UserContextHolder.getUserInfoContext();
        statRequestVO.setMedicalGroupId(user.getMedicalGroupId());
        statService.hspRxListExport(statRequestVO, response);
    }

    /**
     * 饮片使用明细
     */
    @PostMapping("/herbUseList")
    public TablePageInfo<HerbUseExportVO> herbUseList(@RequestBody StatRequestVO statRequestVO) {
        BaseUser user = UserContextHolder.getUserInfoContext();
        statRequestVO.setMedicalGroupId(user.getMedicalGroupId());
        Page page = startPage(statRequestVO.getPage(),statRequestVO.getSize());
        List<HerbUseExportVO> resultList = statService.herbUseList(statRequestVO);
        return new TablePageInfo(resultList, Math.toIntExact(page.getTotal()));
    }

    /**
     * 饮片使用明细导出
     */
    @PostMapping("/herbUseListExport")
    public void herbUseListExport(@RequestBody StatRequestVO statRequestVO, HttpServletResponse response) throws IOException {
        BaseUser user = UserContextHolder.getUserInfoContext();
        statRequestVO.setMedicalGroupId(user.getMedicalGroupId());
        statService.herbUseListExport(statRequestVO, response);
    }

    /**
     * 适宜技术统计
     */
    @PostMapping("/rxChargeList")
    public TablePageInfo<ChargeExportVO> rxChargeList(@RequestBody StatRequestVO statRequestVO) {
        BaseUser user = UserContextHolder.getUserInfoContext();
        statRequestVO.setMedicalGroupId(user.getMedicalGroupId());
        Page page = startPage(statRequestVO.getPage(),statRequestVO.getSize());
        List<ChargeExportVO> resultList = statService.rxChargeList(statRequestVO);
        return new TablePageInfo(resultList, Math.toIntExact(page.getTotal()));
    }

    /**
     * 适宜技术统计导出
     */
    @PostMapping("/rxChargeListExport")
    public void rxChargeListExport(@RequestBody StatRequestVO statRequestVO, HttpServletResponse response) throws IOException {
        BaseUser user = UserContextHolder.getUserInfoContext();
        statRequestVO.setMedicalGroupId(user.getMedicalGroupId());
        statService.herbUseListExport(statRequestVO, response);
    }

    /**
     * 适宜技术明细
     */
    @PostMapping("/rxChargeDetailList")
    public TablePageInfo<ChargeDetailExportVO> rxChargeDetailList(@RequestBody StatRequestVO statRequestVO) {
        BaseUser user = UserContextHolder.getUserInfoContext();
        statRequestVO.setMedicalGroupId(user.getMedicalGroupId());
        Page page = startPage(statRequestVO.getPage(),statRequestVO.getSize());
        List<ChargeDetailExportVO> resultList = statService.rxChargeDetailList(statRequestVO);
        return new TablePageInfo(resultList, Math.toIntExact(page.getTotal()));
    }

    /**
     * 适宜技术明细导出
     */
    @PostMapping("/rxChargeDetailListExport")
    public void rxChargeDetailListExport(@RequestBody StatRequestVO statRequestVO, HttpServletResponse response) throws IOException {
        BaseUser user = UserContextHolder.getUserInfoContext();
        statRequestVO.setMedicalGroupId(user.getMedicalGroupId());
        statService.rxChargeDetailListExport(statRequestVO, response);
    }

    /**
     * 医疗机构下拉选列表
     */
    @GetMapping("/getHspList")
    public Map<String,Long> getHspList() {
        return statService.getHspList();
    }

    /**
     * 历史适宜技术
     */
    @PostMapping("/historyChargeList")
    public TablePageInfo<HistoryChargeExportVO> historyChargeList(@RequestBody StatRequestVO statRequestVO) {
        BaseUser user = UserContextHolder.getUserInfoContext();
        statRequestVO.setMedicalGroupId(user.getMedicalGroupId());
        Page page = startPage(statRequestVO.getPage(),statRequestVO.getSize());
        List<HistoryChargeExportVO> resultList = statService.historyChargeList(statRequestVO);
        return new TablePageInfo(resultList, Math.toIntExact(page.getTotal()));
    }

    /**
     * 历史适宜技术导出
     */
    @PostMapping("/historyChargeListExport")
    public void historyChargeListExport(@RequestBody StatRequestVO statRequestVO, HttpServletResponse response) throws IOException {
        BaseUser user = UserContextHolder.getUserInfoContext();
        statRequestVO.setMedicalGroupId(user.getMedicalGroupId());
        statService.historyChargeListExport(statRequestVO, response);
    }

    /**
     * 入库信息查询
     */
    @GetMapping("/getHerbStorageList")
    public TablePageInfo<HerbStorageVO> getHerbStorageList(HerbStorageReqVO storageReqVO) {
        return statService.getHerbStorageList(storageReqVO);
    }

    /**
     * 无编码饮片明细
     */
    @PostMapping("/getNoCodeHerbDetail")
    public TablePageInfo<NoCodeHerbDetailVO> getNoCodeHerbDetail(@RequestBody StatRequestVO statRequestVO) {
        BaseUser user = UserContextHolder.getUserInfoContext();
        statRequestVO.setMedicalGroupId(user.getMedicalGroupId());
        Page page = startPage(statRequestVO.getPage(),statRequestVO.getSize());
        List<NoCodeHerbDetailVO> resultList = statService.getNoCodeHerbDetail(statRequestVO);
        return new TablePageInfo(resultList, Math.toIntExact(page.getTotal()));
    }
}
