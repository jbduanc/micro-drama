package com.series.payment.utils;

import com.series.common.auth.AuthAudience;
import com.series.common.auth.JwtTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserJwtUtil {

    private static final AuthAudience AUDIENCE = AuthAudience.USER;

    @Autowired
    private JwtTokenService jwtTokenService;

    public boolean isValidate(String token) {
        return jwtTokenService.isValidate(token, AUDIENCE);
    }

    public boolean isSessionValid(String token) {
        return jwtTokenService.isSessionValid(token, AUDIENCE);
    }

    public String getUserId(String token) {
        return jwtTokenService.getSubject(token);
    }
}
