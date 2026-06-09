package com.series.gatewayauth.service;

import com.series.common.auth.AuthAudience;
import com.series.common.auth.AuthCookieNames;
import com.series.common.auth.AuthCookieSupport;
import com.series.common.auth.AuthTokenIssueService;
import com.series.common.auth.AuthTokenPair;
import com.series.common.auth.GatewayAudPolicy;
import com.series.common.auth.GatewayAuthSupport;
import com.series.common.auth.JwtTokenService;
import com.series.common.auth.ValidatedToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@Service
public class ForwardAuthService {

    @Autowired
    private JwtTokenService jwtTokenService;

    @Autowired
    private AuthTokenIssueService authTokenIssueService;

    @Autowired
    private AuthCookieSupport authCookieSupport;

    public ForwardAuthResult authenticate(HttpServletRequest request, GatewayAudPolicy policy) {
        String bearer = GatewayAuthSupport.bearerToken(request);
        String cookieAccess = authCookieSupport.readCookie(request, AuthCookieNames.ACCESS);

        // Bearer（localStorage）可能滞后；Cookie 在网关静默 refresh 后会更新，需依次尝试
        if (bearer != null) {
            Optional<ValidatedToken> validated = validateAccess(bearer, policy);
            if (validated.isPresent()) {
                return ForwardAuthResult.ok(validated.get(), bearer, null, false);
            }
        }
        if (cookieAccess != null && !cookieAccess.equals(bearer)) {
            Optional<ValidatedToken> validated = validateAccess(cookieAccess, policy);
            if (validated.isPresent()) {
                return ForwardAuthResult.ok(validated.get(), cookieAccess, null, false);
            }
        }

        // 静默 refresh：优先 Cookie（httpOnly 权威），再尝试 X-Refresh-Token（小程序等 fallback）
        Optional<AuthTokenPair> refreshed = tryRefresh(request, policy);
        if (refreshed.isPresent()) {
            AuthTokenPair pair = refreshed.get();
            AuthAudience aud = jwtTokenService.getAudience(pair.getAccessToken());
            if (policy.allows(aud)) {
                ValidatedToken token = new ValidatedToken(jwtTokenService.getSubject(pair.getAccessToken()), aud);
                return ForwardAuthResult.ok(token, pair.getAccessToken(), pair, true);
            }
        }

        return ForwardAuthResult.unauthorized();
    }

    public java.util.Optional<AuthTokenPair> refreshForAudience(String refresh, AuthAudience audience) {
        return authTokenIssueService.refresh(refresh, audience);
    }

    private Optional<ValidatedToken> validateAccess(String access, GatewayAudPolicy policy) {
        AuthAudience expected = policy.expectedAudience();
        if (expected != null) {
            return jwtTokenService.validateAny(access, expected);
        }
        return jwtTokenService.validateAny(access, AuthAudience.ADMIN, AuthAudience.USER)
                .filter(v -> policy.allows(v.getAudience()));
    }

    private Optional<AuthTokenPair> refreshAccess(String refresh, GatewayAudPolicy policy) {
        AuthAudience expected = policy.expectedAudience();
        if (expected != null) {
            return authTokenIssueService.refresh(refresh, expected);
        }
        Optional<AuthTokenPair> admin = authTokenIssueService.refresh(refresh, AuthAudience.ADMIN);
        if (admin.isPresent()) {
            return admin;
        }
        return authTokenIssueService.refresh(refresh, AuthAudience.USER);
    }

    private Optional<AuthTokenPair> tryRefresh(HttpServletRequest request, GatewayAudPolicy policy) {
        String cookieRefresh = authCookieSupport.readCookie(request, AuthCookieNames.REFRESH);
        if (cookieRefresh != null && !cookieRefresh.isEmpty()) {
            Optional<AuthTokenPair> fromCookie = refreshAccess(cookieRefresh, policy);
            if (fromCookie.isPresent()) {
                return fromCookie;
            }
        }
        String headerRefresh = request.getHeader(GatewayAuthSupport.REFRESH_HEADER);
        if (headerRefresh != null && !headerRefresh.trim().isEmpty()) {
            return refreshAccess(headerRefresh.trim(), policy);
        }
        return Optional.empty();
    }
}
