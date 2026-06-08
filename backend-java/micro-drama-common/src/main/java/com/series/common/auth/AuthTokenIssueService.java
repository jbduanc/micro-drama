package com.series.common.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthTokenIssueService {

    @Autowired
    private JwtTokenService jwtTokenService;

    @Autowired
    private RefreshTokenService refreshTokenService;

    public AuthTokenPair issue(String subject, AuthAudience audience) {
        String access = jwtTokenService.generateToken(subject, audience);
        jwtTokenService.storeLoginToken(subject, audience, access);
        String refresh = refreshTokenService.issue(subject, audience);
        return new AuthTokenPair(access, refresh);
    }

    public Optional<AuthTokenPair> refresh(String refreshToken, AuthAudience audience) {
        Optional<String> subject = refreshTokenService.resolveSubject(refreshToken, audience);
        if (!subject.isPresent()) {
            return Optional.empty();
        }
        refreshTokenService.revoke(refreshToken, audience);
        return Optional.of(issue(subject.get(), audience));
    }

    public void revokeAll(String subject, AuthAudience audience, String accessToken) {
        jwtTokenService.revokeLoginToken(subject, audience);
        refreshTokenService.revokeForSubject(subject, audience);
        if (accessToken != null && !accessToken.isEmpty()) {
            jwtTokenService.blacklistToken(accessToken, audience, 15);
        }
    }
}
