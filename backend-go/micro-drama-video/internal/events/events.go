// Package events 定义 Kafka 消息的 Topic 常量与 JSON 结构体。
//
// 字段名、Topic 名与 Java 模块 micro-drama-common 保持一致，
// 保证 Go 生产的消息能被 Java/Go 消费者正确反序列化。
//
// 本服务（video-api）仅生产 VideoUploadCompletedEvent；
// VideoTranscodeCompletedEvent / VideoTranscodeFailedEvent 由 transcoder 生产，此处定义便于对照文档。
package events

// Kafka Topic 常量，对应 Java com.series.common.events.Topics。
const (
	// ContentVideoUploadCompleted 原片上传完成，通知 transcoder 开始转码。
	ContentVideoUploadCompleted = "content.video.upload_completed"
	// VideoTranscodeCompleted 转码成功（本服务不发送，由 transcoder 发送）。
	VideoTranscodeCompleted = "video.transcode.completed"
	// VideoTranscodeFailed 转码失败（本服务不发送）。
	VideoTranscodeFailed = "video.transcode.failed"
)

// VideoUploadCompletedEvent 上传完成事件体。
// 对应 Java：com.series.common.events.video.VideoUploadCompletedEvent
//
// 消费方：micro-drama-transcoder，根据 sourceObjectKey 下载原片并转 HLS。
type VideoUploadCompletedEvent struct {
	// VideoID 视频资源唯一 ID，由本服务 uuid 生成。
	VideoID string `json:"videoId"`
	// DramaID 剧集 ID（可选）。用于转码服务落盘到 hls/{dramaId}/{episodeId}/。
	DramaID *string `json:"dramaId,omitempty"`
	// EpisodeID 集数 ID（可选）。用于转码服务落盘到 hls/{dramaId}/{episodeId}/。
	EpisodeID *string `json:"episodeId,omitempty"`
	// SourceObjectKey 原片在 OSS 中的对象键，例如 uploads/{videoId}/demo.mp4。
	SourceObjectKey string `json:"sourceObjectKey"`
	// SourceEtag OSS 返回的 ETag，可用于校验文件完整性（可选）。
	SourceEtag string `json:"sourceEtag,omitempty"`
	// UploadedAt 上传完成时间；Java 可能是 Date，Go 发毫秒时间戳 number 即可。
	UploadedAt any `json:"uploadedAt,omitempty"`
}

// TranscodeVariant 单路码率/分辨率 HLS 变体信息（转码完成事件中使用）。
type TranscodeVariant struct {
	Width       int32  `json:"width,omitempty"`
	Height      int32  `json:"height,omitempty"`
	BitrateKbps int32  `json:"bitrateKbps,omitempty"`
	PlaylistKey string `json:"playlistKey,omitempty"` // 该清晰度 playlist.m3u8 的 object key
}

// VideoTranscodeCompletedEvent 转码成功事件（transcoder → 其他服务）。
type VideoTranscodeCompletedEvent struct {
	VideoID           string             `json:"videoId"`
	MasterPlaylistKey string             `json:"masterPlaylistKey"` // 主 m3u8，播放鉴权通常签此 key
	Variants          []TranscodeVariant `json:"variants,omitempty"`
}

// VideoTranscodeFailedEvent 转码失败事件。
type VideoTranscodeFailedEvent struct {
	VideoID string `json:"videoId"`
	Reason  string `json:"reason"`           // 简短错误码，如 ffmpeg、oss_download
	Detail  string `json:"detail,omitempty"` // 详细错误信息
}
