package com.series.common.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 不透明 refresh token（Redis 存储），供 ForwardAuth 与登录服务共用。
 */
@Service
public class RefreshTokenService {

    @Value("${jwt.refresh-expire:2592000000}")
    private Long refreshExpire;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public String issue(String subject, AuthAudience audience) {
        revokeForSubject(subject, audience);
        String refreshId = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set(
                AuthRedisKeys.refreshToken(audience, refreshId),
                subject,
                refreshExpire,
                TimeUnit.MILLISECONDS
        );
        redisTemplate.opsForValue().set(
                AuthRedisKeys.refreshSubject(audience, subject),
                refreshId,
                refreshExpire,
                TimeUnit.MILLISECONDS
        );
        return refreshId;
    }

    public Optional<String> resolveSubject(String refreshId, AuthAudience audience) {
        if (refreshId == null || refreshId.isEmpty()) {
            return Optional.empty();
        }
        String subject = redisTemplate.opsForValue().get(AuthRedisKeys.refreshToken(audience, refreshId));
        return subject == null || subject.isEmpty() ? Optional.empty() : Optional.of(subject);
    }

    public void revoke(String refreshId, AuthAudience audience) {
        if (refreshId == null || refreshId.isEmpty()) {
            return;
        }
        String subject = redisTemplate.opsForValue().get(AuthRedisKeys.refreshToken(audience, refreshId));
        redisTemplate.delete(AuthRedisKeys.refreshToken(audience, refreshId));
        if (subject != null) {
            String current = redisTemplate.opsForValue().get(AuthRedisKeys.refreshSubject(audience, subject));
            if (refreshId.equals(current)) {
                redisTemplate.delete(AuthRedisKeys.refreshSubject(audience, subject));
            }
        }
    }

    public void revokeForSubject(String subject, AuthAudience audience) {
        if (subject == null || subject.isEmpty()) {
            return;
        }
        String old = redisTemplate.opsForValue().get(AuthRedisKeys.refreshSubject(audience, subject));
        if (old != null) {
            redisTemplate.delete(AuthRedisKeys.refreshToken(audience, old));
        }
        redisTemplate.delete(AuthRedisKeys.refreshSubject(audience, subject));
    }
}
