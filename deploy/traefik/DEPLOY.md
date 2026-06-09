# 生产部署指南（阿里云 ECS + Docker Compose）

本文档描述 **Traefik + gateway-auth（ForwardAuth）** 网关的完整部署流程，适用于当前单机 ECS；文末含 ACK/K8s 迁移要点。

---

## 1. 架构总览

```text
Internet
   │
   ▼
Nginx/Caddy（ECS，HTTPS 终止）
   ├─ admin.dramadjbo.com        → Admin SPA 静态文件
   ├─ www.dramadjbo.com          → 小程序 / 官网
   └─ api.dramadjbo.com:443      → 反代 127.0.0.1:3000
                                      │
                                      ▼
                               Traefik (:8000)
                                 ├─ CORS
                                 ├─ strip /video-api 前缀
                                 ├─ 免登路由 → 直接 upstream
                                 └─ 受保护路由
                                        │
                                        ▼ ForwardAuth
                                 gateway-auth (:6010)
                                   JWT 验签 + aud
                                   access 过期 → refresh
                                   注入 X-Auth-* / Authorization
                                        │
                    ┌───────────────────┼───────────────────┐
                    ▼                   ▼                   ▼
            backend-admin:6001   backend-user:6003   backend-content:6002
            backend-payment:6004 backend-video:6005
                    │
            backend-chain:9092（gRPC，不经 HTTP 网关）
```

| 组件 | 容器名 | 对外端口 | 内存约 |
|------|--------|----------|--------|
| Traefik | traefik | 3000→8000 | ~80MB |
| gateway-auth | gateway-auth | 6010（内网） | ~128MB |
| backend-* | 各业务 | 6001–6007（勿公网暴露） | 视服务而定 |

**不要**将 6001–6007 直接暴露公网；公网只开 443 → Traefik。

---

## 2. 前置条件

- ECS 已安装 Docker + Docker Compose v2
- 已有 Docker 网络 `app-network`（业务 compose 使用）
- 已运行：Postgres、Redis、各 `backend-*` 业务容器
- 域名 DNS 指向 ECS；Nginx 已配置 HTTPS 证书
- 本地/registry 已构建镜像：
  - `backend-micro-drama-gateway-auth:latest`
  - 各 backend 服务最新镜像（含 refresh token 改动）

---

## 3. 构建 gateway-auth 镜像

在 CI 或构建机：

```bash
cd backend-java/micro-drama-common
mvn install -DskipTests

cd ../micro-drama-gateway-auth
mvn package -DskipTests
docker build -t 127.0.0.1:8082/backend-micro-drama-gateway-auth:latest .
docker push 127.0.0.1:8082/backend-micro-drama-gateway-auth:latest   # 若用私有仓库
```

---

## 4. 配置业务服务

在 ECS `~/services/config/` 下，各服务 `application.yml` 需包含：

```yaml
jwt:
  secret: "<与 gateway-auth 相同>"
  access-expire: 7200000      # 2h
  refresh-expire: 2592000000  # 30d

auth:
  gateway:
    mode: gateway
  cookie:
    domain: .dramadjbo.com
    secure: true
```

Go `backend-video/application.yml`：

```yaml
auth_gateway_mode: gateway
jwt_secret: "<同上>"
```

模板见 `deploy/config/*/application.yml.example`。

重启业务容器使配置生效：

```bash
cd ~/services
docker compose up -d
```

---

## 5. 部署 Traefik + gateway-auth

```bash
mkdir -p ~/traefik/dynamic ~/traefik/config/gateway-auth
cd ~/traefik

# 从仓库复制
cp /path/to/micro-drama/deploy/traefik/traefik.yml .
cp /path/to/micro-drama/deploy/traefik/dynamic/routes.yml ./dynamic/
cp /path/to/micro-drama/deploy/traefik/docker-compose-traefik.example.yml ./docker-compose-traefik.yml
cp /path/to/micro-drama/deploy/config/gateway-auth/application.yml.example ./config/gateway-auth/application.yml
```

编辑 `config/gateway-auth/application.yml`：

```yaml
jwt:
  secret: "<与各 backend 一致>"
  access-expire: 7200000
  refresh-expire: 2592000000

spring:
  redis:
    host: redis        # 与业务同网 app-network 中的 Redis 服务名
    port: 6379
    password: "***"

auth:
  cookie:
    domain: .dramadjbo.com
    secure: true
```

启动：

```bash
docker compose -f docker-compose-traefik.yml up -d
docker compose -f docker-compose-traefik.yml ps
curl -s http://127.0.0.1:3000/payment-api/healthz   # 应 200
```

---

## 6. 配置 Nginx（api 域名）

`/etc/nginx/conf.d/api.dramadjbo.com.conf` 示例：

```nginx
server {
    listen 443 ssl http2;
    server_name api.dramadjbo.com;

    ssl_certificate     /path/to/fullchain.pem;
    ssl_certificate_key /path/to/privkey.pem;

    location / {
        proxy_pass http://127.0.0.1:3000;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_set_header X-Forwarded-Uri $request_uri;
    }
}
```

`X-Forwarded-Uri` 供 ForwardAuth 判断 aud 策略（admin/user/both）。

重载 Nginx：`nginx -t && systemctl reload nginx`

---

## 7. 路由与鉴权规则

| 对外前缀 | 上游 | strip 前缀 | aud 要求 |
|----------|------|------------|----------|
| `/admin-api` | backend-admin:6001 | 否 | admin |
| `/user-api` | backend-user:6003 | 否 | user |
| `/content-api` | backend-content:6002 | 否 | admin 或 user |
| `/payment-api` | backend-payment:6004 | 否 | user |
| `/video-api` | backend-video:6005 | **是** | admin 或 user |

### 免登（无 ForwardAuth）

| 路径 |
|------|
| `GET/POST /admin-api/oauth2/authorize-url`、`/login/google`、`/refresh` |
| `POST /user-api/auth/telegram`、`/dev/init`、`/web3/challenge`、`/refresh` |
| `GET /payment-api/healthz` |
| `GET /video-api/healthz`、`POST /video-api/v1/video/oss-event` |
| 各服务 `/actuator/**` |

完整规则见 [`dynamic/routes.yml`](./dynamic/routes.yml)。

**注意**：`OPTIONS` 预检必须绕过 ForwardAuth（见 `*-api-options` 路由，priority 200）。否则浏览器对 `POST /content-api/...` 等写操作会先收到 401，表现为仅 POST 失败、GET 正常。

---

## 8. 验证清单

```bash
# 1. 健康检查
curl https://api.dramadjbo.com/payment-api/healthz

# 2. 小程序登录（免登）
curl -X POST https://api.dramadjbo.com/user-api/auth/telegram \
  -H 'Content-Type: application/json' \
  -d '{"initData":"..."}'

# 3. 受保护接口（需 Bearer）
curl https://api.dramadjbo.com/user-api/auth/user/info \
  -H "Authorization: Bearer <accessToken>"

# 4. gateway-auth 健康
curl http://127.0.0.1:6010/actuator/health

# 5. ForwardAuth 入口（应 401 无凭证，但响应体不应含 www-authenticate: Basic）
curl -i http://127.0.0.1:6010/auth/verify
# 正常：HTTP/1.1 401，无 Basic realm；异常：含 WWW-Authenticate: Basic realm="Realm" → gateway-auth 缺 SecurityConfig
```

浏览器：

1. Admin Google 登录 → 检查响应含 `accessToken` + `refreshToken`，Cookie 含 `md_access`/`md_refresh`
2. 小程序 Telegram 登录 → 同上
3. access 过期后操作 → 应静默 refresh，或 401 后自动调 `/refresh` 重试

---

## 9. 更新路由

改 `~/traefik/dynamic/routes.yml` 后 Traefik **自动热加载**（file provider `watch: true`），无需重启。

改 JWT 逻辑需重新构建并滚动更新 `gateway-auth` 容器。

---

## 10. 上 ACK/K8s（后续）

见 [K8S-MIGRATION.md](./K8S-MIGRATION.md)。

要点：

- Traefik Ingress Controller（Helm）或 NGINX Ingress + ForwardAuth
- `gateway-auth` Deployment（1–2 副本）
- `JWT_SECRET` → Secret；路由 → IngressRoute CRD
- 阿里云 ALB Ingress 作为公网入口

---

## 11. 与 Kong 方案对比 & 海外主流

| 维度 | 旧 Kong 方案 | 现 Traefik + ForwardAuth |
|------|-------------|---------------------------|
| 组件 | Kong + Postgres + deck | Traefik + 小 Java 服务 |
| 内存 | ~400MB+ | ~200MB |
| 自定义鉴权 | Lua post-function | Java（团队栈） |
| Token refresh | ❌ 不支持 | ✅ ForwardAuth 静默 refresh |
| 配置 | deck sync + DB | YAML 文件热加载 |
| 运维复杂度 | 中高 | 低 |

**是否符合海外主流？**

- **Traefik**：CNCF 生态，欧洲/云原生团队广泛使用，K8s Ingress 事实选项之一。
- **ForwardAuth**：Traefik、NGINX Ingress、Caddy 均支持的标准扩展点；与 **OAuth2 Proxy**、**Pomerium** 等同属常见模式。
- **对比 Kong**：Kong 在大型企业/API 管理平台仍常见，但对你这种 **JWT 路由 + 少量自定义逻辑 + 单机/小集群** 场景，Traefik 更轻；若未来要完整 API 管理（计费、开发者门户、多租户），再考虑 Kong/Apigee。

**结论**：对当前短剧平台（ECS → ACK、admin + 小程序、HS256 JWT），该方案 **更省资源、更易维护、具备 refresh 能力**，且 **符合海外云原生主流路径**；不是「最大厂标配」，但是 **中小团队生产可用、K8s 可平滑迁移** 的合理选择。

---

## 12. 相关文档

- [AUTH-ARCHITECTURE.md](./AUTH-ARCHITECTURE.md) — 鉴权与 refresh 流程
- [K8S-MIGRATION.md](./K8S-MIGRATION.md) — ACK 迁移
- [deploy/config/README.md](../config/README.md) — 业务配置说明
