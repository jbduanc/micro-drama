package service

import (
	"context"
	"strings"

	"go.uber.org/zap"

	"micro-drama-video/internal/model"
)

// deleteVideoAssetFully 删除 video_asset / transcode_task 记录及 OSS 原片、HLS 目录。
// preserveRawPath：同集多 videoId 共用一条 raw 路径时，清理旧记录但保留即将转码的原片。
func (s *VideoService) deleteVideoAssetFully(ctx context.Context, asset *model.VideoAsset, preserveRawPath string) error {
	if asset == nil || strings.TrimSpace(asset.ID) == "" {
		return nil
	}

	preserveRawPath = strings.TrimPrefix(strings.TrimSpace(preserveRawPath), "/")
	if key := strings.TrimSpace(asset.RawPath); key != "" && key != preserveRawPath {
		if err := s.oss.RemoveObject(ctx, key); err != nil {
			s.log.Warn("oss remove raw failed", zap.String("objectKey", key), zap.Error(err))
		}
	}

	if prefix := hlsOSSPrefix(s.resolveHLSPath(asset)); prefix != "" {
		if err := s.oss.RemovePrefix(ctx, prefix); err != nil {
			s.log.Warn("oss remove hls prefix failed", zap.String("prefix", prefix), zap.Error(err))
		}
	}

	if err := s.repo.DeleteVideoAssetsByIDs(ctx, []string{asset.ID}); err != nil {
		return err
	}
	s.log.Info("video asset removed", zap.String("videoId", asset.ID), zap.String("rawPath", asset.RawPath))
	return nil
}

// replaceEpisodeVideoByRawPath 同集重复上传前删除已有资产（任意状态均覆盖）。
func (s *VideoService) replaceEpisodeVideoByRawPath(ctx context.Context, fileKey string) error {
	existing, err := s.repo.GetVideoAssetByRawPath(ctx, fileKey)
	if err != nil {
		return err
	}
	s.log.Info("replacing episode video for new upload",
		zap.String("replacedVideoId", existing.ID),
		zap.String("fileKey", fileKey),
		zap.String("status", existing.Status),
	)
	return s.deleteVideoAssetFully(ctx, existing, "")
}

func hlsOSSPrefix(hlsPath string) string {
	hlsPath = strings.TrimSpace(hlsPath)
	if hlsPath == "" || strings.HasPrefix(strings.ToLower(hlsPath), "http") {
		return ""
	}
	if strings.HasSuffix(hlsPath, "/index.m3u8") {
		return strings.TrimSuffix(hlsPath, "index.m3u8")
	}
	if i := strings.LastIndex(hlsPath, "/"); i >= 0 {
		return hlsPath[:i+1]
	}
	return ""
}
