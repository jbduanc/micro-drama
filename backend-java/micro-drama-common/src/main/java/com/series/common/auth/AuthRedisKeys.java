package com.series.common.auth;

/**
 * 登录态与黑名单 Redis 键（按受众隔离，避免 admin 与 mini-app 用户 token 混用）。
 */
public final class AuthRedisKeys {

    private AuthRedisKeys() {
    }

    public static String loginToken(AuthAudience audience, String subject) {
        return audience.getValue() + ":login:token:" + subject;
    }

    public static String blacklist(AuthAudience audience, String token) {
        return audience.getValue() + ":blacklist:" + token;
    }
}
