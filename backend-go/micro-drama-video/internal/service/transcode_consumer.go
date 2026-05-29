package service

import (
	"context"
	"encoding/json"
	"strings"

	"go.uber.org/zap"

	"micro-drama-video/internal/events"
)

// HandleTranscodeCompleted 消费 video.transcode.completed，将视频标记为可播放。
func (s *VideoService) HandleTranscodeCompleted(ctx context.Context, payload []byte) error {
	var ev events.VideoTranscodeCompletedEvent
	if err := json.Unmarshal(payload, &ev); err != nil {
		return err
	}
	videoID := strings.TrimSpace(ev.VideoID)
	hlsPath := strings.TrimSpace(ev.MasterPlaylistKey)
	if videoID == "" || hlsPath == "" {
		return nil
	}
	if err := s.repo.MarkTranscodeSuccess(ctx, videoID, hlsPath); err != nil {
		s.log.Error("mark transcode success failed", zap.String("videoId", videoID), zap.Error(err))
		return err
	}
	s.log.Info("video ready for playback", zap.String("videoId", videoID), zap.String("hlsPath", hlsPath))
	return nil
}

// HandleTranscodeFailed 消费 video.transcode.failed，更新失败状态。
func (s *VideoService) HandleTranscodeFailed(ctx context.Context, payload []byte) error {
	var ev events.VideoTranscodeFailedEvent
	if err := json.Unmarshal(payload, &ev); err != nil {
		return err
	}
	videoID := strings.TrimSpace(ev.VideoID)
	if videoID == "" {
		return nil
	}
	errMsg := strings.TrimSpace(ev.Reason)
	if ev.Detail != "" {
		errMsg = errMsg + ": " + ev.Detail
	}
	if err := s.repo.MarkTranscodeFailed(ctx, videoID, errMsg); err != nil {
		s.log.Error("mark transcode failed", zap.String("videoId", videoID), zap.Error(err))
		return err
	}
	s.log.Warn("video transcode failed", zap.String("videoId", videoID), zap.String("reason", errMsg))
	return nil
}
