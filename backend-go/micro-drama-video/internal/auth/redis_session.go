package auth

import (
	"context"
	"errors"

	"github.com/redis/go-redis/v9"
)

// SessionStore gateway 模式下校验 Redis 登录态/黑名单（键格式与 Java AuthRedisKeys 一致）。
type SessionStore interface {
	SessionValid(ctx context.Context, token, subject, audience string) (bool, error)
}

// RedisSessionStore 使用与 admin/user/content 相同的 Redis 键。
type RedisSessionStore struct {
	client *redis.Client
}

func NewRedisSessionStore(client *redis.Client) *RedisSessionStore {
	return &RedisSessionStore{client: client}
}

func loginTokenKey(audience, subject string) string {
	return audience + ":login:token:" + subject
}

func blacklistKey(audience, token string) string {
	return audience + ":blacklist:" + token
}

func (s *RedisSessionStore) SessionValid(ctx context.Context, token, subject, audience string) (bool, error) {
	if token == "" || subject == "" || audience == "" {
		return false, nil
	}
	blacked, err := s.client.Exists(ctx, blacklistKey(audience, token)).Result()
	if err != nil {
		return false, err
	}
	if blacked > 0 {
		return false, nil
	}
	cached, err := s.client.Get(ctx, loginTokenKey(audience, subject)).Result()
	if err != nil {
		if errors.Is(err, redis.Nil) {
			return false, nil
		}
		return false, err
	}
	return cached == token, nil
}
