package com.series.common.auth;

/**
 * JWT 受众，与 Kong 路由/插件按服务划分鉴权一致。
 */
public enum AuthAudience {
    ADMIN("admin"),
    USER("user");

    private final String value;

    AuthAudience(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static AuthAudience fromClaim(String aud) {
        if (aud == null) {
            return null;
        }
        for (AuthAudience a : values()) {
            if (a.value.equals(aud)) {
                return a;
            }
        }
        return null;
    }
}
