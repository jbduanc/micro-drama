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
| POST | `/v1/video/upload-url` | 申请原片直传预签名 URL（JSON） |
| POST | `/v1/video/upload-complete` | 直传完成后回调，落库并发 Kafka |
| GET | `/v1/video/play?videoId=&orderId=` | 播放鉴权（订单校验预留），返回带 token 的预签名 URL |

### 直传上传流程

1. `POST /v1/video/upload-url` body: `{"dramaId":"1001","episodeId":"1"}`  
   → 返回 `uploadUrl`、`fileKey`（`raw/1001/1.mp4`）、`videoId`
2. 前端 `PUT uploadUrl`，Body 为视频文件
3. `POST /v1/video/upload-complete` body: `{"videoId","fileKey","dramaId","episodeId","etag?","sizeBytes?"}`  
   → 校验 OSS 对象存在 → 落库 → Kafka → 创建转码任务

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
| `oss_endpoint` / `oss_access_key` / `oss_secret_key` / `oss_bucket` | 阿里云 OSS |
| `db_host` / `db_dsn` | PostgreSQL |
| `kafka_enabled` / `kafka_brokers` | Kafka（本地可 `kafka_enabled: false`） |
| `playback_url_expire_seconds` | 播放 URL 有效期（秒） |
