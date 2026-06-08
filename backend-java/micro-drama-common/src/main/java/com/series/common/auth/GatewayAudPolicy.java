package com.series.common.auth;

/**
 * ForwardAuth 按路径划分的 aud 策略。
 */
public enum GatewayAudPolicy {
    ADMIN,
    USER,
    BOTH;

    public static GatewayAudPolicy forPath(String path) {
        if (path == null || path.isEmpty()) {
            return BOTH;
        }
        if (path.startsWith("/admin-api")) {
            return ADMIN;
        }
        if (path.startsWith("/user-api")) {
            return USER;
        }
        if (path.startsWith("/payment-api")) {
            return USER;
        }
        if (path.startsWith("/content-api") || path.startsWith("/video-api")) {
            return BOTH;
        }
        return BOTH;
    }

    public boolean allows(AuthAudience audience) {
        if (audience == null) {
            return false;
        }
        switch (this) {
            case ADMIN:
                return audience == AuthAudience.ADMIN;
            case USER:
                return audience == AuthAudience.USER;
            case BOTH:
                return audience == AuthAudience.ADMIN || audience == AuthAudience.USER;
            default:
                return false;
        }
    }

    public AuthAudience expectedAudience() {
        switch (this) {
            case ADMIN:
                return AuthAudience.ADMIN;
            case USER:
                return AuthAudience.USER;
            default:
                return null;
        }
    }
}
