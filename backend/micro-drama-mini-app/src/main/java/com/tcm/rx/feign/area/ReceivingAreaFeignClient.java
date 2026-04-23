package com.tcm.rx.feign.area;

import com.tcm.common.config.security.FeignUserInfoConfig;
import com.tcm.common.entity.Result;
import com.tcm.rx.feign.area.vo.City;
import com.tcm.rx.feign.area.vo.District;
import com.tcm.rx.feign.area.vo.Province;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 收货地址 Feign 接口
 */
@FeignClient(name = "tcm-sharing-decoction", contextId = "receiving-area-service",
        path = "/tcm-sharing-decoction/receivingArea", configuration = FeignUserInfoConfig.class)
public interface ReceivingAreaFeignClient {

    /**
     * 城市列表
     *
     * @param name
     * @param provinceId
     * @return
     */
    @GetMapping("/cities")
    Result listCitiesByNameAndProvince(@RequestParam(value = "name", required = false) String name,
                                       @RequestParam(value = "provinceId") Long provinceId);

    /**
     * 区县列表
     *
     * @param name
     * @param cityId
     * @return
     */
    @GetMapping("/districts")
    Result listDistrictsByNameAndParent(@RequestParam(value = "name", required = false) String name,
                                                @RequestParam(value = "cityId") Long cityId);
    /**
     * 省份列表
     *
     * @param name
     * @return
     */
    @GetMapping("/provinces")
    Result listProvincesByName(@RequestParam(value = "name", required = false) String name);
}