# micro-drama-video

微短剧 **视频 API 服务**（Go）。负责：原片上传 OSS、写库、发 Kafka 通知转码、播放鉴权（HLS 预签名 URL）。

转码由独立服务 `micro-drama-transcoder` 消费 Kafka 完成，本服务 **不跑 FFmpeg**。

---

## Java 开发者对照

| Java (Spring) | 本仓库 Go |
|---------------|-----------|
| `@SpringBootApplication` main | `cmd/video-api/main.go` |
| `application.yml` + Nacos/Consul | Consul KV `config/micro-drama-video/data` + 环境变量 |
| `@RestController` | `internal/handler` |
| `@Service` | `internal/service` |
| `@Repository` / MyBatis | `internal/repository` |
| Entity | `internal/model` |
| `Result<T>` 统一响应 | `internal/response` |
| `RestTemplate` / OSS SDK | `internal/storage` (minio-go) |
| `KafkaTemplate` 生产者 | `internal/kafka` |
| 事件 DTO | `internal/events` |
| Logback / SLF4J | `go.uber.org/zap`（JSON 结构化日志） |

---

## 目录说明

```
micro-drama-video/
├── cmd/video-api/main.go      # 程序入口：装配依赖、启动 HTTP
├── internal/
│   ├── config/                # 从 Consul 加载配置并校验
│   ├── consul/                # Consul KV 配置 + 服务注册
│   ├── handler/               # HTTP 路由（Gin），类比 Controller
│   ├── service/               # 业务编排，类比 Service
│   ├── repository/            # PostgreSQL，类比 Mapper/Repository
│   ├── model/                 # 表对应结构体，类比 Entity
│   ├── storage/               # 阿里云 OSS（S3 协议）
│   ├── kafka/                 # Kafka 生产者
│   ├── events/                # Kafka 消息体（与 Java common 对齐）
│   └── response/              # 统一 JSON 响应 Result
├── .vscode/                   # VS Code 调试配置（可选）
├── go.mod                     # 模块依赖（类似 pom.xml）
└── Dockerfile                 # 容器镜像构建
```

---

## 启动前准备

1. **Consul** 可达，且存在 KV：`config/micro-drama-video/data`（YAML）。
2. KV 中 `db_host`、`kafka_brokers` 等须为本机或 IP，不能写 Docker 服务名 `postgres`/`kafka`（除非本机 hosts 已映射）。
3. 环境变量（本地 VS Code 见 `.vscode/launch.json`）：
   - `CONSUL_HOST`、`CONSUL_PORT`
   - `VIDEO_CONSUL_DISCOVERY_ENABLED=false`（本地一般不注册服务）

```powershell
cd backend-go/micro-drama-video
$env:CONSUL_HOST="你的Consul地址"
$env:CONSUL_PORT="8500"
$env:VIDEO_CONSUL_DISCOVERY_ENABLED="false"
go run ./cmd/video-api
```

成功日志应包含：`micro-drama-video api started`。

---

## HTTP 接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/healthz` | 健康检查 |
| POST | `/v1/video/sts` | 申请阿里云 STS 临时凭证（管理端 ali-oss / 签名直传） |
| POST | `/v1/video/oss-event` | OSS 事件通知 / 上传回调，自动触发转码 |
| POST | `/v1/video/upload-url` | 申请原片直传预签名 URL（JSON，兼容旧流程） |
| POST | `/v1/video/upload-complete` | 直传完成回调（与 notify-transcode 相同，兼容旧客户端） |
| POST | `/v1/video/notify-transcode` | 管理端保存剧集时通知转码（校验 OSS → 落库 → Kafka） |
| POST | `/v1/video/delete` | 批量删除视频（OSS + 数据库） |
| GET | `/v1/video/play?videoId=&orderId=` | 播放鉴权（需状态 READY），返回带 token 的预签名 URL |

### STS 直传上传流程（推荐）

1. `POST /v1/video/sts` body: `{"dramaId":"uuid","episodeId":"num-1"}`  
   → 返回 STS 凭证、`endpoint`（Bucket 域名）、`fileKey`、`videoId`；预创建 `video_asset` 状态 `UPLOADING`
2. 前端使用 STS 将视频 `PUT` 到 `https://{bucket}.{region}.aliyuncs.com/{fileKey}`
3. OSS 配置事件通知，回调 `POST /v1/video/oss-event`（可带 Header `X-Oss-Callback-Token`）  
   → 校验对象 → Kafka `content.video.upload_completed` → 创建转码任务
4. `micro-drama-transcoder` 转码完成后发 `video.transcode.completed`，本服务消费后把 `video_asset.status` 置为 `READY`
5. `GET /v1/video/play?videoId=` 在状态为 `READY` 时返回 HLS 预签名播放地址

OSS 事件通知控制台配置详见：[docs/oss-event-notification.md](docs/oss-event-notification.md)

### 兼容：预签名 PUT + 手动 notify

1. `POST /v1/video/upload-url` → 预签名 URL  
2. 前端 PUT 上传  
3. `POST /v1/video/notify-transcode`（管理端旧逻辑，新前端不再调用）

---

## 上传完成流程（日志关键字）

1. `upload url created` → 签发预签名 PUT  
2. `upload complete started` → 收到完成回调  
3. `video_asset created` → 写入 PostgreSQL  
4. `kafka event published` → 通知 transcoder  
5. `upload complete finished` → 转码任务已创建  

---

## 配置项（Consul KV 常见键）

| 键 | 含义 |
|----|------|
| `http_addr` | 监听地址，如 `:8080` |
| `oss_endpoint` / `oss_access_key` / `oss_secret_key` / `oss_bucket` / `oss_region` | 阿里云 OSS |
| `oss_sts_role_arn` | RAM 角色 ARN（STS 直传必填） |
| `oss_sts_duration_seconds` | STS 有效期，默认 3600 |
| `oss_upload_callback_base_url` | 公网 video-api 根路径；STS 自动生成一次性 callback token 拼入 URL |
| `oss_event_callback_secret` | 可选，仅 MNS/JSON 事件通知全局密钥（上传回调不需要） |
| `kafka_topic_transcode_completed` / `kafka_topic_transcode_failed` | 转码结果消费 |
| `kafka_consumer_group` | 默认 `micro-drama-video` |
| `db_host` / `db_dsn` | PostgreSQL |
| `kafka_enabled` / `kafka_brokers` | Kafka（本地可 `kafka_enabled: false`） |
| `playback_url_expire_seconds` | 播放 URL 有效期（秒） |
