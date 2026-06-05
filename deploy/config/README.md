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

仓库内 `*.example` 为**脱敏模板**，复制为 `application.yml` 后在服务器填写真实密钥。

## 密钥（勿提交 Git）

- `jwt.secret` 与 Kong 容器环境变量 **`JWT_SECRET`** 必须一致
- 数据库、Redis、OSS、Google OAuth、Telegram 等使用环境变量或密钥管理注入
- 若配置曾泄露到聊天/日志，请**轮换** OSS AccessKey、DB 密码、`jwt.secret`、OAuth Client Secret

## 网关

登录校验在 Kong（`deploy/kong/`）；Java 业务服务建议：

```yaml
auth:
  gateway:
    mode: kong
```

## compose 示例

见 `deploy/docker-compose-services.example.yml`（注意 volume 路径为 `backend-*`，不是 `micro-drama-*`）。

Java 服务在 compose 中建议注入（与 `application.yml` / JAR 内 `${DB_*}` 占位符对应）：

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

1. 查看外挂配置是否含 **`spring.datasource.url`**（缩进须在 `spring:` 下）：
   `cat config/backend-user/application.yml`
2. 进容器确认挂载：`docker compose run --rm --no-deps backend-user cat /config/application.yml`
3. 若外挂文件缺 datasource，可从 `application.yml.example` 复制整段 `spring.datasource`，或依赖 JAR 内默认 + 上述 `DB_*` 环境变量（需**重新构建镜像**）。
4. 勿在 `application.yml` 里写未在 compose 中声明的占位符（如 `${DB_URL}`），否则 url 会解析为空。
