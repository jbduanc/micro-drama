package com.series.common.auth;

/**
 * 网关 ForwardAuth 与登录服务共用的 httpOnly Cookie 名（domain 由配置决定）。
 */
public final class AuthCookieNames {

    public static final String ACCESS = "md_access";
    public static final String REFRESH = "md_refresh";

    private AuthCookieNames() {
    }
}
