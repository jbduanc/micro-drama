// Package events 定义 Kafka Topic 与 JSON 结构体。
package events

const (
	// ContentVideoUploadCompleted 原片上传完成，通知 transcoder 开始转码。
	ContentVideoUploadCompleted = "content.video.upload_completed"
	// VideoTranscodeCompleted 转码成功事件。
	VideoTranscodeCompleted = "video.transcode.completed"
	// VideoTranscodeFailed 转码失败事件。
	VideoTranscodeFailed = "video.transcode.failed"
)

type VideoUploadCompletedEvent struct {
	VideoID         string  `json:"videoId"`
	DramaID         *string `json:"dramaId,omitempty"`
	EpisodeID       *string `json:"episodeId,omitempty"`
	SourceObjectKey string  `json:"sourceObjectKey"`
	SourceEtag      string  `json:"sourceEtag,omitempty"`
	UploadedAt      any     `json:"uploadedAt,omitempty"`
}

type TranscodeVariant struct {
	Width       int32  `json:"width,omitempty"`
	Height      int32  `json:"height,omitempty"`
	BitrateKbps int32  `json:"bitrateKbps,omitempty"`
	PlaylistKey string `json:"playlistKey,omitempty"`
}

type VideoTranscodeCompletedEvent struct {
	VideoID           string             `json:"videoId"`
	MasterPlaylistKey string             `json:"masterPlaylistKey"`
	Variants          []TranscodeVariant `json:"variants,omitempty"`
}

type VideoTranscodeFailedEvent struct {
	VideoID string `json:"videoId"`
	Reason  string `json:"reason"`
	Detail  string `json:"detail,omitempty"`
}
