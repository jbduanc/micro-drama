package com.series.admin.filter;

import com.series.admin.utils.JwtUtil;
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
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 1. 公开 OAuth 接口：与 SecurityConfig.permitAll 一致（servletPath 与 Spring Security 匹配口径一致）
        String servletPath = request.getServletPath() != null ? request.getServletPath() : "";
        String pathInfo = request.getPathInfo() != null ? request.getPathInfo() : "";
        String path = servletPath + pathInfo;
        if (path.startsWith("/admin-api/")) {
            path = path.substring("/admin-api".length());
            if (!path.startsWith("/")) {
                path = "/" + path;
            }
        }
        if (path.startsWith("/oauth2/authorize-url") || path.startsWith("/oauth2/login/google")) {
            // 对于公开接口，直接放行，不校验 token
            filterChain.doFilter(request, response);
            return;
        }

        // 2. 对于需要认证的接口，执行原有的 JWT 校验逻辑
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // 注意：这里不应该直接设置状态码并返回，而应该抛出异常或交由后续处理
            // 但由于是 Filter 层，简单起见可以返回 401，但更规范的做法是交给 Spring Security 的异常处理器
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"未提供有效令牌\"}");
            return;
        }

        String token = authHeader.replace("Bearer ", "");
        if (!jwtUtil.isValidate(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"令牌无效或已过期\"}");
            return;
        }

        // 3. 令牌有效，将用户信息存入 SecurityContext
        String email = jwtUtil.getEmail(token);
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(email, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authToken);

        // 4. 继续执行后续过滤器
        filterChain.doFilter(request, response);
    }
}
