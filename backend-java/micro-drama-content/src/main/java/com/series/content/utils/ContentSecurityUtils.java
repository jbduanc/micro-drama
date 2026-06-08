package com.series.content.utils;

import com.series.common.auth.AuthAudience;
import com.series.common.auth.GatewayAuthContext;
import com.series.common.auth.JwtPrincipal;

import java.util.Optional;

/**
 * 内容服务获取当前登录人（网关注入或 JWT 过滤器写入 {@link JwtPrincipal}）。
 */
public final class ContentSecurityUtils {

    private ContentSecurityUtils() {
    }

    public static Optional<JwtPrincipal> getCurrentPrincipal() {
        return GatewayAuthContext.currentPrincipal();
    }

    public static Optional<String> getSubject() {
        return GatewayAuthContext.getSubject();
    }

    public static Optional<String> getUserId() {
        return GatewayAuthContext.getUserId();
    }

    public static Optional<String> getAdminEmail() {
        return GatewayAuthContext.getAdminEmail();
    }

    public static boolean isAdmin() {
        return GatewayAuthContext.isAdmin();
    }

    public static boolean isUser() {
        return GatewayAuthContext.isUser();
    }

    public static Optional<AuthAudience> getAudience() {
        return GatewayAuthContext.getAudience();
    }
}
