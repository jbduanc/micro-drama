package service

import (
	"context"
	"fmt"
	"io"
	"mime"
	"os"
	"path/filepath"
	"strings"

	"go.uber.org/zap"

	"micro-drama-transcoder/internal/config"
	"micro-drama-transcoder/internal/events"
	"micro-drama-transcoder/internal/kafka"
	"micro-drama-transcoder/internal/storage"
	"micro-drama-transcoder/internal/transcode"
)

type Service struct {
	log  *zap.Logger
	cfg  *config.Config
	oss  *storage.OSS
	prod *kafka.Producer
}

func New(log *zap.Logger, cfg *config.Config, oss *storage.OSS, prod *kafka.Producer) *Service {
	return &Service{log: log, cfg: cfg, oss: oss, prod: prod}
}

func (s *Service) HandleUploadCompleted(ctx context.Context, ev *events.VideoUploadCompletedEvent) error {
	if ev == nil || strings.TrimSpace(ev.VideoID) == "" || strings.TrimSpace(ev.SourceObjectKey) == "" {
		return fmt.Errorf("invalid event")
	}

	dramaID := "unknown"
	episodeID := "unknown"
	if ev.DramaID != nil && strings.TrimSpace(*ev.DramaID) != "" {
		dramaID = strings.TrimSpace(*ev.DramaID)
	}
	if ev.EpisodeID != nil && strings.TrimSpace(*ev.EpisodeID) != "" {
		episodeID = strings.TrimSpace(*ev.EpisodeID)
	}

	workDir, err := os.MkdirTemp(s.cfg.Transcode.TempDir, "micro-drama-transcode-*")
	if err != nil {
		return err
	}
	if !s.cfg.Transcode.KeepWorkDir {
		defer os.RemoveAll(workDir)
	}

	inputPath := filepath.Join(workDir, "input.mp4")
	if err := s.downloadToFile(ctx, ev.SourceObjectKey, inputPath); err != nil {
		_ = s.prod.PublishTranscodeFailed(&events.VideoTranscodeFailedEvent{
			VideoID: ev.VideoID, Reason: "oss_download", Detail: err.Error(),
		})
		return err
	}
	if info, err := os.Stat(inputPath); err != nil || info.Size() < 1024 {
		detail := "downloaded file missing or too small (<1KB), upload may be incomplete"
		if err != nil {
			detail = err.Error()
		}
		_ = s.prod.PublishTranscodeFailed(&events.VideoTranscodeFailedEvent{
			VideoID: ev.VideoID, Reason: "invalid_input", Detail: detail,
		})
		return fmt.Errorf("%s", detail)
	}

	outDir := filepath.Join(workDir, "hls")
	masterFile, vars, err := transcode.TranscodeToHLS(ctx, s.log, s.cfg, inputPath, outDir)
	if err != nil {
		_ = s.prod.PublishTranscodeFailed(&events.VideoTranscodeFailedEvent{
			VideoID: ev.VideoID, Reason: "ffmpeg", Detail: err.Error(),
		})
		return err
	}

	remoteBase := storage.Join(s.cfg.Transcode.HlsPrefix, dramaID, episodeID)
	masterKey := storage.Join(remoteBase, "index.m3u8")
	if err := s.uploadDir(ctx, outDir, remoteBase); err != nil {
		_ = s.prod.PublishTranscodeFailed(&events.VideoTranscodeFailedEvent{
			VideoID: ev.VideoID, Reason: "oss_upload", Detail: err.Error(),
		})
		return err
	}

	_ = masterFile // local; remote uses masterKey
	variants := make([]events.TranscodeVariant, 0, len(vars))
	for _, v := range vars {
		variants = append(variants, events.TranscodeVariant{
			Width:       int32(v.Width),
			Height:      int32(v.Height),
			BitrateKbps: int32(v.BitrateKbps),
			PlaylistKey: storage.Join(remoteBase, filepath.ToSlash(filepath.Join(v.Name, "index.m3u8"))),
		})
	}
	_ = s.prod.PublishTranscodeCompleted(&events.VideoTranscodeCompletedEvent{
		VideoID:           ev.VideoID,
		MasterPlaylistKey: masterKey,
		Variants:          variants,
	})

	s.log.Info("transcode completed",
		zap.String("videoId", ev.VideoID),
		zap.String("masterKey", masterKey),
		zap.String("remoteBase", remoteBase),
		zap.Bool("keepWorkDir", s.cfg.Transcode.KeepWorkDir),
	)
	return nil
}

func (s *Service) downloadToFile(ctx context.Context, objectKey, dstPath string) error {
	rc, err := s.oss.GetObject(ctx, objectKey)
	if err != nil {
		return err
	}
	defer rc.Close()

	f, err := os.Create(dstPath)
	if err != nil {
		return err
	}
	defer f.Close()
	_, err = io.Copy(f, rc)
	return err
}

func (s *Service) uploadDir(ctx context.Context, localDir, remoteBase string) error {
	return filepath.Walk(localDir, func(p string, info os.FileInfo, err error) error {
		if err != nil {
			return err
		}
		if info.IsDir() {
			return nil
		}
		rel, err := filepath.Rel(localDir, p)
		if err != nil {
			return err
		}
		rel = filepath.ToSlash(rel)
		key := storage.Join(remoteBase, rel)

		ct := contentTypeByExt(p)
		f, err := os.Open(p)
		if err != nil {
			return err
		}
		defer f.Close()
		_, err = s.oss.PutObject(ctx, key, f, info.Size(), ct)
		return err
	})
}

func contentTypeByExt(path string) string {
	ext := strings.ToLower(filepath.Ext(path))
	switch ext {
	case ".m3u8":
		return "application/vnd.apple.mpegurl"
	case ".ts":
		return "video/mp2t"
	default:
		if ct := mime.TypeByExtension(ext); ct != "" {
			return ct
		}
		return "application/octet-stream"
	}
}
