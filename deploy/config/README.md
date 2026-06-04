# 外挂配置目录

各服务容器挂载为 `/config`，通过 `CONFIG_FILE` 或 Spring `spring.config.additional-location` 加载。

| 目录 | 服务 |
|------|------|
| `micro-drama-admin/` | 管理端 |
| `micro-drama-content/` | 内容 |
| `micro-drama-user/` | 用户 |
| `micro-drama-payment/` | 支付（Web2/Web3） |
| `micro-drama-chain/` | 链上 gRPC |
| `micro-drama-video/` | 视频 |

Docker Compose / K8s 使用同一网络内的 **服务名** 作为 hostname（见根目录 `docker-compose.yml`）。
