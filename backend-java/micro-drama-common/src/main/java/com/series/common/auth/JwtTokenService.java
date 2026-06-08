package com.series.common.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 统一 JWT 签发与校验（HS256），供 admin / user 等服务复用。
 */
@Component
public class JwtTokenService {

    public static final String CLAIM_AUD = "aud";

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-expire}")
    private Long accessExpire;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public String generateToken(String subject, AuthAudience audience) {
        return Jwts.builder()
                .setSubject(subject)
                .claim(CLAIM_AUD, audience.getValue())
                .setExpiration(new Date(System.currentTimeMillis() + accessExpire))
                // jjwt 0.9.1 String overload base64-decodes; use UTF-8 bytes to match Go
                .signWith(SignatureAlgorithm.HS256, secret.getBytes(StandardCharsets.UTF_8))
                .compact();
    }

    public String getSubject(String token) {
        return parseClaims(token).getSubject();
    }

    public AuthAudience getAudience(String token) {
        Claims claims = parseClaims(token);
        return AuthAudience.fromClaim(claims.get(CLAIM_AUD, String.class));
    }

    public void storeLoginToken(String subject, AuthAudience audience, String token) {
        String key = AuthRedisKeys.loginToken(audience, subject);
        redisTemplate.opsForValue().set(key, token, accessExpire, TimeUnit.MILLISECONDS);
    }

    public void revokeLoginToken(String subject, AuthAudience audience) {
        if (subject != null) {
            redisTemplate.delete(AuthRedisKeys.loginToken(audience, subject));
        }
    }

    public void blacklistToken(String token, AuthAudience audience, long minutes) {
        redisTemplate.opsForValue().set(
                AuthRedisKeys.blacklist(audience, token),
                "1",
                minutes,
                TimeUnit.MINUTES
        );
    }

    /**
     * 校验 token 是否属于允许的任一受众（供 content / video 等共享资源服务使用）。
     */
    public Optional<ValidatedToken> validateAny(String token, AuthAudience... allowedAudiences) {
        if (allowedAudiences == null || allowedAudiences.length == 0) {
            return Optional.empty();
        }
        Set<AuthAudience> allowed = new HashSet<>(Arrays.asList(allowedAudiences));
        try {
            Claims claims = parseClaims(token);
            AuthAudience aud = AuthAudience.fromClaim(claims.get(CLAIM_AUD, String.class));
            if (aud == null) {
                aud = AuthAudience.ADMIN;
            }
            if (!allowed.contains(aud)) {
                return Optional.empty();
            }
            if (!passesRedisChecks(token, aud, claims.getSubject())) {
                return Optional.empty();
            }
            Jwts.parser().setSigningKey(secret.getBytes(StandardCharsets.UTF_8)).parseClaimsJws(token);
            return Optional.of(new ValidatedToken(claims.getSubject(), aud));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * gateway 模式：信任 ForwardAuth 已验签，仅用网关注入身份做 Redis 会话/黑名单校验。
     */
    public Optional<ValidatedToken> sessionValidAny(String token,
                                                  String subject,
                                                  AuthAudience audience,
                                                  AuthAudience... allowedAudiences) {
        if (token == null || token.isEmpty() || subject == null || subject.isEmpty() || audience == null) {
            return Optional.empty();
        }
        if (allowedAudiences == null || allowedAudiences.length == 0) {
            return Optional.empty();
        }
        Set<AuthAudience> allowed = new HashSet<>(Arrays.asList(allowedAudiences));
        if (!allowed.contains(audience)) {
            return Optional.empty();
        }
        if (!passesRedisChecks(token, audience, subject)) {
            return Optional.empty();
        }
        return Optional.of(new ValidatedToken(subject, audience));
    }

    /**
     * 完整校验：黑名单 + Redis 单点登录 + 签名/过期 + aud（无 aud 的旧 token 视为 admin，兼容迁移）。
     */
    public boolean isValidate(String token, AuthAudience expectedAudience) {
        return validateAny(token, expectedAudience).isPresent();
    }

    /**
     * ForwardAuth 已在网关校验 JWT 签名时，应用仅校验 Redis 会话与黑名单。
     */
    public boolean isSessionValid(String token, String subject, AuthAudience expectedAudience) {
        return sessionValidAny(token, subject, expectedAudience, expectedAudience).isPresent();
    }

    private boolean passesRedisChecks(String token, AuthAudience audience, String subject) {
        if (Boolean.TRUE.equals(redisTemplate.hasKey(AuthRedisKeys.blacklist(audience, token)))) {
            return false;
        }
        String cached = redisTemplate.opsForValue().get(AuthRedisKeys.loginToken(audience, subject));
        return cached != null && cached.equals(token);
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .setSigningKey(secret.getBytes(StandardCharsets.UTF_8))
                .parseClaimsJws(token)
                .getBody();
    }
}
