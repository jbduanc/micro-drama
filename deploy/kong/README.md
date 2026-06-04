# Kong 网关

- 上游地址使用 **Docker 网络 / K8s Service 名**（如 `http://micro-drama-payment:8080`），不再使用 Consul DNS。
- 业务配置由各服务挂载 `deploy/config/<service>/` 目录（见仓库根目录 `docker-compose.yml`）。
- `micro-drama-chain` 仅对内 gRPC（`:9092`），由 `micro-drama-payment` 通过 `CHAIN_GRPC_ADDRESS=static://micro-drama-chain:9092` 调用。
