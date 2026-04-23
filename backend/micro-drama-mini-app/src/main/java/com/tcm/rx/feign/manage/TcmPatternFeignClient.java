package com.tcm.rx.feign.manage;

import com.tcm.common.config.security.FeignUserInfoConfig;
import com.tcm.common.entity.Result;
import com.tcm.rx.feign.manage.vo.TcmPatternQueryVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 医疗机构 Feign 客户端
 */
@FeignClient(name = "tcm-sharing-admin", contextId = "tcmPattern-service",
        path = "/tcm-sharing-admin/tcmPattern", configuration = FeignUserInfoConfig.class)
public interface TcmPatternFeignClient {
    /**
     * 查询中医证候
     */
    @PostMapping("/pageList")
    Result pageList(@RequestBody TcmPatternQueryVO videoCourseVO);

}