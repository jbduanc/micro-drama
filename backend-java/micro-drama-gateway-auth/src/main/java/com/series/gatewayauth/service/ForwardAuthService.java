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
        String access = extractAccess(request);
        String refresh = extractRefresh(request);

        if (access != null) {
            Optional<ValidatedToken> validated = validateAccess(access, policy);
            if (validated.isPresent()) {
                return ForwardAuthResult.ok(validated.get(), access, null, false);
            }
        }

        if (refresh != null) {
            Optional<AuthTokenPair> refreshed = refreshAccess(refresh, policy);
            if (refreshed.isPresent()) {
                AuthTokenPair pair = refreshed.get();
                AuthAudience aud = jwtTokenService.getAudience(pair.getAccessToken());
                if (policy.allows(aud)) {
                    ValidatedToken token = new ValidatedToken(jwtTokenService.getSubject(pair.getAccessToken()), aud);
                    return ForwardAuthResult.ok(token, pair.getAccessToken(), pair, true);
                }
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

    private String extractAccess(HttpServletRequest request) {
        String bearer = GatewayAuthSupport.bearerToken(request);
        if (bearer != null) {
            return bearer;
        }
        return authCookieSupport.readCookie(request, AuthCookieNames.ACCESS);
    }

    private String extractRefresh(HttpServletRequest request) {
        String header = request.getHeader(GatewayAuthSupport.REFRESH_HEADER);
        if (header != null && !header.trim().isEmpty()) {
            return header.trim();
        }
        return authCookieSupport.readCookie(request, AuthCookieNames.REFRESH);
    }
}
