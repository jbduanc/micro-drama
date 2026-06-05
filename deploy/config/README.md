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
