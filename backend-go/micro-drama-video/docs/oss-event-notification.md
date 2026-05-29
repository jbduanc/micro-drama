# 阿里云 OSS 上传完成 → 自动触发转码

上传完成后由 OSS 回调 `micro-drama-video` 的 `POST /v1/video/oss-event`，服务校验对象后发送 Kafka，**无需管理端再调 notify-transcode**。

## 区域说明（重要）

| 区域 | 控制台「事件通知」 | 推荐方案 |
|------|-------------------|----------|
| 中国大陆部分区域 | 可走 MNS 主题/队列 | 方式一 或 方式二 |
| **新加坡等海外区域** | **仅支持 MNS**，当地可能**未开通消息队列** | **方式二：上传回调**（无需控制台配置） |

> 新加坡 Bucket 创建「事件通知」时只能选「主题 / 队列」，不能直填 HTTP。  
> 本项目使用 **上传回调（PutObject Callback）**：STS 返回带**一次性 token** 的 `callbackUrl`，前端 `ali-oss` 上传时携带，OSS 服务器 POST 你的 API。

---

## 数据库迁移（首次部署必做）

```bash
psql -d video_db -f migrations/001_video_asset_callback_token.sql
```

为 `video_asset` 增加 `callback_token` 字段，用于存储每次 STS 签发的一次性回调口令。

---

## 前置条件

1. Consul 配置 `oss_upload_callback_base_url`（公网 video-api 根路径）。
2. `video-api` 对**公网**可访问（本地用 ngrok）。
3. Bucket 已开启 **CORS**（浏览器 STS 直传）。

### CORS 示例

| 配置项 | 值 |
|--------|-----|
| 来源 | `http://localhost:5173`、生产管理端域名 |
| Methods | `PUT`, `POST`, `GET`, `HEAD` |
| 允许 Headers | `*` |
| 暴露 Headers | `ETag`, `x-oss-request-id` |

---

## 方式一：控制台事件通知 + MNS

适用于已开通 MNS 的区域。新加坡一般不可用，请用方式二。

可选配置全局密钥（仅 MNS/JSON 事件用）：

```yaml
oss_event_callback_secret: your-global-token
```

---

## 方式二：上传回调（推荐，新加坡 / 无 MNS）

**无需在 OSS 控制台创建事件通知规则。**

### 原理

```text
POST /v1/video/sts
  → 生成 64 位随机 callback_token 写入 video_asset
  → 返回 callbackUrl（含 videoId + 一次性 token）

浏览器 multipartUpload(callbackUrl)
  → OSS 存文件
  → OSS POST callbackUrl
  → 服务端校验 token、立即作废（置 NULL）
  → 触发 Kafka 转码
```

### 一次性 token 安全说明

| 项目 | 说明 |
|------|------|
| 谁生成 | **服务端**每次 `/v1/video/sts` 自动生成（`crypto/rand` 32 字节 hex） |
| 存哪 | `video_asset.callback_token`，回调成功后**立即清空** |
| 能否复用 | **不能**；同一 token 只能用一次 |
| 泄露风险 | DevTools 可见，但只能触发**该次上传**对应视频，用后失效 |
| 还要配 `oss_event_callback_secret` 吗 | **上传回调不需要**；仅 MNS 事件通知可选 |

### Consul 配置

```yaml
oss_upload_callback_base_url: https://api.dramadjbo.com/video-api
```

STS 返回示例：

```json
{
  "videoId": "uuid-...",
  "callbackUrl": "https://api.dramadjbo.com/video-api/v1/video/oss-event?videoId=uuid-...&token=64位十六进制一次性口令",
  ...
}
```

### 回调要求

- 公网 HTTPS（或 ngrok 测试）
- 成功响应：`{"Status":"Ok"}`

### 本地调试

```bash
ngrok http 8080
```

```yaml
oss_upload_callback_base_url: https://xxxx.ngrok-free.app
```

### 验证

1. 管理端上传测试视频。
2. 日志：`callback token consumed`、`transcode triggered`。
3. `video_asset.callback_token` 应为 `NULL`，`status` → `TRANSCODING` → `READY`。

---

## 完整流程

```text
POST /v1/video/sts → UPLOADING + callback_token
ali-oss multipartUpload（带 callbackUrl）
OSS POST /v1/video/oss-event?videoId=&token=
  → 消费 token → Kafka → TRANSCODING
transcoder → READY → 可播放
```

---

## 常见问题

| 现象 | 处理 |
|------|------|
| invalid or expired callback token | token 已用过、或没先调 `/sts`；重新上传 |
| 上传成功但不转码 | `oss_upload_callback_base_url` 是否公网可达 |
| CallbackFailed | video-api 是否返回 `{"Status":"Ok"}` |
| column callback_token does not exist | 执行 `migrations/001_video_asset_callback_token.sql` |

---

## Consul 配置项汇总

```yaml
oss_region: oss-ap-southeast-1
oss_sts_role_arn: acs:ram::xxx:role/oss-upload
oss_sts_duration_seconds: 3600
oss_upload_callback_base_url: https://api.dramadjbo.com/video-api
oss_upload_prefix: raw
kafka_enabled: true
kafka_topic_transcode_completed: video.transcode.completed
kafka_topic_transcode_failed: video.transcode.failed
kafka_consumer_group: micro-drama-video
# 可选，仅 MNS 事件通知：
# oss_event_callback_secret: global-webhook-secret
```
