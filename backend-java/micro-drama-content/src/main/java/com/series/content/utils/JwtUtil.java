package com.series.content.utils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 与 micro-drama-admin 共用同一套 Redis 键与签发规则，便于管理端登录态直接访问内容服务。
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-expire}")
    private Long accessExpire;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public String generateToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setExpiration(new Date(System.currentTimeMillis() + accessExpire))
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }

    public String getEmail(String token) {
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean isValidate(String token) {
        try {
            if (Boolean.TRUE.equals(redisTemplate.hasKey("admin:blacklist:" + token))) {
                return false;
            }

            String email = getEmail(token);
            String redisKey = "admin:login:token:" + email;
            String cachedToken = redisTemplate.opsForValue().get(redisKey);
            if (cachedToken == null || !cachedToken.equals(token)) {
                return false;
            }

            Jwts.parser().setSigningKey(secret).parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
