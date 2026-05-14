# Go 微服务

目标：Worker / Web3 / 高并发播放等用 Go；与 Java `content` 等通过 **gRPC / Kafka** 协作。

## micro-drama-video（转码 Worker）

- **目录**：`micro-drama-video/`
- **入口**：`cmd/video-worker/main.go`
- **技术**：Sarama（Kafka）+ 调用 FFmpeg + MinIO SDK（S3 兼容，适合 Vultr Object Storage）

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

## micro-drama-payment（Web3 支付）

- **目录**：`micro-drama-payment/`
- **入口**：`cmd/payment/main.go`
- **技术**：Gin + Viper + Zap + go-ethereum（HTTP RPC；链上监听可再接 WS）

### 环境变量（前缀 `PAYMENT_`）

| 变量 | 说明 | 示例 |
|------|------|------|
| `PAYMENT_HTTP_ADDR` | 监听地址 | `:8080` |
| `PAYMENT_ETH_RPC_HTTP` | 可选；不配置则链功能仅占位 | `https://...` |
| `PAYMENT_ETH_WS` | 预留：日志订阅 | `wss://...` |

### HTTP

- `GET /healthz`
- `POST /v1/orders` body: `{"id":"order-1"}`
- `POST /v1/tx/raw` body: `{"signedHex":"0x..."}`（待实现发链）

## micro-drama-transcoder

见 `micro-drama-transcoder/`（Gin 骨架；与 `micro-drama-video` Kafka Worker 分工：本服务偏 HTTP 侧编排/签 URL 等）。
