# Kong 网关（生产 backend-* ）

**全新重部署（丢弃旧配置）**见 [REDEPLOY.md](./REDEPLOY.md) 与 [docker-compose-kong.example.yml](./docker-compose-kong.example.yml)。

## 与当前 compose 的对应关系

| Kong Service | 上游 URL | 对外路径前缀 | strip_path |
|--------------|----------|--------------|------------|
| backend-admin | `http://backend-admin:6001` | `/admin-api` | **false**（保留 context-path） |
| backend-user | `http://backend-user:6003` | `/user-api` | false |
| backend-content | `http://backend-content:6002` | `/content-api` | false |
| backend-payment | `http://backend-payment:6004` | `/payment-api` | false |
| backend-video | `http://backend-video:6005` | `/video-api` | **true**（剥离前缀 → `/v1/video/...`） |

`backend-chain` 不经 Kong HTTP，payment 使用 `CHAIN_GRPC_ADDRESS=static://backend-chain:9092`。

## 环境变量（Kong 容器）

```bash
# 与各服务 /config 内 jwt.secret 相同；勿写入 Git / kong.yml
export JWT_SECRET='<在服务器单独保管>'
```

## 部署

```bash
# 确认 kong.yml 中 upstream 与 compose 服务名、端口一致
deck sync -s deploy/kong/kong.yml
# 或挂载到 Kong 后 kong reload
```

本地开发 compose（`micro-drama-*:8080`、无 context-path）请改用根目录 `docker-compose.yml` 或自行改 upstream，**不要**与生产 `kong.yml` 混用。

## 免登接口（无 pre-function 插件）

| 前缀 | 路径示例 |
|------|----------|
| admin-api | `GET /admin-api/oauth2/authorize-url`、`POST /admin-api/oauth2/login/google`、`/admin-api/actuator/**` |
| user-api | `POST /user-api/auth/telegram`、`/user-api/auth/dev/init`、`/user-api/auth/web3/challenge`、`/user-api/actuator/**` |
| payment-api | `GET /payment-api/healthz`、`/payment-api/actuator/**` |
| video-api | `GET /video-api/healthz`、`POST /video-api/v1/video/oss-event`（strip 后为 `/healthz`、`/v1/video/oss-event`） |

新增免登接口：在同一 Service 下增加**更具体**的 Route，`strip_path` 与受保护路由一致，**不要**挂 JWT 插件。

## 需登录接口

- `POST /admin-api/oauth2/logout`、`GET /admin-api/oauth2/user/info` 及所有其它 `/admin-api/**`
- `GET /user-api/auth/user/info`、`POST /user-api/auth/logout` 及所有其它 `/user-api/**`
- `/content-api/**`、`/payment-api/**`（除上表免登）、`/video-api/**`（除上表免登）

校验逻辑见 `kong.yml` 中 **pre-function**（HS256 + `aud` + 注入 `X-Auth-Subject` / `X-Auth-Audience`）。

## 下游取登录人

| 服务 | 用法 |
|------|------|
| Java admin / user | `SecurityUserUtils` |
| Java content | `ContentSecurityUtils.getUserId()` / `getAdminEmail()` |
| Java payment | `PaymentSecurityUtils.getCurrentUserId()` |
| Go video | `auth.GetUserID(c)` |

请求须经 Kong（带 `X-Kong-Request-Id`）。业务服务配置 `auth.gateway.mode=kong` 时，content/payment 仍可用 `Authorization` 做 Redis 会话校验。

## 安全提醒

- 勿在仓库提交真实 `jwt.secret`、OSS Key、DB 密码；使用 `deploy/config/*.example` 模板。
- 客户端不要直连 `6001–6007` 端口对外暴露业务 API，应只暴露 Kong HTTPS。
