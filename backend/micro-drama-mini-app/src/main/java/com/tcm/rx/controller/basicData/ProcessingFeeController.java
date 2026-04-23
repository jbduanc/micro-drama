package com.tcm.rx.controller.basicData;

import com.tcm.common.entity.TablePageInfo;
import com.tcm.rx.service.basicData.processFee.ProcessingFeeService;
import com.tcm.rx.vo.basicData.ProcessingFeeVo.ProcessingFeeEditVO;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

/**
 * 诊疗开方系统--加工费管理表(ProcessingFee)表控制层
 *
 * @author duanqiyuan
 * @since 2025-07-17 14:58:47
 */
@RestController
@RequestMapping("/processingFee")
@RequiredArgsConstructor
public class ProcessingFeeController {

    /**
     * 服务对象
     */
    private final ProcessingFeeService processingFeeService;

    /**
     * 加工费管理-新增加工费
     *
     * @param processingFeeAddVO
     * @return
     */
    @PostMapping
    public Long saveProcessingFee(@RequestBody ProcessingFeeEditVO processingFeeAddVO) {
        return processingFeeService.saveProcessingFee(processingFeeAddVO);
    }

    ;

    /**
     * 加工费管理-编辑加工费
     *
     * @param processingFeeAddVO
     * @return
     */
    @PostMapping("/update")
    public Long updateProcessingFee(@RequestBody ProcessingFeeEditVO processingFeeAddVO) {
        return processingFeeService.updateProcessingFee(processingFeeAddVO);
    }

    /**
     * 加工费管理-删除加工费
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Boolean delProcessingFee(@PathVariable(name = "id")  Long id) {
        return processingFeeService.delProcessingFee(id);
    }

    /**
     * 加工费管理-查找加工费
     *
     * @param id
     * @param decoctRequire
     * @return
     */
    @GetMapping
    public TablePageInfo<ProcessingFeeEditVO> selProcessingFee(
            @RequestParam(name = "id", required = false) Long id,
            @RequestParam(name = "decoctRequire", required = false) String decoctRequire,
            @RequestParam(name = "page", required = false, defaultValue = "1") Integer page,
            @RequestParam(name = "size", required = false, defaultValue = "10") Integer size) {
        return processingFeeService.selProcessingFee(id,decoctRequire,page,size);
    }

}

