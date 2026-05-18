// Package model 定义与 PostgreSQL 表对应的领域模型（类比 Java Entity）。
package model

import "time"

// 视频资产状态，对应 video_asset.status 字段。
const (
	VideoStatusUploaded    = "UPLOADED"
	VideoStatusTranscoding = "TRANSCODING"
	VideoStatusReady       = "READY"
	VideoStatusFailed      = "FAILED"
)

// 转码任务状态，对应 transcode_task.status 字段。
const (
	TaskStatusPending = "PENDING"
	TaskStatusRunning = "RUNNING"
	TaskStatusSuccess = "SUCCESS"
	TaskStatusFailed  = "FAILED"
)

// VideoAsset 对应表 video_asset。
type VideoAsset struct {
	ID           string
	DramaID      *string
	EpisodeID    *string
	RawPath      string
	HlsPath      *string
	CoverPath    *string
	SubtitlePath *string
	Duration     *int
	SizeBytes    *int64
	Resolution   *string
	Status       string
	CreatedAt    time.Time
	UpdatedAt    time.Time
}

// TranscodeTask 对应表 transcode_task。
type TranscodeTask struct {
	ID           string
	VideoAssetID string
	InputPath    string
	OutputPath   string
	Status       string
	RetryCount   int
	ErrorMsg     *string
	CreatedAt    time.Time
	UpdatedAt    time.Time
}
