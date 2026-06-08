# 外挂配置目录（生产）

容器挂载 **`./config/backend-<服务名>:/config:ro`**，文件名 **`application.yml`**（Go 可用 `.yaml`，建议设置 `CONFIG_FILE=/config/application.yml`）。

## 目录对照

| 宿主机目录 | Compose 服务 | 端口 | context-path / 说明 |
|------------|--------------|------|---------------------|
| `backend-admin/` | backend-admin | 6001 | `/admin-api` |
| `backend-content/` | backend-content | 6002 | `/content-api` |
| `backend-user/` | backend-user | 6003 | `/user-api` |
| `backend-payment/` | backend-payment | 6004 | `/payment-api` |
| `backend-video/` | backend-video | 6005 | Go 无 context-path |
| `backend-transcoder/` | backend-transcoder | 6006 | 内网 worker |
| `backend-chain/` | backend-chain | 9092（映射 6007） | 仅 gRPC |
| `gateway-auth/` | gateway-auth | 6010 | Traefik ForwardAuth |

仓库内 `*.example` 为**脱敏模板**，复制为 `application.yml` 后在服务器填写真实密钥。

## 密钥（勿提交 Git）

- `jwt.secret` 与 **gateway-auth**、各 Java/Go 服务必须一致
- 数据库、Redis、OSS、Google OAuth、Telegram 等使用环境变量或密钥管理注入
- 若配置曾泄露，请轮换 OSS AccessKey、DB 密码、`jwt.secret`、OAuth Client Secret

## 网关模式

登录 JWT 验签在 **Traefik ForwardAuth**（`deploy/traefik/` + `gateway-auth` 服务）。业务服务生产配置：

```yaml
auth:
  gateway:
    mode: gateway
```

Go video 服务：`auth_gateway_mode: gateway`

## compose 示例

- 业务服务：`deploy/docker-compose-services.example.yml`
- 网关：`deploy/traefik/docker-compose-traefik.example.yml`

Java 服务在 compose 中建议注入：

```yaml
environment:
  - DB_HOST=postgres
  - DB_PORT=5432
  - DB_NAME=postgres
  - DB_USER=postgres
  - DB_PASSWORD=***
  - REDIS_HOST=redis
  - REDIS_PORT=6379
  - REDIS_PASSWORD=***
  - JWT_SECRET=***
```

## 启动失败：`url attribute is not specified`

1. 查看外挂配置是否含 **`spring.datasource.url`**
2. 进容器确认挂载：`docker compose run --rm --no-deps backend-user cat /config/application.yml`
3. 从 `application.yml.example` 复制完整 `spring.datasource` 段
