package com.tcm.rx.feign.manage;

import com.tcm.common.config.security.FeignUserInfoConfig;
import com.tcm.common.entity.Result;
import com.tcm.rx.feign.manage.vo.DiagnosisQueryVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 医疗机构 Feign 客户端
 */
@FeignClient(name = "tcm-sharing-admin", contextId = "diagnosis-service",
        path = "/tcm-sharing-admin/diagnosis", configuration = FeignUserInfoConfig.class)
public interface DiagnosisFeignClient {
    /**
     * 查询西医诊断
     */
    @PostMapping("/pageList")
    Result pageList(@RequestBody DiagnosisQueryVO diagnosisQueryVO);

}