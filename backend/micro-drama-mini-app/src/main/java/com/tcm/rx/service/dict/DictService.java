package com.tcm.rx.service.dict;

import com.tcm.rx.feign.dict.vo.DictDataQueryVO;
import com.tcm.rx.feign.dict.vo.DictDataVO;

import java.util.List;
import java.util.Map;

/**
 * 集团（医联体）系统--字典数据 接口
 */
public interface DictService {

    /**
     * 查询字典数据
     */
    List<DictDataVO> dictDataList(DictDataQueryVO queryVO);

    /**
     * 获取字典map
     *
     * @param queryVO
     * @param isCodeMap
     * @return
     */
    public Map<String, String> getDictDataMap(DictDataQueryVO queryVO, boolean isCodeMap);

}
