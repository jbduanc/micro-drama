package com.series.common.auth;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

/**
 * 从网关注入的请求头解析登录身份（须同时存在 {@code X-Gateway-Request-Id}）。
 */
public final class GatewayAuthSupport {

    public static final String REFRESH_HEADER = "X-Refresh-Token";

    private GatewayAuthSupport() {
    }

    public static Optional<JwtPrincipal> principalFromGateway(HttpServletRequest request) {
        if (!GatewayPathSupport.isGatewayProxied(request)) {
            return Optional.empty();
        }
        String subject = trimHeader(request, GatewayAuthHeaders.SUBJECT);
        if (subject == null) {
            return Optional.empty();
        }
        AuthAudience audience = AuthAudience.fromClaim(trimHeader(request, GatewayAuthHeaders.AUDIENCE));
        if (audience == null) {
            audience = AuthAudience.USER;
        }
        return Optional.of(new JwtPrincipal(subject, audience));
    }

    public static String bearerToken(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            return null;
        }
        String token = auth.substring(7).trim();
        return token.isEmpty() ? null : token;
    }

    private static String trimHeader(HttpServletRequest request, String name) {
        String v = request.getHeader(name);
        if (v == null) {
            return null;
        }
        v = v.trim();
        return v.isEmpty() ? null : v;
    }
}
