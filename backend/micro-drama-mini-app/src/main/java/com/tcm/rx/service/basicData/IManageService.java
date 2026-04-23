package com.tcm.rx.service.basicData;

import cn.hutool.json.JSONObject;
import com.tcm.rx.feign.college.vo.MedicalRecordsVO;
import com.tcm.rx.feign.college.vo.TcmKnowledgeVO;
import com.tcm.rx.feign.college.vo.VideoCourseVO;
import com.tcm.rx.feign.manage.vo.DiagnosisQueryVO;
import com.tcm.rx.feign.manage.vo.TcmDiseaseQueryVO;
import com.tcm.rx.feign.manage.vo.TcmPatternQueryVO;

import java.util.Map;

public interface IManageService {

    JSONObject diagnosisList(DiagnosisQueryVO vo);

    JSONObject tcmDiseaseList(TcmDiseaseQueryVO vo);

    JSONObject tcmPatternList(TcmPatternQueryVO vo);

    /**
     * diagnosisMap
     *
     * @return
     */
    public Map<Long, String> diagnosisMap();

    /**
     * tcmDiseaseMap
     *
     * @return
     */
    public Map<Long, String> tcmDiseaseMap();

    /**
     * tcmPatternMap
     *
     * @return
     */
    public Map<Long, String> tcmPatternMap();
}
