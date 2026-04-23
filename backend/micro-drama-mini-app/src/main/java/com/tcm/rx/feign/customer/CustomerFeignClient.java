package com.tcm.rx.feign.customer;

import com.tcm.common.config.security.FeignUserInfoConfig;
import com.tcm.common.entity.Result;
import com.tcm.rx.feign.herb.vo.CustomerHerbVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 医疗机构 Feign 客户端
 */
@FeignClient(name = "tcm-sharing-decoction", contextId = "customer-service",
        path = "/tcm-sharing-decoction/customer", configuration = FeignUserInfoConfig.class)
public interface CustomerFeignClient {
    /**
     * 查询医疗机构
     */
    @GetMapping("/feign/list")
    Result customerList(@RequestParam("medicalGroupCode") String medicalGroupCode);

}