package com.series.common.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 网关鉴权：生产由 Kong 校验 JWT；content 等业务服务在 kong 模式下仍可做 Redis 会话/黑名单校验。
 * admin / user 登录服务不读取本配置。
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
