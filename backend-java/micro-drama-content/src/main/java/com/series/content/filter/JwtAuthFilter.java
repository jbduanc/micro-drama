package com.series.content.filter;

import com.series.common.auth.GatewayAuthProperties;
import com.series.common.auth.GatewayAuthSupport;
import com.series.common.auth.GatewayPathSupport;
import com.series.common.auth.JwtPrincipal;
import com.series.common.auth.ValidatedToken;
import com.series.content.auth.ContentAuthPolicy;
import com.series.content.utils.JwtUtil;
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
    private JwtUtil jwtUtil;

    @Autowired
    private GatewayAuthProperties gatewayAuthProperties;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = GatewayPathSupport.normalizeServletPath(request);
        if (path.startsWith("/actuator/")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Optional<JwtPrincipal> principal = resolvePrincipal(request);
            if (!principal.isPresent()) {
                writeJson(response, HttpServletResponse.SC_UNAUTHORIZED, "未提供有效令牌");
                return;
            }

            JwtPrincipal p = principal.get();
            if (p.isUser() && ContentAuthPolicy.requiresAdmin(path)) {
                writeJson(response, HttpServletResponse.SC_FORBIDDEN, "需要管理端登录");
                return;
            }

            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(p, null, Collections.emptyList()));
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
                if (token != null) {
                    Optional<ValidatedToken> session = jwtUtil.validateSession(token);
                    if (!session.isPresent()) {
                        return Optional.empty();
                    }
                    JwtPrincipal p = fromKong.get();
                    if (!session.get().getSubject().equals(p.getSubject())
                            || session.get().getAudience() != p.getAudience()) {
                        return Optional.empty();
                    }
                }
            }
            return fromKong;
        }

        String token = GatewayAuthSupport.bearerToken(request);
        if (token == null) {
            return Optional.empty();
        }
        return jwtUtil.validate(token).map(vt -> new JwtPrincipal(vt.getSubject(), vt.getAudience()));
    }

    private static void writeJson(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":" + status + ",\"message\":\"" + message + "\"}");
    }
}
