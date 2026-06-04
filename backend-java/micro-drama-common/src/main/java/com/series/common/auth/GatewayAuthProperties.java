package com.series.common.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 网关鉴权：生产由 Kong JWT 插件校验签名；应用侧保留 Redis 会话/黑名单校验。
 */
@Component
@ConfigurationProperties(prefix = "auth.gateway")
public class GatewayAuthProperties {

    /**
     * off — 应用内完整 JWT 校验（本地直连）；
     * kong — 请求经 Kong（带 X-Kong-Request-Id）时跳过签名校验，仍校验 Redis 会话。
     */
    private String mode = "off";

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public boolean isKongMode() {
        return "kong".equalsIgnoreCase(mode);
    }
}
