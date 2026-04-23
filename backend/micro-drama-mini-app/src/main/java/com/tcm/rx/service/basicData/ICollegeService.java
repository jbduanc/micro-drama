package com.tcm.rx.service.basicData;

import cn.hutool.json.JSONObject;
import com.tcm.rx.feign.college.vo.MedicalRecordsVO;
import com.tcm.rx.feign.college.vo.TcmKnowledgeVO;
import com.tcm.rx.feign.college.vo.VideoCourseVO;

public interface ICollegeService {
    JSONObject medicalRecordsList(MedicalRecordsVO medicalRecordsVO);

    JSONObject tcmKnowledgeList(TcmKnowledgeVO tcmKnowledgeVO);

    JSONObject videoCourseList(VideoCourseVO videoCourseVO);

    JSONObject medicalRecordsDetail(Long id);

    JSONObject tcmKnowledgeDetail(Long id);
}
