package com.tcm.rx.feign.dataBase;

import com.alibaba.fastjson2.JSONObject;
import com.tcm.common.entity.Result;


/**
 * 远程调用dataBase
 */
public interface RemoteDataFeignClient {

    /**
     * 远程调用DataBase项目 （统一post json格式）
     * 推送处方，发药等等
     *
     * @param path 接口路径
     * @param req  请求参数
     * @return
     */
    public Result remoteData(String path, JSONObject req, String medicalGroupCode);


}
