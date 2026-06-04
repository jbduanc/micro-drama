package com.series.content.filter;

import com.series.common.auth.GatewayAuthProperties;
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

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            writeJson(response, HttpServletResponse.SC_UNAUTHORIZED, "未提供有效令牌");
            return;
        }

        String token = authHeader.substring(7).trim();
        Optional<ValidatedToken> validated = gatewayAuthProperties.isKongMode()
                && GatewayPathSupport.isKongProxied(request)
                ? jwtUtil.validateSession(token)
                : jwtUtil.validate(token);

        if (!validated.isPresent()) {
            writeJson(response, HttpServletResponse.SC_UNAUTHORIZED, "令牌无效或已过期");
            return;
        }

        ValidatedToken vt = validated.get();
        if (vt.getAudience() == com.series.common.auth.AuthAudience.USER
                && ContentAuthPolicy.requiresAdmin(path)) {
            writeJson(response, HttpServletResponse.SC_FORBIDDEN, "需要管理端登录");
            return;
        }

        JwtPrincipal principal = new JwtPrincipal(vt.getSubject(), vt.getAudience());
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authToken);

        filterChain.doFilter(request, response);
    }

    private static void writeJson(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":" + status + ",\"message\":\"" + message + "\"}");
    }
}
