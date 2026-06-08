# ECS Docker → 阿里云 ACK/K8s

## 组件映射

```text
ECS 现在                    ACK 以后
──────────────────────────────────────────────────
~/traefik/traefik.yml      Traefik Helm + IngressRoute CRD
dynamic/routes.yml         同名 CRD 或 ConfigMap 挂载
gateway-auth 容器           Deployment + Service (6010)
JWT_SECRET .env            Kubernetes Secret
app-network                Cluster DNS / Service
Nginx 443 → :3000          ALB Ingress → Traefik
```

## 推荐 Helm

```bash
helm repo add traefik https://traefik.github.io/charts
helm install traefik traefik/traefik -n ingress --create-namespace
```

## gateway-auth Deployment（概念）

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: gateway-auth
spec:
  replicas: 2
  template:
    spec:
      containers:
        - name: gateway-auth
          image: registry.example.com/backend-micro-drama-gateway-auth:latest
          ports:
            - containerPort: 6010
          envFrom:
            - secretRef:
                name: jwt-secret
          volumeMounts:
            - name: config
              mountPath: /config
      volumes:
        - name: config
          configMap:
            name: gateway-auth-config
---
apiVersion: v1
kind: Service
metadata:
  name: gateway-auth
spec:
  ports:
    - port: 6010
      targetPort: 6010
```

## ForwardAuth Middleware（Traefik CRD 概念）

```yaml
apiVersion: traefik.io/v1alpha1
kind: Middleware
metadata:
  name: forward-auth
spec:
  forwardAuth:
    address: http://gateway-auth:6010/auth/verify
    authResponseHeaders:
      - X-Auth-Subject
      - X-Auth-Audience
      - Authorization
      - X-Gateway-Request-Id
      - X-Access-Token-Refreshed
    authRequestHeaders:
      - Authorization
      - Cookie
      - X-Refresh-Token
    addAuthCookiesToResponse: true
```

## IngressRoute 示例

将 `deploy/traefik/dynamic/routes.yml` 中 `routers` / `services` 逐条转为 `IngressRoute` + `TraefikService`。

业务 Pod 通过 K8s Service 名访问：`backend-admin`、`backend-user` 等。

## 密钥与配置

- `JWT_SECRET` → `Secret`，挂载到 gateway-auth 与各 Java Deployment
- Redis/Postgres 改用 K8s Service 名或阿里云托管 Redis/ RDS
- `AUTH_COOKIE_DOMAIN` 保持 `.dramadjbo.com`

## 滚动迁移建议

1. ACK 集群就绪，先部署业务 Pod + gateway-auth
2. Traefik Ingress 内网测试 `Ingress` 主机名
3. 切换 DNS `api.dramadjbo.com` 到 ALB
4. 下线 ECS 上旧网关容器

详细 ECS 首次部署见 [DEPLOY.md](./DEPLOY.md)。
