package com.tcm.rx.feign.herb;

import com.tcm.common.config.security.FeignUserInfoConfig;
import com.tcm.common.entity.Result;
import com.tcm.rx.feign.herb.vo.CustomerHerbVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * 饮片库存 Feign 客户端
 */
@FeignClient(name = "tcm-sharing-decoction", contextId = "herb-stock-service",
        path = "/tcm-sharing-decoction/herbStock", configuration = FeignUserInfoConfig.class)
public interface HerbStockFeignClient {
    /**
     * 饮片库存查询
     */
    @PostMapping("/queryCustomerHerbStock")
    Result queryCustomerHerbStock(@RequestBody List<CustomerHerbVO> customerHerbVOS);

    /**
     * 根据His采购单号查询饮片是否入库
     * @return
     */
    @GetMapping("/getHerbPurchaseByHisPurchaseNo/{hisPurchaseNo}")
    Result getHerbPurchaseByHisPurchaseNo(@PathVariable("hisPurchaseNo") String hisPurchaseNo);

}