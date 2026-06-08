package com.series.gatewayauth.web;

import com.series.common.auth.AuthCookieSupport;
import com.series.common.auth.GatewayAudPolicy;
import com.series.common.auth.GatewayAuthHeaders;
import com.series.common.auth.AuthAudience;
import com.series.gatewayauth.service.ForwardAuthResult;
import com.series.gatewayauth.service.ForwardAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
public class ForwardAuthController {

    @Autowired
    private ForwardAuthService forwardAuthService;

    @Autowired
    private AuthCookieSupport authCookieSupport;

    /**
     * Traefik forwardAuth 入口：验签 + aud + 静默 refresh。
     */
    @GetMapping("/auth/verify")
    public ResponseEntity<Void> verify(HttpServletRequest request) {
        String uri = firstNonEmpty(
                request.getHeader("X-Forwarded-Uri"),
                request.getHeader("X-Original-Url"),
                request.getRequestURI()
        );
        GatewayAudPolicy policy = GatewayAudPolicy.forPath(uri);
        ForwardAuthResult result = forwardAuthService.authenticate(request, policy);
        if (!result.isOk()) {
            return ResponseEntity.status(result.getStatus()).build();
        }
        return buildAuthResponse(result);
    }

    /**
     * 客户端主动 refresh（admin / 小程序 fallback；网关静默 refresh 失败时使用）。
     */
    @PostMapping("/auth/refresh")
    public ResponseEntity<Map<String, String>> refresh(HttpServletRequest request,
                                                       @RequestBody(required = false) Map<String, String> body) {
        String audience = body != null ? body.get("audience") : null;
        AuthAudience aud = "admin".equalsIgnoreCase(audience)
                ? AuthAudience.ADMIN
                : AuthAudience.USER;

        String refresh = authCookieSupport.readCookie(request, com.series.common.auth.AuthCookieNames.REFRESH);
        if (refresh == null && body != null) {
            refresh = body.get("refreshToken");
        }
        if (refresh == null) {
            refresh = request.getHeader(com.series.common.auth.GatewayAuthSupport.REFRESH_HEADER);
        }
        if (refresh == null || refresh.isEmpty()) {
            return ResponseEntity.status(401).build();
        }

        return forwardAuthService.refreshForAudience(refresh, aud)
                .map(pair -> {
                    HttpHeaders headers = new HttpHeaders();
                    authCookieSupport.buildSetCookieHeaders(pair).forEach(v -> headers.add(HttpHeaders.SET_COOKIE, v));
                    Map<String, String> payload = new HashMap<>();
                    payload.put("accessToken", pair.getAccessToken());
                    payload.put("refreshToken", pair.getRefreshToken());
                    return ResponseEntity.ok().headers(headers).body(payload);
                })
                .orElseGet(() -> ResponseEntity.status(401).build());
    }

    private ResponseEntity<Void> buildAuthResponse(ForwardAuthResult result) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(GatewayAuthHeaders.GATEWAY_REQUEST_ID, UUID.randomUUID().toString());
        headers.set(GatewayAuthHeaders.SUBJECT, result.getSubject());
        headers.set(GatewayAuthHeaders.AUDIENCE, result.getAudience().getValue());
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + result.getAccessToken());
        if (result.isRefreshed()) {
            headers.set(GatewayAuthHeaders.ACCESS_REFRESHED, "true");
            authCookieSupport.buildSetCookieHeaders(result.getRefreshedPair())
                    .forEach(v -> headers.add(HttpHeaders.SET_COOKIE, v));
        }
        return ResponseEntity.ok().headers(headers).build();
    }

    private static String firstNonEmpty(String... values) {
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }
        return "/";
    }
}
