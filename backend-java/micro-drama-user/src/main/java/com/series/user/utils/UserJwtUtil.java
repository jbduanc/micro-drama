package com.series.user.utils;

import com.series.common.auth.AuthAudience;
import com.series.common.auth.JwtTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserJwtUtil {

    private static final AuthAudience AUDIENCE = AuthAudience.USER;

    @Autowired
    private JwtTokenService jwtTokenService;

    public String generateToken(String userId) {
        return jwtTokenService.generateToken(userId, AUDIENCE);
    }

    public String getUserId(String token) {
        return jwtTokenService.getSubject(token);
    }

    public boolean isValidate(String token) {
        return jwtTokenService.isValidate(token, AUDIENCE);
    }

    public boolean isSessionValid(String token) {
        return jwtTokenService.isSessionValid(token, AUDIENCE);
    }

    public void storeLoginToken(String userId, String token) {
        jwtTokenService.storeLoginToken(userId, AUDIENCE, token);
    }

    public void revokeLoginToken(String userId) {
        jwtTokenService.revokeLoginToken(userId, AUDIENCE);
    }

    public void blacklistToken(String token) {
        jwtTokenService.blacklistToken(token, AUDIENCE, 15);
    }
}
