package com.tcm.rx.service.basicData.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSON;
import com.github.pagehelper.Page;
import com.tcm.common.entity.Result;
import com.tcm.common.entity.TablePageInfo;
import com.tcm.rx.feign.manage.DiagnosisFeignClient;
import com.tcm.rx.feign.manage.TcmDiseaseFeignClient;
import com.tcm.rx.feign.manage.TcmPatternFeignClient;
import com.tcm.rx.feign.manage.vo.DiagnosisQueryVO;
import com.tcm.rx.feign.manage.vo.TcmDiseaseQueryVO;
import com.tcm.rx.feign.manage.vo.TcmPatternQueryVO;
import com.tcm.rx.service.basicData.IManageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.pagehelper.page.PageMethod.startPage;

@Service
@Slf4j
public class ManageServiceImpl implements IManageService {
    @Autowired
    private DiagnosisFeignClient diagnosisFeignClient;

    @Autowired
    private TcmDiseaseFeignClient tcmDiseaseFeignClient;

    @Autowired
    private TcmPatternFeignClient tcmPatternFeignClient;

    @Override
    public JSONObject diagnosisList(DiagnosisQueryVO vo) {
        Result result = diagnosisFeignClient.pageList(vo);
        log.info("请求西医诊断列表接口返回数据：{}", JSONUtil.toJsonStr(result.getData()));
        return JSONUtil.parseObj(result.getData());
    }

    @Override
    public JSONObject tcmDiseaseList(TcmDiseaseQueryVO vo) {
        Result result = tcmDiseaseFeignClient.pageList(vo);
        log.info("请求中医诊断列表接口返回数据：{}", JSONUtil.toJsonStr(result.getData()));
        return JSONUtil.parseObj(result.getData());
    }

    @Override
    public JSONObject tcmPatternList(TcmPatternQueryVO vo) {
        Result result = tcmPatternFeignClient.pageList(vo);
        log.info("请求中医证候列表接口返回数据：{}", JSONUtil.toJsonStr(result.getData()));
        return JSONUtil.parseObj(result.getData());
    }

    /**
     * tcmPatternMap
     *
     * @return
     */
    public Map<Long, String> tcmPatternMap() {
        TcmPatternQueryVO vo = new TcmPatternQueryVO();
        vo.setPage(1);
        vo.setSize(10000);
        Result result = tcmPatternFeignClient.pageList(vo);
        return convertToMap(JSON.toJSONString(result.getData()));
    }

    /**
     * tcmDiseaseMap
     *
     * @return
     */
    public Map<Long, String> tcmDiseaseMap() {
        TcmDiseaseQueryVO vo = new TcmDiseaseQueryVO();
        vo.setPage(1);
        vo.setSize(10000);
        Result result = tcmDiseaseFeignClient.pageList(vo);
        log.info("tcmDiseaseResult: {}", JSONUtil.toJsonStr(result));
        return convertToMap(JSON.toJSONString(result.getData()));
    }

    /**
     * diagnosisMap
     *
     * @return
     */
    public Map<Long, String> diagnosisMap() {
        DiagnosisQueryVO vo = new DiagnosisQueryVO();
        vo.setPage(1);
        vo.setSize(10000);
        Result result = diagnosisFeignClient.pageList(vo);
        return convertToMap(JSON.toJSONString(result.getData()));
    }

    /**
     * 将包含JSONArray的JSONObject转换为Map<Long, String>
     *
     * @param jsonArrStr 包含JSONArray的JSONObject
     * @return 转换后的Map，key为id，value为name
     */
    public static Map<Long, String> convertToMap(String jsonArrStr) {
        Map<Long, String> resultMap = new HashMap<>();
        if (ObjectUtil.isEmpty(jsonArrStr)) {
            return resultMap;
        }
        log.info("jsonArrStr: {}", JSONUtil.toJsonStr(jsonArrStr));
        com.alibaba.fastjson2.JSONArray jsonArray = JSON.parseObject(jsonArrStr).getJSONArray("data");
        if (ObjectUtil.isEmpty(jsonArray)) {
            return resultMap;
        }
        for (int i = 0; i < jsonArray.size(); i++) {
            com.alibaba.fastjson2.JSONObject item = jsonArray.getJSONObject(i);
            if (item != null) {
                Long id = item.getLong("id");
                String name = item.getString("name");
                if (id != null && name != null) {
                    resultMap.put(id, name);
                }
            }
        }
        return resultMap;
    }
}
