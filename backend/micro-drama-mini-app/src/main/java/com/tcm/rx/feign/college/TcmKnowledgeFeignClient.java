package com.tcm.rx.feign.college;

import com.tcm.common.config.security.FeignUserInfoConfig;
import com.tcm.common.entity.Result;
import com.tcm.rx.feign.college.vo.TcmKnowledgeVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 医疗机构 Feign 客户端
 */
@FeignClient(name = "tcm-sharing-admin", contextId = "tcmKnowledge-service",
        path = "/tcm-sharing-admin/tcmKnowledge", configuration = FeignUserInfoConfig.class)
public interface TcmKnowledgeFeignClient {
    /**
     * 查询中药知识列表
     */
    @GetMapping("/pageList")
    Result pageList(@SpringQueryMap TcmKnowledgeVO tcmKnowledgeVO);

    /**
     * 查询中药知识详情
     */
    @GetMapping(value = "/{id}")
    Result tcmKnowledgeById(@PathVariable("id") Long id);

}