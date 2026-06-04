package com.series.admin.filter;

import com.series.admin.utils.JwtUtil;
import com.series.common.auth.GatewayAuthProperties;
import com.series.common.auth.GatewayPathSupport;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Set<String> PUBLIC_PATHS = new HashSet<>(Arrays.asList(
            "/oauth2/authorize-url",
            "/oauth2/login/google",
            "/actuator/health",
            "/actuator/info"
    ));

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private GatewayAuthProperties gatewayAuthProperties;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = GatewayPathSupport.normalizeServletPath(request);
        if (isPublicPath(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            writeUnauthorized(response, "未提供有效令牌");
            return;
        }

        String token = authHeader.substring(7).trim();
        boolean valid = gatewayAuthProperties.isKongMode()
                && GatewayPathSupport.isKongProxied(request)
                ? jwtUtil.isSessionValid(token)
                : jwtUtil.isValidate(token);

        if (!valid) {
            writeUnauthorized(response, "令牌无效或已过期");
            return;
        }

        String email = jwtUtil.getEmail(token);
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(email, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authToken);

        filterChain.doFilter(request, response);
    }

    private static boolean isPublicPath(String path) {
        for (String pub : PUBLIC_PATHS) {
            if (path.equals(pub) || path.startsWith(pub + "/")) {
                return true;
            }
        }
        return path.startsWith("/actuator/");
    }

    private static void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":401,\"message\":\"" + message + "\"}");
    }
}
