package com.tcm.rx.controller.rx;


import com.github.pagehelper.Page;
import com.tcm.common.core.UserContextHolder;
import com.tcm.common.entity.BaseUser;
import com.tcm.common.entity.TablePageInfo;
import com.tcm.rx.service.rx.IRxService;
import com.tcm.rx.vo.rx.request.RxQueryVO;
import com.tcm.rx.vo.rx.request.RxSaveVO;
import com.tcm.rx.vo.rx.request.RxStatusVO;
import com.tcm.rx.vo.rx.response.RxVO;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

import static com.github.pagehelper.page.PageMethod.startPage;

/**
 * <p>
 * 诊疗开方系统--处方主表 前端控制器
 * </p>
 *
 * @author shouhan
 * @since 2025-07-21
 */
@RestController
@RequestMapping("/rx")
public class RxController {
    @Resource
    private IRxService rxService;

    /**
     * 分页查询
     */
    @PostMapping("/pageList")
    public TablePageInfo<RxVO> pageList(@RequestBody RxQueryVO queryVO) {
        BaseUser user = UserContextHolder.getUserInfoContext();
        queryVO.setMedicalGroupId(user.getMedicalGroupId());
        Page<RxVO> page = startPage(queryVO.getPage(), queryVO.getSize());
        List<RxVO> list = rxService.queryPage(queryVO);
        return new TablePageInfo<>(list, Math.toIntExact(page.getTotal()));
    }

    /**
     * 获取处方详情
     *
     * @param id
     * @return
     */
    @PostMapping("/getRxDetail/{id}")
    public RxVO getRxDetail(@PathVariable String id) {
        return rxService.getRxVo(id, null, null, null, null);
    }

    /**
     * 保存处方
     *
     * @param dto
     * @return
     */
    @PostMapping("/saveRx")
    public String saveRx(@RequestBody RxSaveVO dto) {
        return rxService.saveRx(dto);
    }

    /**
     * 处方明细导出
     */
    @PostMapping("/exportRxDetail")
    public void exportRxDetail(@RequestBody RxQueryVO queryVO, HttpServletResponse response) throws IOException {
        BaseUser user = UserContextHolder.getUserInfoContext();
        queryVO.setMedicalGroupId(user.getMedicalGroupId());
        rxService.exportRxDetail(queryVO, response);
    }

    /**
     * 发药接口
     *
     * @param rxStatusVO
     * @return
     */
    @PostMapping("/updateRxStatus")
    public Boolean dispenser(@RequestBody RxStatusVO rxStatusVO) {
        return rxService.dispenser(rxStatusVO);
    }

    /**
     * 批量发药接口
     *
     * @param rxStatusVOList
     * @return
     */
    @PostMapping("/batchUpdateRxStatus")
    public Boolean batchDispenser(@RequestBody List<RxStatusVO> rxStatusVOList) {
        return rxService.batchDispenser(rxStatusVOList);
    }

    /**
     * his修改处方状态
     *
     * @param rxStatusVO
     * @return
     */
    @PostMapping("/updateRxStatusByHis")
    public Boolean updateRxStatusByHis(@RequestBody RxStatusVO rxStatusVO) {
        return rxService.updateRxStatus(rxStatusVO);
    }
}

