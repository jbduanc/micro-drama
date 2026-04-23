package com.tcm.rx.controller.basicData;


import cn.hutool.json.JSONObject;
import com.tcm.rx.feign.college.vo.MedicalRecordsVO;
import com.tcm.rx.feign.college.vo.TcmKnowledgeVO;
import com.tcm.rx.feign.college.vo.VideoCourseVO;
import com.tcm.rx.service.basicData.ICollegeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;


@Slf4j
@RestController
@RequestMapping("/college")
public class CollegeController {
    @Autowired
    private ICollegeService collegeService;

    @GetMapping("/medicalRecordsList")
    public JSONObject medicalRecordsList(MedicalRecordsVO medicalRecordsVO){
        return collegeService.medicalRecordsList(medicalRecordsVO);
    }

    @GetMapping("/medicalRecordsDetail/{id}")
    public JSONObject medicalRecordsList(@PathVariable("id") Long id){
        return collegeService.medicalRecordsDetail(id);
    }

    @GetMapping("/tcmKnowledgeList")
    public JSONObject tcmKnowledgeList(TcmKnowledgeVO tcmKnowledgeVO){
        return collegeService.tcmKnowledgeList(tcmKnowledgeVO);
    }

    @GetMapping("/tcmKnowledgeDetail/{id}")
    public JSONObject tcmKnowledgeDetail(@PathVariable("id") Long id){
        return collegeService.tcmKnowledgeDetail(id);
    }

    @GetMapping("/videoCourseList")
    public JSONObject videoCourseList(VideoCourseVO videoCourseVO){
        return collegeService.videoCourseList(videoCourseVO);
    }
}
