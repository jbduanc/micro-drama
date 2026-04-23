package com.tcm.rx.service.dict.impl;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.tcm.common.core.UserContextHolder;
import com.tcm.common.entity.BaseUser;
import com.tcm.common.entity.Result;
import com.tcm.common.enums.BooleanEnum;
import com.tcm.rx.feign.dict.DictDataServiceClient;
import com.tcm.rx.feign.dict.vo.DictDataQueryVO;
import com.tcm.rx.feign.dict.vo.DictDataVO;
import com.tcm.rx.service.dict.DictService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description 集团（医联体）系统--字典数据 接口实现类
 * @Author xph
 * @Date 2025/7/18 16:48
 */
@Service
public class DictServiceImpl implements DictService {

    @Resource
    private DictDataServiceClient dictDataServiceClient;

    @Override
    public List<DictDataVO> dictDataList(DictDataQueryVO queryVO) {
        if ((Objects.isNull(queryVO.getMedicalGroupId()) || queryVO.getMedicalGroupId() < 1)
                && StringUtils.isBlank(queryVO.getMedicalGroupCode())) {
            BaseUser loginUser = UserContextHolder.getUserInfoContext();
            queryVO.setMedicalGroupId(loginUser.getMedicalGroupId());
            queryVO.setMedicalGroupCode(loginUser.getMedicalGroupCode());
        }
        queryVO.setRxPerms(BooleanEnum.TRUE.getNum());
        Result result = dictDataServiceClient.dictDataList(queryVO);

        String jsonString = JSONObject.toJSONString(result.getData());
        if (StringUtils.isBlank(jsonString)) {
            return Collections.emptyList();
        }
        JSONArray jsonArray = JSONArray.parseArray(jsonString);
        if (CollectionUtils.isEmpty(jsonArray)) {
            return Collections.emptyList();
        }
        return jsonArray.toJavaList(DictDataVO.class);
    }

    /**
     * 获取字典map
     *
     * @param queryVO
     * @param isCodeMap
     * @return
     */
    public Map<String, String> getDictDataMap(DictDataQueryVO queryVO, boolean isCodeMap) {
        BaseUser loginUser = UserContextHolder.getUserInfoContext();
        queryVO.setMedicalGroupId(loginUser.getMedicalGroupId());
        queryVO.setMedicalGroupCode(loginUser.getMedicalGroupCode());
        queryVO.setRxPerms(BooleanEnum.TRUE.getNum());
        Result result = dictDataServiceClient.dictDataList(queryVO);
        String jsonString = JSONObject.toJSONString(result.getData());
        if (StringUtils.isBlank(jsonString)) {
            return new HashMap<>();
        }
        JSONArray jsonArray = JSONArray.parseArray(jsonString);
        if (CollectionUtils.isEmpty(jsonArray)) {
            return new HashMap<>();
        }
        if (isCodeMap) {
            return jsonArray.toJavaList(DictDataVO.class).stream().collect(Collectors.toMap(DictDataVO::getDictValue,
                    DictDataVO::getDictLabel, (existingValue, newValue) -> newValue));
        } else {
            return jsonArray.toJavaList(DictDataVO.class).stream().collect(Collectors.toMap(DictDataVO::getDictLabel,
                    DictDataVO::getDictValue, (existingValue, newValue) -> newValue));
        }
    }

}
