package com.tcm.rx.feign.manage;

import com.tcm.common.config.security.FeignUserInfoConfig;
import com.tcm.common.entity.Result;
import com.tcm.rx.feign.manage.vo.TcmDiseaseQueryVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 医疗机构 Feign 客户端
 */
@FeignClient(name = "tcm-sharing-admin", contextId = "tcmDisease-service",
        path = "/tcm-sharing-admin/tcmDisease", configuration = FeignUserInfoConfig.class)
public interface TcmDiseaseFeignClient {
    /**
     * 查询中医诊断
     */
    @PostMapping("/pageList")
    Result pageList(@RequestBody TcmDiseaseQueryVO tcmDiseaseQueryVO);

}