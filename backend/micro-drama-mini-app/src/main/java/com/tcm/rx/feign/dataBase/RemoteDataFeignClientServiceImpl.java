package com.tcm.rx.feign.dataBase;

import com.alibaba.fastjson2.JSONObject;
import com.tcm.common.entity.Result;
import com.tcm.rx.config.HisDataRouteConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


/**
 * 远程调用dataBase
 */
@Service
@Slf4j
public class RemoteDataFeignClientServiceImpl implements RemoteDataFeignClient{

    @Autowired
    private HisDataRouteConfig routeMap;

    @Autowired
    private RestTemplate restTemplate;

    /**
     * 远程调用DataBase项目 （统一post json格式）
     *
     * @param path 接口路径
     * @param req  请求参数
     * @return
     */
    @Override
    public Result remoteData(String path, JSONObject req, String medicalGroupCode) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<JSONObject> requestEntity = new HttpEntity<>(req, headers);
        String route = routeMap.getRoute().containsKey(medicalGroupCode) ?
                routeMap.getRoute().get(medicalGroupCode) : routeMap.getRoute().get("default");
        String url = "http://" + route + "/" + route + path;
        log.info("请求dataBase入参url={}，参数={}", url, JSONObject.toJSONString(req));
        ResponseEntity<com.tcm.common.entity.Result> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, com.tcm.common.entity.Result.class);
        Result result = response.getBody();
        log.info("his推送返回结果：" + result.toString());//判断消息状态
        return result;
    }
}
