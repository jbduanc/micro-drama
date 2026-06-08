package com.series.common.auth;

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

/**
 * 登录服务：gateway 模式下信任网关注入头，并校验 Redis 会话/黑名单（注销、单点登录）。
 * 本地直连（无 X-Gateway-Request-Id）时不设置身份，由业务自行处理公开路径。
 */
@Component
public class GatewayIdentityFilter extends OncePerRequestFilter {

    @Autowired
    private GatewayAuthProperties gatewayAuthProperties;

    @Autowired
    private JwtTokenService jwtTokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            Optional<JwtPrincipal> principal = GatewayAuthSupport.principalFromGateway(request);
            if (principal.isPresent() && gatewayAuthProperties.isGatewayMode()) {
                String token = GatewayAuthSupport.bearerToken(request);
                JwtPrincipal p = principal.get();
                if (token == null || !jwtTokenService.isSessionValid(token, p.getSubject(), p.getAudience())) {
                    filterChain.doFilter(request, response);
                    return;
                }
            }
            principal.ifPresent(p -> SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(p, null, Collections.emptyList())));
            filterChain.doFilter(request, response);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}
