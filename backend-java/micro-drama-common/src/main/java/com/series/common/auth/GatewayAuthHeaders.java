package com.series.common.auth;

/**
 * Traefik ForwardAuth（gateway-auth）校验 JWT 后向下游注入的身份头。
 * 勿由客户端伪造；仅信任带 {@code X-Gateway-Request-Id} 的请求。
 */
public final class GatewayAuthHeaders {

    public static final String SUBJECT = "X-Auth-Subject";
    public static final String AUDIENCE = "X-Auth-Audience";
    public static final String GATEWAY_REQUEST_ID = "X-Gateway-Request-Id";
    public static final String ACCESS_REFRESHED = "X-Access-Token-Refreshed";

    private GatewayAuthHeaders() {
    }
}
