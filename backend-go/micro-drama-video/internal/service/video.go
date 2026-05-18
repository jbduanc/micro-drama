// Package service 是业务逻辑层（类比 Java @Service）。
//
// 编排 OSS 上传、PostgreSQL 落库、Kafka 通知、播放鉴权等流程。
package service

import (
	"context"
	"errors"
	"fmt"
	"io"
	"path"
	"strings"
	"time"

	"github.com/google/uuid"
	"go.uber.org/zap"

	"micro-drama-video/internal/config"
	"micro-drama-video/internal/events"
	"micro-drama-video/internal/kafka"
	"micro-drama-video/internal/model"
	"micro-drama-video/internal/repository"
	"micro-drama-video/internal/storage"
)

// VideoService 视频相关业务实现（依赖注入：构造时传入 OSS/Kafka/Repo）。
type VideoService struct {
	log   *zap.Logger
	cfg   *config.Config
	oss   *storage.OSS
	kafka *kafka.Producer
	repo  *repository.VideoRepo
}

// NewVideoService 构造 VideoService，由 main 在启动时调用一次。
func NewVideoService(log *zap.Logger, cfg *config.Config, oss *storage.OSS, producer *kafka.Producer, repo *repository.VideoRepo) *VideoService {
	return &VideoService{log: log, cfg: cfg, oss: oss, kafka: producer, repo: repo}
}

// UploadInput 上传接口入参。
type UploadInput struct {
	FileName    string
	ContentType string
	Size        int64
	Reader      io.Reader
	UserID      string
	DramaID     *string
	EpisodeID   *string
}

// UploadOutput 上传成功返回给前端。
type UploadOutput struct {
	VideoID         string `json:"videoId"`
	SourceObjectKey string `json:"sourceObjectKey"`
	SourceEtag      string `json:"sourceEtag,omitempty"`
	TranscodeTaskID string `json:"transcodeTaskId,omitempty"`
}

// Upload 完整上传流程：OSS → video_asset → Kafka → transcode_task。
func (s *VideoService) Upload(ctx context.Context, in *UploadInput) (*UploadOutput, error) {
	if in == nil || in.Reader == nil {
		return nil, fmt.Errorf("file is required")
	}
	_ = in.UserID // TODO: JWT 鉴权

	videoID := uuid.NewString()
	objectKey := storage.BuildUploadKey(s.cfg.OSS.UploadPrefix, videoID, sanitizeFileName(in.FileName))

	s.log.Info("upload started",
		zap.String("videoId", videoID),
		zap.String("fileName", in.FileName),
		zap.Int64("sizeBytes", in.Size),
		zap.String("objectKey", objectKey),
		zap.Stringp("dramaId", in.DramaID),
		zap.Stringp("episodeId", in.EpisodeID),
	)

	etag, err := s.oss.PutObject(ctx, objectKey, in.Reader, in.Size, in.ContentType)
	if err != nil {
		s.log.Error("oss put object failed", zap.String("videoId", videoID), zap.Error(err))
		return nil, fmt.Errorf("oss upload: %w", err)
	}
	s.log.Info("oss put object succeeded",
		zap.String("videoId", videoID),
		zap.String("objectKey", objectKey),
		zap.String("etag", etag),
	)

	if err := s.repo.CreateVideoAsset(ctx, repository.CreateVideoAssetParams{
		ID:        videoID,
		DramaID:   in.DramaID,
		EpisodeID: in.EpisodeID,
		RawPath:   objectKey,
		SizeBytes: in.Size,
	}); err != nil {
		s.log.Error("insert video_asset failed", zap.String("videoId", videoID), zap.Error(err))
		return nil, fmt.Errorf("insert video_asset: %w", err)
	}
	s.log.Info("video_asset created",
		zap.String("videoId", videoID),
		zap.String("status", model.VideoStatusUploaded),
	)

	outputPath := strings.ReplaceAll(s.cfg.Playback.HLSKeyTemplate, "{videoId}", videoID)

	ev := &events.VideoUploadCompletedEvent{
		VideoID:         videoID,
		SourceObjectKey: objectKey,
		SourceEtag:      etag,
		UploadedAt:      time.Now().UnixMilli(),
	}
	if err := s.kafka.PublishUploadCompleted(ev); err != nil {
		s.log.Error("kafka publish failed", zap.String("videoId", videoID), zap.Error(err))
		return nil, fmt.Errorf("kafka publish: %w", err)
	}

	taskID, err := s.repo.CreateTranscodeTask(ctx, repository.CreateTranscodeTaskParams{
		VideoAssetID: videoID,
		InputPath:    objectKey,
		OutputPath:   outputPath,
	})
	if err != nil {
		s.log.Error("insert transcode_task failed", zap.String("videoId", videoID), zap.Error(err))
		return nil, fmt.Errorf("insert transcode_task: %w", err)
	}

	s.log.Info("upload completed",
		zap.String("videoId", videoID),
		zap.String("transcodeTaskId", taskID),
		zap.String("expectedHlsPath", outputPath),
	)

	return &UploadOutput{
		VideoID:         videoID,
		SourceObjectKey: objectKey,
		SourceEtag:      etag,
		TranscodeTaskID: taskID,
	}, nil
}

// PlayInput 播放鉴权入参。
type PlayInput struct {
	VideoID string
	UserID  string
}

// PlayOutput 播放鉴权成功返回。
type PlayOutput struct {
	VideoID   string `json:"videoId"`
	PlayURL   string `json:"playUrl"`
	ExpiresIn int64  `json:"expiresIn"`
	Status    string `json:"status"`
	HlsPath   string `json:"hlsPath"`
}

// PlayAuth 鉴权后读取 hls_path，必要时签发 OSS 预签名 URL。
func (s *VideoService) PlayAuth(ctx context.Context, in *PlayInput) (*PlayOutput, error) {
	if in == nil || strings.TrimSpace(in.VideoID) == "" {
		return nil, fmt.Errorf("videoId is required")
	}
	_ = in.UserID // TODO: 权益校验

	s.log.Info("play auth started", zap.String("videoId", in.VideoID))

	asset, err := s.repo.GetVideoAssetByID(ctx, in.VideoID)
	if err != nil {
		if errors.Is(err, repository.ErrVideoNotFound) {
			s.log.Warn("play auth video not found", zap.String("videoId", in.VideoID))
			return nil, fmt.Errorf("video not found")
		}
		s.log.Error("play auth query failed", zap.String("videoId", in.VideoID), zap.Error(err))
		return nil, err
	}

	if s.cfg.Playback.RequireReady && asset.Status != model.VideoStatusReady {
		s.log.Warn("play auth video not ready",
			zap.String("videoId", in.VideoID),
			zap.String("status", asset.Status),
		)
		return nil, fmt.Errorf("video status is %s, not ready for playback", asset.Status)
	}
	if asset.HlsPath == nil || strings.TrimSpace(*asset.HlsPath) == "" {
		s.log.Warn("play auth hls_path empty", zap.String("videoId", in.VideoID), zap.String("status", asset.Status))
		return nil, fmt.Errorf("hls_path is empty, video may still be transcoding")
	}

	hlsPath := strings.TrimSpace(*asset.HlsPath)
	playURL, err := s.resolvePlayURL(ctx, hlsPath)
	if err != nil {
		s.log.Error("play auth presign failed", zap.String("videoId", in.VideoID), zap.Error(err))
		return nil, err
	}

	expire := int64(s.cfg.Playback.URLExpireSeconds)
	s.log.Info("play auth succeeded",
		zap.String("videoId", in.VideoID),
		zap.String("status", asset.Status),
		zap.Int64("expiresIn", expire),
	)

	return &PlayOutput{
		VideoID:   in.VideoID,
		PlayURL:   playURL,
		ExpiresIn: expire,
		Status:    asset.Status,
		HlsPath:   hlsPath,
	}, nil
}

// resolvePlayURL 若 hls_path 已是 http(s) 则直接返回，否则按 OSS object key 预签名。
func (s *VideoService) resolvePlayURL(ctx context.Context, hlsPath string) (string, error) {
	lower := strings.ToLower(hlsPath)
	if strings.HasPrefix(lower, "http://") || strings.HasPrefix(lower, "https://") {
		return hlsPath, nil
	}
	expire := time.Duration(s.cfg.Playback.URLExpireSeconds) * time.Second
	url, err := s.oss.PresignGet(ctx, hlsPath, expire)
	if err != nil {
		return "", fmt.Errorf("presign play url: %w", err)
	}
	return url, nil
}

func sanitizeFileName(name string) string {
	name = path.Base(strings.TrimSpace(name))
	if name == "" || name == "." {
		return "video.mp4"
	}
	return name
}
