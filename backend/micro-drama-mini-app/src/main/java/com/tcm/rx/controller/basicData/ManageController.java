package com.tcm.rx.controller.basicData;

import cn.hutool.json.JSONObject;

import com.tcm.rx.feign.manage.vo.DiagnosisQueryVO;
import com.tcm.rx.feign.manage.vo.TcmDiseaseQueryVO;
import com.tcm.rx.feign.manage.vo.TcmPatternQueryVO;
import com.tcm.rx.service.basicData.IManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/manage")
public class ManageController {
    @Autowired
    private IManageService manageService;
    /**
     * 西医疾病
     * @return
     */
    @GetMapping("/diagnosisList")
    public JSONObject diagnosisList(DiagnosisQueryVO vo){
        return manageService.diagnosisList(vo);
    }

    /**
     * 中医疾病
     */
    @GetMapping("/tcmDiseaseList")
    public JSONObject tcmDiseaseList(TcmDiseaseQueryVO vo){
        return manageService.tcmDiseaseList(vo);
    }

    /**
     * 中医症侯
     */
    @GetMapping("/tcmPatternList")
    public JSONObject tcmPatternList(TcmPatternQueryVO vo){
        return manageService.tcmPatternList(vo);
    }
}
