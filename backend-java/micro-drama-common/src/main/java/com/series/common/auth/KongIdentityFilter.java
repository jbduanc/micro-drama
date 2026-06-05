package com.series.common.auth;

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

/**
 * 登录服务：仅根据 Kong 注入头设置身份，不做 JWT 校验。
 */
@Component
public class KongIdentityFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            Optional<JwtPrincipal> principal = GatewayAuthSupport.principalFromKong(request);
            principal.ifPresent(p -> SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(p, null, Collections.emptyList())));
            filterChain.doFilter(request, response);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}
