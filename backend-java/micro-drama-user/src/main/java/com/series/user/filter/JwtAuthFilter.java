package com.series.user.filter;

import com.series.common.auth.GatewayAuthProperties;
import com.series.common.auth.GatewayPathSupport;
import com.series.user.utils.UserJwtUtil;
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

    private static final Set<String> PUBLIC_PREFIXES = new HashSet<>(Arrays.asList(
            "/auth/telegram",
            "/auth/dev/init",
            "/auth/web3/challenge",
            "/actuator"
    ));

    @Autowired
    private UserJwtUtil userJwtUtil;

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
            writeUnauthorized(response);
            return;
        }

        String token = authHeader.substring(7).trim();
        boolean valid = gatewayAuthProperties.isKongMode()
                && GatewayPathSupport.isKongProxied(request)
                ? userJwtUtil.isSessionValid(token)
                : userJwtUtil.isValidate(token);

        if (!valid) {
            writeUnauthorized(response);
            return;
        }

        String userId = userJwtUtil.getUserId(token);
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authToken);

        filterChain.doFilter(request, response);
    }

    private static boolean isPublicPath(String path) {
        for (String prefix : PUBLIC_PREFIXES) {
            if (path.equals(prefix) || path.startsWith(prefix + "/")) {
                return true;
            }
        }
        return false;
    }

    private static void writeUnauthorized(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":401,\"message\":\"令牌无效或已过期\"}");
    }
}
