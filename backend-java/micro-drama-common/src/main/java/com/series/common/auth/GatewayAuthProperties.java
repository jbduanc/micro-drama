package com.series.common.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 网关鉴权：生产由 Traefik ForwardAuth（gateway-auth）校验 JWT；
 * 业务服务在 gateway 模式下仍做 Redis 会话/黑名单校验。
 */
@Component
@ConfigurationProperties(prefix = "auth.gateway")
public class GatewayAuthProperties {

    /**
     * off — 应用内完整 JWT 校验（本地直连）；
     * gateway — 请求经 Traefik ForwardAuth（带 X-Gateway-Request-Id）时跳过签名校验，仍校验 Redis 会话。
     */
    private String mode = "off";

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public boolean isGatewayMode() {
        return "gateway".equalsIgnoreCase(mode);
    }
}
