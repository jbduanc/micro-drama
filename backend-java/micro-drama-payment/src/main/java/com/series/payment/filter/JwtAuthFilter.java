package com.series.payment.filter;

import com.series.common.auth.GatewayAuthProperties;
import com.series.common.auth.GatewayPathSupport;
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

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            unauthorized(response);
            return;
        }
        String token = authHeader.substring(7).trim();
        boolean valid = gatewayAuthProperties.isKongMode()
                && GatewayPathSupport.isKongProxied(request)
                ? userJwtUtil.isSessionValid(token)
                : userJwtUtil.isValidate(token);
        if (!valid) {
            unauthorized(response);
            return;
        }
        String userId = userJwtUtil.getUserId(token);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList()));
        filterChain.doFilter(request, response);
    }

    private static void unauthorized(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":401,\"message\":\"令牌无效或已过期\"}");
    }
}
