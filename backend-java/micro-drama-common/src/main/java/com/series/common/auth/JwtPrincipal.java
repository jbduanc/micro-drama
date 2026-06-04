package com.series.common.auth;

import java.security.Principal;

/**
 * SecurityContext 主体：subject + 受众（admin 邮箱或 C 端 userId）。
 */
public class JwtPrincipal implements Principal {

    private final String subject;
    private final AuthAudience audience;

    public JwtPrincipal(String subject, AuthAudience audience) {
        this.subject = subject;
        this.audience = audience;
    }

    @Override
    public String getName() {
        return subject;
    }

    public String getSubject() {
        return subject;
    }

    public AuthAudience getAudience() {
        return audience;
    }

    public boolean isAdmin() {
        return audience == AuthAudience.ADMIN;
    }

    public boolean isUser() {
        return audience == AuthAudience.USER;
    }
}
