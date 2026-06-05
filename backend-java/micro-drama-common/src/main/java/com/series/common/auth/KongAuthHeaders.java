package com.series.common.auth;

/**
 * Kong 网关在校验 JWT 后向下游注入的身份头（勿由客户端伪造；仅信任带 {@code X-Kong-Request-Id} 的请求）。
 */
public final class KongAuthHeaders {

    public static final String SUBJECT = "X-Auth-Subject";
    public static final String AUDIENCE = "X-Auth-Audience";

    private KongAuthHeaders() {
    }
}
