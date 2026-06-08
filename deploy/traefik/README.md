# Traefik API 网关

生产环境统一入口：`api.dramadjbo.com`

| 文档 | 说明 |
|------|------|
| **[DEPLOY.md](./DEPLOY.md)** | **ECS 完整部署步骤（必读）** |
| [AUTH-ARCHITECTURE.md](./AUTH-ARCHITECTURE.md) | 鉴权、refresh、aud 策略 |
| [K8S-MIGRATION.md](./K8S-MIGRATION.md) | 上 ACK/K8s |

## 目录

| 文件 | 用途 |
|------|------|
| `traefik.yml` | 静态配置（entryPoint、file provider） |
| `dynamic/routes.yml` | 路由、CORS、ForwardAuth、upstream |
| `docker-compose-traefik.example.yml` | ECS compose 示例 |

## 快速启动

```bash
cp docker-compose-traefik.example.yml docker-compose-traefik.yml
docker compose -f docker-compose-traefik.yml up -d
```

完整前置条件、Nginx、业务配置见 [DEPLOY.md](./DEPLOY.md)。
