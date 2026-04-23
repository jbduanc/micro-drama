package com.tcm.rx.feign.dict;

import com.tcm.common.config.security.FeignUserInfoConfig;
import com.tcm.common.entity.Result;
import com.tcm.rx.feign.dict.vo.DictDataQueryVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * 集团（医联体）系统--字典数据表--请求类P端
 */
@FeignClient(name = "tcm-sharing-admin", contextId = "dict-data-service",
        path = "/tcm-sharing-admin/dictData",configuration = FeignUserInfoConfig.class)
public interface DictDataServiceClient {

    /**
     * 查询字典数据
     */
    @PostMapping("/dictDataList")
    Result dictDataList(DictDataQueryVO queryVO);

}
