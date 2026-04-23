package com.tcm.rx.service.basicData.impl;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.tcm.common.entity.Result;
import com.tcm.rx.feign.college.MedicalRecordsFeignClient;
import com.tcm.rx.feign.college.TcmKnowledgeFeignClient;
import com.tcm.rx.feign.college.VideoCourseFeignClient;
import com.tcm.rx.feign.college.vo.MedicalRecordsVO;
import com.tcm.rx.feign.college.vo.TcmKnowledgeVO;
import com.tcm.rx.feign.college.vo.VideoCourseVO;
import com.tcm.rx.service.basicData.ICollegeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class CollegeServiceImpl implements ICollegeService {
    @Autowired
    private MedicalRecordsFeignClient recordsFeignClient;

    @Autowired
    private TcmKnowledgeFeignClient knowledgeFeignClient;

    @Autowired
    private VideoCourseFeignClient courseFeignClient;

    @Override
    public JSONObject medicalRecordsList(MedicalRecordsVO medicalRecordsVO) {
        Result result = recordsFeignClient.pageList(medicalRecordsVO);
        log.info("请求医案列表接口返回数据：{}", JSONUtil.toJsonStr(result.getData()));
        return JSONUtil.parseObj(result.getData());
    }

    @Override
    public JSONObject medicalRecordsDetail(Long id) {
        Result result = recordsFeignClient.medicalRecordsVOById(id);
        log.info("请求医案详情接口返回数据：{}", JSONUtil.toJsonStr(result.getData()));
        return JSONUtil.parseObj(result.getData());
    }

    @Override
    public JSONObject tcmKnowledgeList(TcmKnowledgeVO tcmKnowledgeVO) {
        Result result = knowledgeFeignClient.pageList(tcmKnowledgeVO);
        log.info("请求中医知识列表接口返回数据：{}", JSONUtil.toJsonStr(result.getData()));
        return JSONUtil.parseObj(result.getData());
    }

    @Override
    public JSONObject tcmKnowledgeDetail(Long id) {
        Result result = knowledgeFeignClient.tcmKnowledgeById(id);
        log.info("请求中医知识详情接口返回数据：{}", JSONUtil.toJsonStr(result.getData()));
        return JSONUtil.parseObj(result.getData());
    }


    @Override
    public JSONObject videoCourseList(VideoCourseVO videoCourseVO) {
        Result result = courseFeignClient.pageList(videoCourseVO);
        log.info("请求视频课列表接口返回数据：{}", JSONUtil.toJsonStr(result.getData()));
        return JSONUtil.parseObj(result.getData());
    }
}
