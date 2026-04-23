package com.tcm.rx.feign.herb.herbCustomer;

import com.tcm.common.config.security.FeignUserInfoConfig;
import com.tcm.common.entity.Result;
import com.tcm.rx.feign.herb.herbCustomer.vo.HerbCustomer;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * 饮片库存 Feign 客户端
 */
@FeignClient(name = "tcm-sharing-decoction", contextId = "herb-customer-service",
        path = "/tcm-sharing-decoction/HerbCustomer", configuration = FeignUserInfoConfig.class)
public interface HerbCustomerFeignClient {
    /**
     * 饮片库存查询
     */
    @GetMapping("/findHerbCustomer")
    Result findHerbCustomer(@SpringQueryMap HerbCustomer herbCustomer);

}