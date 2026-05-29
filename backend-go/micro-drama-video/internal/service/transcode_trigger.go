package service

import (
	"context"
	"errors"
	"fmt"
	"strings"
	"time"

	"go.uber.org/zap"

	"micro-drama-video/internal/events"
	"micro-drama-video/internal/model"
	"micro-drama-video/internal/repository"
	"micro-drama-video/internal/storage"
)

// triggerTranscode 校验 OSS 原片 → 落库/更新 → Kafka → 创建转码任务。
func (s *VideoService) triggerTranscode(
	ctx context.Context,
	videoID, fileKey, dramaID, episodeID, etag string,
	sizeBytes int64,
) (*CompleteUploadOutput, error) {
	videoID = strings.TrimSpace(videoID)
	fileKey = strings.TrimSpace(fileKey)
	dramaID = strings.TrimSpace(dramaID)
	episodeID = strings.TrimSpace(episodeID)
	if videoID == "" || fileKey == "" || dramaID == "" || episodeID == "" {
		return nil, fmt.Errorf("videoId, fileKey, dramaId and episodeId are required")
	}

	expectedKey := storage.BuildRawKey(s.cfg.OSS.UploadPrefix, dramaID, episodeID)
	if fileKey != expectedKey {
		return nil, fmt.Errorf("fileKey mismatch, expected %s", expectedKey)
	}

	size, ossEtag, err := s.oss.StatObject(ctx, fileKey)
	if err != nil {
		s.log.Error("oss stat object failed", zap.String("fileKey", fileKey), zap.Error(err))
		return nil, fmt.Errorf("object not found in storage, upload may have failed")
	}
	if sizeBytes > 0 {
		size = sizeBytes
	}
	if strings.TrimSpace(etag) != "" {
		ossEtag = strings.TrimSpace(etag)
	}

	asset, err := s.repo.GetVideoAssetByID(ctx, videoID)
	if err != nil && !errors.Is(err, repository.ErrVideoNotFound) {
		return nil, err
	}
	if asset == nil {
		if err := s.repo.CreateVideoAsset(ctx, repository.CreateVideoAssetParams{
			ID:        videoID,
			DramaID:   &dramaID,
			EpisodeID: &episodeID,
			RawPath:   fileKey,
			SizeBytes: size,
		}); err != nil {
			return nil, fmt.Errorf("insert video_asset: %w", err)
		}
	} else {
		if asset.Status == model.VideoStatusReady {
			return &CompleteUploadOutput{
				VideoID:         videoID,
				SourceObjectKey: fileKey,
				SourceEtag:      ossEtag,
			}, nil
		}
		if asset.Status == model.VideoStatusTranscoding {
			return &CompleteUploadOutput{
				VideoID:         videoID,
				SourceObjectKey: fileKey,
				SourceEtag:      ossEtag,
			}, nil
		}
	}

	outputPath := storage.BuildHLSKey(s.cfg.OSS.HLSPrefix, dramaID, episodeID)

	ev := &events.VideoUploadCompletedEvent{
		VideoID:         videoID,
		DramaID:         &dramaID,
		EpisodeID:       &episodeID,
		SourceObjectKey: fileKey,
		SourceEtag:      ossEtag,
		UploadedAt:      time.Now().UnixMilli(),
	}
	if err := s.kafka.PublishUploadCompleted(ev); err != nil {
		return nil, fmt.Errorf("kafka publish: %w", err)
	}

	taskID, err := s.repo.CreateTranscodeTask(ctx, repository.CreateTranscodeTaskParams{
		VideoAssetID: videoID,
		InputPath:    fileKey,
		OutputPath:   outputPath,
	})
	if err != nil {
		return nil, fmt.Errorf("insert transcode_task: %w", err)
	}

	s.log.Info("transcode triggered",
		zap.String("videoId", videoID),
		zap.String("transcodeTaskId", taskID),
		zap.String("fileKey", fileKey),
	)

	return &CompleteUploadOutput{
		VideoID:         videoID,
		SourceObjectKey: fileKey,
		SourceEtag:      ossEtag,
		TranscodeTaskID: taskID,
	}, nil
}
