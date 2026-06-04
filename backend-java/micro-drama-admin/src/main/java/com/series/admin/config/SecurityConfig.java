package com.series.admin.config;

import com.series.admin.filter.JwtAuthFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    // 修复：自动注入 Spring 托管的 Filter
    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .cors().and()
                .csrf().disable()
                // 无状态模式，不使用Session
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                // 权限配置
                .authorizeRequests()
                // 登录/OAuth 与 Actuator 放行（Kong 路由 strip_path 后路径与无前缀一致）
                .antMatchers(
                        "/oauth2/login/google",
                        "/oauth2/authorize-url",
                        "/admin-api/oauth2/login/google",
                        "/admin-api/oauth2/authorize-url",
                        "/actuator/**",
                        "/admin-api/actuator/**"
                ).permitAll()
                .anyRequest().authenticated();

        // 把JWT过滤器添加到 UsernamePasswordAuthenticationFilter 之前
        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
    }
}