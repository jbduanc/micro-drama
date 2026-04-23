package com.tcm.rx.service.patient;

import com.tcm.common.patient.request.HisInfoUploadDTO;
import com.tcm.common.patient.response.PatientHisRespDTO;

/**
 * 诊疗开方系统--跳转中药诊疗系统，上传患者和医生数据 对接接口
 */
public interface PatientService {

    /**
     * 处理跳转中药诊疗系统，上传患者和医生数据
     */
    PatientHisRespDTO patientInfoUpload(HisInfoUploadDTO hisInfoUploadDTO) throws Exception;

}
