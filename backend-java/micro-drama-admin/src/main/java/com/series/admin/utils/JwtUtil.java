package com.series.admin.utils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-expire}")
    private Long accessExpire;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    // 生成Token
    public String generateToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setExpiration(new Date(System.currentTimeMillis() + accessExpire))
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }

    // 解析邮箱
    public String getEmail(String token) {
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // 校验Token是否有效（未拉黑 + 登录态存在 + 签名/过期正常）
    public boolean isValidate(String token) {
        try {
            // 1. 检查是否在黑名单
            if (Boolean.TRUE.equals(redisTemplate.hasKey("admin:blacklist:" + token))) {
                return false;
            }

            // 2. 解析邮箱，检查Redis中的登录态是否存在且匹配
            String email = getEmail(token);
            String redisKey = "admin:login:token:" + email;
            String cachedToken = redisTemplate.opsForValue().get(redisKey);
            if (cachedToken == null || !cachedToken.equals(token)) {
                return false;
            }

            // 3. 检查签名和过期时间
            Jwts.parser().setSigningKey(secret).parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            // 解析失败/异常均视为无效
            return false;
        }
    }
}