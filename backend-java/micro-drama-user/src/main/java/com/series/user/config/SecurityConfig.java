package com.series.user.config;

import com.series.common.auth.GatewayIdentityFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * 用户服务负责 Telegram/开发登录与注销；Traefik ForwardAuth 校验 JWT，本服务校验 Redis 会话/黑名单。
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private GatewayIdentityFilter gatewayIdentityFilter;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .cors().and()
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                .anyRequest().permitAll();

        http.addFilterBefore(gatewayIdentityFilter, UsernamePasswordAuthenticationFilter.class);
    }
}
