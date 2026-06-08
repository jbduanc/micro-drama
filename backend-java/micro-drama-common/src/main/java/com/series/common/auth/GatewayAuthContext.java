package com.series.common.auth;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * 在 Controller / Service 中获取当前登录人（来自网关头或 JWT 过滤器写入的 {@link JwtPrincipal}）。
 */
public final class GatewayAuthContext {

    private GatewayAuthContext() {
    }

    public static Optional<JwtPrincipal> currentPrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            return Optional.empty();
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof JwtPrincipal) {
            return Optional.of((JwtPrincipal) principal);
        }
        if (principal instanceof String) {
            String subject = (String) principal;
            if (subject.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(new JwtPrincipal(subject, AuthAudience.USER));
        }
        return Optional.empty();
    }

    /** 登录主体：admin=邮箱，user=用户 UUID */
    public static Optional<String> getSubject() {
        return currentPrincipal().map(JwtPrincipal::getSubject);
    }

    public static Optional<AuthAudience> getAudience() {
        return currentPrincipal().map(JwtPrincipal::getAudience);
    }

    /** C 端用户 ID（aud=user 时的 subject） */
    public static Optional<String> getUserId() {
        return currentPrincipal()
                .filter(JwtPrincipal::isUser)
                .map(JwtPrincipal::getSubject);
    }

    /** 管理端邮箱（aud=admin 时的 subject） */
    public static Optional<String> getAdminEmail() {
        return currentPrincipal()
                .filter(JwtPrincipal::isAdmin)
                .map(JwtPrincipal::getSubject);
    }

    public static boolean isAdmin() {
        return currentPrincipal().map(JwtPrincipal::isAdmin).orElse(false);
    }

    public static boolean isUser() {
        return currentPrincipal().map(JwtPrincipal::isUser).orElse(false);
    }
}
