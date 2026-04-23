package com.tcm.rx.controller.dict;

import com.tcm.rx.feign.dict.vo.DictDataQueryVO;
import com.tcm.rx.feign.dict.vo.DictDataVO;
import com.tcm.rx.service.dict.DictService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 诊疗系统--feign调用集团（医联体）系统--字典表 前端控制器
 * </p>
 *
 * @author xph
 * @since 2025-07-18
 */
@RestController
@RequestMapping("/dictDataRxFeign")
public class DictDataRxFeignController {

    @Resource
    private DictService dictService;

    /**
     * 查询字典数据
     */
    @PostMapping("/dictDataList")
    public List<DictDataVO> dictDataList(@RequestBody DictDataQueryVO queryVO) {
        return dictService.dictDataList(queryVO);
    }

}

