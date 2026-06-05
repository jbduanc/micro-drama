package com.series.payment.filter;

import com.series.common.auth.GatewayAuthProperties;
import com.series.common.auth.GatewayAuthSupport;
import com.series.common.auth.GatewayPathSupport;
import com.series.common.auth.JwtPrincipal;
import com.series.payment.utils.UserJwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private UserJwtUtil userJwtUtil;

    @Autowired
    private GatewayAuthProperties gatewayAuthProperties;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = GatewayPathSupport.normalizeServletPath(request);
        if (path.equals("/healthz") || path.startsWith("/actuator/")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Optional<JwtPrincipal> principal = resolvePrincipal(request);
            if (!principal.isPresent() || !principal.get().isUser()) {
                unauthorized(response);
                return;
            }
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(principal.get(), null, Collections.emptyList()));
            filterChain.doFilter(request, response);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    private Optional<JwtPrincipal> resolvePrincipal(HttpServletRequest request) {
        if (GatewayPathSupport.isKongProxied(request)) {
            Optional<JwtPrincipal> fromKong = GatewayAuthSupport.principalFromKong(request);
            if (!fromKong.isPresent()) {
                return Optional.empty();
            }
            if (gatewayAuthProperties.isKongMode()) {
                String token = GatewayAuthSupport.bearerToken(request);
                JwtPrincipal kongPrincipal = fromKong.get();
                if (token == null || !userJwtUtil.isSessionValid(token, kongPrincipal.getSubject())) {
                    return Optional.empty();
                }
            }
            return fromKong;
        }
        String token = GatewayAuthSupport.bearerToken(request);
        if (token == null || !userJwtUtil.isValidate(token)) {
            return Optional.empty();
        }
        return Optional.of(new JwtPrincipal(userJwtUtil.getUserId(token), com.series.common.auth.AuthAudience.USER));
    }

    private static void unauthorized(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":401,\"message\":\"令牌无效或已过期\"}");
    }
}
