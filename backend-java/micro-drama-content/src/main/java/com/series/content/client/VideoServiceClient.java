package com.series.content.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 调用 micro-drama-video 删除视频资产（剧集删除时级联清理）。
 */
@Component
public class VideoServiceClient {

    private static final Logger log = LoggerFactory.getLogger(VideoServiceClient.class);

    @Value("${video-service.base-url:http://127.0.0.1:8080}")
    private String baseUrl;

    @Resource
    private RestTemplate restTemplate;

    public void deleteVideoAsset(String videoId, String fileKey) {
        if (!StringUtils.hasText(videoId)) {
            return;
        }
        String url = baseUrl.replaceAll("/+$", "") + "/v1/video/delete";
        Map<String, Object> item = new HashMap<>();
        item.put("videoId", videoId.trim());
        if (StringUtils.hasText(fileKey)) {
            item.put("fileKey", fileKey.trim());
        }
        Map<String, Object> body = new HashMap<>();
        body.put("items", Collections.singletonList(item));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            VideoApiResult<?> res = restTemplate.postForObject(url, entity, VideoApiResult.class);
            if (res == null || res.getCode() != 0) {
                String msg = res != null ? res.getMsg() : "empty response";
                throw new IllegalStateException("video delete failed: " + msg);
            }
        } catch (RestClientException ex) {
            log.error("call video delete failed, videoId={}", videoId, ex);
            throw new IllegalStateException("video delete request failed: " + ex.getMessage(), ex);
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class VideoApiResult<T> {
        private int code;
        private String msg;
        private T data;
    }
}
