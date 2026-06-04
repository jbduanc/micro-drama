package com.series.admin.utils;

import com.series.common.auth.AuthAudience;
import com.series.common.auth.JwtTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 管理端 JWT，受众 {@link AuthAudience#ADMIN}；签名校验可由 Kong 网关承担（{@code auth.gateway.mode=kong}）。
 */
@Component
public class JwtUtil {

    private static final AuthAudience AUDIENCE = AuthAudience.ADMIN;

    @Autowired
    private JwtTokenService jwtTokenService;

    public String generateToken(String email) {
        return jwtTokenService.generateToken(email, AUDIENCE);
    }

    public String getEmail(String token) {
        return jwtTokenService.getSubject(token);
    }

    public boolean isValidate(String token) {
        return jwtTokenService.isValidate(token, AUDIENCE);
    }

    public boolean isSessionValid(String token) {
        return jwtTokenService.isSessionValid(token, AUDIENCE);
    }

    public void storeLoginToken(String email, String token) {
        jwtTokenService.storeLoginToken(email, AUDIENCE, token);
    }

    public void revokeLoginToken(String email) {
        jwtTokenService.revokeLoginToken(email, AUDIENCE);
    }

    public void blacklistToken(String token) {
        jwtTokenService.blacklistToken(token, AUDIENCE, 15);
    }
}
