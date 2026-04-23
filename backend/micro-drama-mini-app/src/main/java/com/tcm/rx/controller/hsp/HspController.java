package com.tcm.rx.controller.hsp;

import com.github.pagehelper.Page;
import com.tcm.common.core.UserContextHolder;
import com.tcm.common.entity.BaseUser;
import com.tcm.common.entity.TablePageInfo;
import com.tcm.common.enums.BooleanEnum;
import com.tcm.rx.entity.hsp.Hsp;
import com.tcm.rx.service.hsp.IHspService;
import com.tcm.rx.vo.hsp.request.HspQueryVO;
import com.tcm.rx.vo.hsp.response.HspVO;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

import static com.github.pagehelper.page.PageMethod.startPage;

/**
 * <p>
 * 诊疗开方系统--医疗机构表 前端控制器
 * </p>
 *
 * @author xph
 * @since 2025-07-10
 */
@RestController
@RequestMapping("/hsp")
public class HspController {

    @Resource
    private IHspService hspService;

    /**
     * 查询医疗机构的数据（分页查询）
     */
    @PostMapping("/hspPageList")
    public TablePageInfo<HspVO> hspPageList(@RequestBody HspQueryVO queryVO) {
        Page page = startPage(queryVO.getPage(),queryVO.getSize());
        BaseUser loginUser = UserContextHolder.getUserInfoContext();
        if (Objects.nonNull(loginUser)
                && BooleanEnum.FALSE.getNum().longValue() != loginUser.getHspId()
                && !BooleanEnum.FALSE.getNumStr().equals(loginUser.getHspCode())){
            queryVO.setId(loginUser.getHspId());
            queryVO.setHspCode(loginUser.getHspCode());
        }
        List<HspVO> resultList = hspService.hspList(queryVO);
        return new TablePageInfo(resultList, Math.toIntExact(page.getTotal()));
    }

    /**
     * 查询医疗机构的数据（分页查询）
     */
    @PostMapping("/hspPageAllList")
    public TablePageInfo<HspVO> hspPageAllList(@RequestBody HspQueryVO queryVO) {
        Page page = startPage(queryVO.getPage(),queryVO.getSize());
        List<HspVO> resultList = hspService.hspList(queryVO);
        return new TablePageInfo(resultList, Math.toIntExact(page.getTotal()));
    }

    /**
     * 根据id查询医疗机构的数据
     */
    @GetMapping(value = "/{id}")
    public HspVO hspById(@PathVariable("id") Long id){
        HspVO result = new HspVO();
        Hsp hsp = hspService.getById(id);
        if(Objects.nonNull(hsp)){
            BeanUtils.copyProperties(hsp, result);
        }
        return result;
    }

    /**
     * 医疗机构管理--新增医疗机构
     */
    @PostMapping(value = "/insertHsp")
    public Long insertHsp(@RequestBody HspVO hspVO){
        return hspService.insertHsp(hspVO);
    }

    /**
     * 医疗机构管理--批量新增医疗机构
     */
    @PostMapping(value = "/batchInsertHsp")
    public Boolean batchInsertHsp(@RequestBody List<HspVO> hspVOList){
        return hspService.batchInsertHsp(hspVOList);
    }

    /**
     * 医疗机构管理--更新医疗机构
     */
    @PostMapping(value = "/updateHsp")
    public Long updateHsp(@RequestBody HspVO hspVO){
        return hspService.updateHsp(hspVO);
    }

    /**
     * 医疗机构管理--删除医疗机构
     */
    @GetMapping(value = "/delete/{id}")
    public void deleteHsp(@PathVariable("id") Long id) {
        hspService.deleteHsp(id);
    }

    /**
     * 医疗机构管理--根据医联体，删除医疗机构
     */
    @GetMapping(value = "/deleteHspByMedicalGroup/{medicalGroupId}")
    public void deleteHspByMedicalGroup(@PathVariable("medicalGroupId") Long medicalGroupId) {
        hspService.deleteHspByMedicalGroup(medicalGroupId);
    }

    /**
     * 医疗机构管理--下载导入模版
     */
    @PostMapping(value = "/download/templateHsp")
    public void downloadTemplateHsp(HttpServletResponse response) throws IOException {
        hspService.downloadTemplate(response);
    }

    /**
     * 医疗机构管理--导入数据
     */
    @PostMapping("/importInfoHsp")
    public List<HspVO> importInfoHsp(MultipartFile file) throws Exception {
        return hspService.importInfo(file);
    }

    /**
     * 医疗机构管理--导出数据
     */
    @PostMapping("/exportHsp")
    public void export(HttpServletResponse response, @RequestBody HspQueryVO queryVO) throws IOException {
        hspService.export(response, queryVO);
    }

}

