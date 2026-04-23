package com.tcm.rx.feign.college;

import com.tcm.common.config.security.FeignUserInfoConfig;
import com.tcm.common.entity.Result;
import com.tcm.rx.feign.college.vo.VideoCourseVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 医疗机构 Feign 客户端
 */
@FeignClient(name = "tcm-sharing-admin", contextId = "videoCourse-service",
        path = "/tcm-sharing-admin/videoCourse", configuration = FeignUserInfoConfig.class)
public interface VideoCourseFeignClient {
    /**
     * 查询医疗机构
     */
    @GetMapping("/pageList")
    Result pageList(@SpringQueryMap VideoCourseVO videoCourseVO);

}