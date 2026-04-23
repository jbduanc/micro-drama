package com.tcm.rx.controller.rx;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.tcm.common.entity.Result;
import com.tcm.rx.feign.area.ReceivingAreaFeignClient;
import com.tcm.rx.feign.dict.vo.DictDataVO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.tcm.rx.feign.area.vo.City;
import com.tcm.rx.feign.area.vo.District;
import com.tcm.rx.feign.area.vo.Province;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

/**
 * 收货地址接口
 */
@RestController
@RequestMapping("/receivingArea")
public class ReceivingAreaController {

    @Resource
    private ReceivingAreaFeignClient receivingAreaFeignClient;

    /**
     * 城市列表
     *
     * @param name
     * @param provinceId
     * @return
     */
    @GetMapping("/cities")
    public List<City> listCitiesByNameAndProvince(@RequestParam(required = false) String name,
                                                  @RequestParam Long provinceId) {
        Result result = receivingAreaFeignClient.listCitiesByNameAndProvince(name, provinceId);
        String jsonString = JSONObject.toJSONString(result.getData());
        if (StringUtils.isBlank(jsonString)) {
            return Collections.emptyList();
        }
        JSONArray jsonArray = JSONArray.parseArray(jsonString);
        if (CollectionUtils.isEmpty(jsonArray)) {
            return Collections.emptyList();
        }
        return jsonArray.toJavaList(City.class);
    }

    /**
     * 区县列表
     *
     * @param name
     * @param cityId
     * @return
     */
    @GetMapping("/districts")
    public List<District> listDistrictsByNameAndParent(@RequestParam(required = false) String name,
                                                       @RequestParam Long cityId) {
        Result result = receivingAreaFeignClient.listDistrictsByNameAndParent(name, cityId);
        String jsonString = JSONObject.toJSONString(result.getData());
        if (StringUtils.isBlank(jsonString)) {
            return Collections.emptyList();
        }
        JSONArray jsonArray = JSONArray.parseArray(jsonString);
        if (CollectionUtils.isEmpty(jsonArray)) {
            return Collections.emptyList();
        }
        return jsonArray.toJavaList(District.class);
    }

    /**
     * 省份列表
     *
     * @param name
     * @return
     */
    @GetMapping("/provinces")
    public List<Province> listProvincesByName(@RequestParam(required = false) String name) {
        Result result = receivingAreaFeignClient.listProvincesByName(name);
        String jsonString = JSONObject.toJSONString(result.getData());
        if (StringUtils.isBlank(jsonString)) {
            return Collections.emptyList();
        }
        JSONArray jsonArray = JSONArray.parseArray(jsonString);
        if (CollectionUtils.isEmpty(jsonArray)) {
            return Collections.emptyList();
        }
        return jsonArray.toJavaList(Province.class);
    }
}