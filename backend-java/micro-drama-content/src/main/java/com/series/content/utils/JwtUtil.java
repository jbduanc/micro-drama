package com.series.content.utils;

import com.series.common.auth.AuthAudience;
import com.series.common.auth.JwtTokenService;
import com.series.common.auth.ValidatedToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 内容服务：管理端（aud=admin）与小程序用户（aud=user）均可访问读接口。
 */
@Component
public class JwtUtil {

    private static final AuthAudience[] CONTENT_AUDIENCES = {
            AuthAudience.ADMIN,
            AuthAudience.USER
    };

    @Autowired
    private JwtTokenService jwtTokenService;

    public Optional<ValidatedToken> validate(String token) {
        return jwtTokenService.validateAny(token, CONTENT_AUDIENCES);
    }

    public Optional<ValidatedToken> validateSession(String token) {
        return jwtTokenService.sessionValidAny(token, CONTENT_AUDIENCES);
    }
}
