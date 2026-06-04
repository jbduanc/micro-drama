# Go 微服务

目标：Worker / Web3 / 高并发播放等用 Go；与 Java `content` 等通过 **gRPC / Kafka** 协作。

## micro-drama-video（视频 API）

- **目录**：`micro-drama-video/`
- **入口**：`cmd/video-api/main.go`
- **技术**：Gin + 外挂 YAML 配置 + 阿里云 OSS + Kafka + PostgreSQL（转码由 `micro-drama-transcoder` 消费 Kafka 完成）

### 环境变量（前缀 `VIDEO_`）

| 变量 | 说明 | 示例 |
|------|------|------|
| `VIDEO_KAFKA_BROKERS` | 逗号分隔 | `kafka:9092` |
| `VIDEO_KAFKA_CONSUMER_GROUP` | 消费组 | `micro-drama-video` |
| `VIDEO_KAFKA_TOPIC_UPLOAD_COMPLETED` | 与 Java `Topics.CONTENT_VIDEO_UPLOAD_COMPLETED` 一致 | `content.video.upload_completed` |
| `VIDEO_KAFKA_TOPIC_TRANSCODE_COMPLETED` | 完成事件 topic | `video.transcode.completed` |
| `VIDEO_KAFKA_TOPIC_TRANSCODE_FAILED` | 失败 topic | `video.transcode.failed` |
| `VIDEO_S3_ENDPOINT` | S3 兼容 endpoint（不含 `https://`） | `ewr1.vultrobjects.com` |
| `VIDEO_S3_ACCESS_KEY` / `VIDEO_S3_SECRET_KEY` | 密钥 | — |
| `VIDEO_S3_BUCKET` | 桶名 | — |
| `VIDEO_S3_USE_SSL` | `true`/`false` | `true` |
| `VIDEO_S3_REGION` | 可选 | — |
| `VIDEO_FFMPEG_PATH` | ffmpeg 可执行文件 | `ffmpeg` |
| `VIDEO_WORK_DIR` | 临时目录 | `/tmp/micro-drama-video` |

当前 **FFmpeg / 上传 HLS** 在 `internal/worker` 内为可跑通的占位逻辑，接入真实源文件与 S3 后替换即可。

## micro-drama-chain（链上交互）

- **目录**：`micro-drama-chain/`
- **入口**：`cmd/chain/main.go`
- **技术**：gRPC + **go-ethereum**（EVM 海外主流 SDK）+ 外挂 `config/application.yaml`

| 配置项 | 说明 |
|--------|------|
| `grpc_addr` | gRPC 监听，默认 `:9092` |
| `eth_rpc_http` | JSON-RPC HTTP |
| `eth_ws` | 预留 WS 订阅 |

由 `micro-drama-payment`（Java）经 gRPC 调用：`CreatePendingOrder`、`SendRawTransaction`。

## micro-drama-payment（Java 支付）

- **目录**：`backend-java/micro-drama-payment/`
- **HTTP**：`POST /v1/orders`、`POST /v1/tx/raw`（小程序 `payment-api`）
- **gRPC 客户端**：`static://micro-drama-chain:9092`

## micro-drama-transcoder

见 `micro-drama-transcoder/`（Gin 骨架；与 `micro-drama-video` Kafka Worker 分工：本服务偏 HTTP 侧编排/签 URL 等）。
