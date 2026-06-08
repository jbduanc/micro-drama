# API 网关与鉴权架构

## 组件职责

| 组件 | 职责 |
|------|------|
| **Traefik** | HTTP 路由、CORS、路径前缀剥离（video-api）、ForwardAuth 调用 |
| **gateway-auth** | JWT 验签（HS256）、aud 校验、refresh、注入 `X-Auth-*` |
| **backend-admin / user** | 登录签发 token、refresh 接口、Redis 会话 |
| **backend-content / payment / video** | 信任网关头 + Redis 会话校验（`auth.gateway.mode=gateway`） |

## 请求链路（受保护 API）

```text
Client
  Authorization: Bearer <access>     （可选，localStorage）
  Cookie: md_access / md_refresh     （httpOnly，api 域）
  X-Refresh-Token: <refresh>         （可选，客户端 fallback）
        │
        ▼
Traefik forwardAuth → GET gateway-auth:6010/auth/verify
        │  X-Forwarded-Uri 决定 aud 策略
        ├─ access 有效 → 200 + X-Auth-Subject/Audience
        ├─ access 过期 + refresh 有效 → 换票 + Set-Cookie + X-Access-Token-Refreshed
        └─ 均无效 → 401
        ▼
业务服务（仅校验 Redis 会话/黑名单，不重复验 JWT 签名）
```

## aud 策略

| 路径前缀 | 允许 aud |
|----------|----------|
| `/admin-api` | admin |
| `/user-api`、`/payment-api` | user |
| `/content-api`、`/video-api` | admin 或 user |

## Token 生命周期

- **accessToken**：JWT，默认 2h，`jwt.access-expire`
- **refreshToken**：Redis 不透明 token，默认 30d，`jwt.refresh-expire`
- **Cookie**：`md_access`、`md_refresh`，domain `.dramadjbo.com`，httpOnly

## 客户端行为

| 客户端 | 凭证 | refresh |
|--------|------|---------|
| Admin SPA | Bearer + Cookie + `withCredentials` | 网关静默 + 401 调 `/oauth2/refresh` |
| Telegram 小程序 | Bearer + Cookie + `credentials: include` | 网关静默 + 401 调 `/auth/refresh` |

## 未来演进（可选）

1. **RS256 + Keycloak/Auth0**：gateway-auth 改 JWKS 验签；业务改 `spring.security.oauth2.resourceserver`
2. **纯 Cookie 模式**：前端去掉 localStorage Bearer，401 直接跳登录
3. **ACK**：Traefik Ingress + gateway-auth Deployment
