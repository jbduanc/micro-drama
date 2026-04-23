package com.tcm.rx.controller.rx;


import com.github.pagehelper.Page;
import com.tcm.common.entity.TablePageInfo;
import com.tcm.rx.entity.rx.AgreementRxDetail;
import com.tcm.rx.entity.rx.RxMgmtDetail;
import com.tcm.rx.service.rx.IAgreementRxService;
import com.tcm.rx.vo.hsp.request.HspQueryVO;
import com.tcm.rx.vo.hsp.response.HspVO;
import com.tcm.rx.vo.rx.request.*;
import com.tcm.rx.vo.rx.response.AgreementRxDetailVO;
import com.tcm.rx.vo.rx.response.AgreementRxListVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

import static com.github.pagehelper.page.PageMethod.startPage;

/**
 * <p>
 * 诊疗开方系统--协定方主表 前端控制器
 * </p>
 *
 * @author shouhan
 * @since 2025-07-17
 */
@RestController
@RequestMapping("/agreementRx")
public class AgreementRxController {

    @Autowired
    private IAgreementRxService agreementRxService;

    /**
     * 协定方新增/修改
     */
    @PostMapping("/save")
    public Long save(@RequestBody SaveAgreementRxVO saveVo) {
        return agreementRxService.save(saveVo);
    }

    /**
     * 查询协定方数据（分页查询）
     */
    @PostMapping("/page")
    public TablePageInfo<AgreementRxListVO> agreementRxPage(@RequestBody AgreementRxQueryVO queryVO) {
        Page page = startPage(queryVO.getPage(),queryVO.getSize());
        List<AgreementRxListVO> resultList = agreementRxService.agreementRxList(queryVO);
        return new TablePageInfo(resultList, Math.toIntExact(page.getTotal()));
    }


    /**
     * 详情
     */
    @GetMapping("/detail/{id}")
    public AgreementRxDetailVO detail(@PathVariable Long id) {
        AgreementRxDetailVO result = agreementRxService.detail(id);
        return result;
    }

    /**
     * 删除
     */
    @GetMapping("/delete/{id}")
    public void delete(@PathVariable Long id) {
        agreementRxService.delete(id);
    }


    /**
     * 下载模版
     *
     * @Param isCurrDoctor true 个人模版导入 false 协定方管理导入
     */
    @GetMapping(value = "/download/template")
    public void downloadTemplate(HttpServletResponse response, Boolean isCurrDoctor) throws IOException {
        agreementRxService.downloadTemplate(response, isCurrDoctor);
    }

    /**
     * 导入（返回列表）
     *
     * @Param isCurrDoctor true 个人模版导入 false 协定方管理导入
     */
    @PostMapping("/import")
    public List importAgreementRx(MultipartFile file,  Boolean isCurrDoctor) throws Exception {
        return agreementRxService.importAgreementRx(file, isCurrDoctor);
    }

    /**
     * 导入确认新增
     */
    @PostMapping("/import/add")
    public Boolean importAdd(@RequestBody AgreementRxExcelAddVO vo){
        //新增逻辑
        return agreementRxService.importAdd(vo);
    }

    /**
     * 获取转化后的饮片详情
     */
    @PostMapping("/getTransDetails/{id}")
    public List<AgreementRxDetail> getTransDetails(@PathVariable Long id) {
        return agreementRxService.getTransDetails(id);
    }

}

