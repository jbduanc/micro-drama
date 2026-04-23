package com.tcm.rx.controller.patient;

import com.tcm.common.patient.request.HisInfoUploadDTO;
import com.tcm.common.patient.response.PatientHisRespDTO;
import com.tcm.rx.service.patient.PatientService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * <p>
 * 诊疗开方系统--跳转中药诊疗系统，上传患者和医生数据 对接接口
 * </p>
 *
 * @author xph
 * @since 2025-07-23
 */
@RestController
@RequestMapping("/patient")
public class PatientController {

    @Resource
    private PatientService patientService;

    /**
     * 处理跳转中药诊疗系统，上传患者和医生数据
     */
    @PostMapping("/info/upload")
    public PatientHisRespDTO patientInfoUpload(@RequestBody HisInfoUploadDTO hisInfoUploadDTO) throws Exception {
        return patientService.patientInfoUpload(hisInfoUploadDTO);
    }

}

