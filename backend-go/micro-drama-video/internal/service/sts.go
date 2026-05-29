package service

import (
	"context"
	"errors"
	"fmt"
	"strings"

	"github.com/google/uuid"
	"go.uber.org/zap"

	"micro-drama-video/internal/model"
	"micro-drama-video/internal/repository"
	"micro-drama-video/internal/storage"
)

// STSUploadInput 申请 STS 直传凭证。
type STSUploadInput struct {
	DramaID     string
	EpisodeID   string
	ContentType string
	UserID      string
}

// STSUploadOutput 返回浏览器 ali-oss SDK 所需字段。
type STSUploadOutput struct {
	VideoID         string `json:"videoId"`
	FileKey         string `json:"fileKey"`
	AccessKeyID     string `json:"accessKeyId"`
	AccessKeySecret string `json:"accessKeySecret"`
	SecurityToken   string `json:"securityToken"`
	Expiration      string `json:"expiration"`
	ExpiresIn       int64  `json:"expiresIn"`
	Region          string `json:"region"`
	Bucket          string `json:"bucket"`
	Endpoint        string `json:"endpoint"`
	CallbackURL     string `json:"callbackUrl,omitempty"` // OSS 上传回调（无 MNS 区域必填）
}

// CreateSTSUploadCredentials 签发 STS 并预创建 UPLOADING 状态的 video_asset。
func (s *VideoService) CreateSTSUploadCredentials(ctx context.Context, in *STSUploadInput) (*STSUploadOutput, error) {
	if in == nil {
		return nil, fmt.Errorf("invalid request")
	}
	dramaID := strings.TrimSpace(in.DramaID)
	episodeID := strings.TrimSpace(in.EpisodeID)
	if dramaID == "" || episodeID == "" {
		return nil, fmt.Errorf("dramaId and episodeId are required")
	}
	_ = in.UserID

	videoID := uuid.NewString()
	fileKey := storage.BuildRawKey(s.cfg.OSS.UploadPrefix, dramaID, episodeID)

	existing, err := s.repo.GetVideoAssetByRawPath(ctx, fileKey)
	if err != nil && !errors.Is(err, repository.ErrVideoNotFound) {
		return nil, err
	}
	if existing != nil {
		switch existing.Status {
		case model.VideoStatusReady, model.VideoStatusTranscoding:
			return nil, fmt.Errorf("video already exists for this episode, delete it first")
		default:
			if err := s.repo.DeleteVideoAssetsByIDs(ctx, []string{existing.ID}); err != nil {
				return nil, fmt.Errorf("replace pending video: %w", err)
			}
		}
	}

	callbackToken, err := storage.NewCallbackToken()
	if err != nil {
		return nil, err
	}
	token := callbackToken

	if err := s.repo.CreatePendingVideoAsset(ctx, repository.CreateVideoAssetParams{
		ID:            videoID,
		DramaID:       &dramaID,
		EpisodeID:     &episodeID,
		RawPath:       fileKey,
		SizeBytes:     0,
		CallbackToken: &token,
	}); err != nil {
		return nil, fmt.Errorf("create pending video_asset: %w", err)
	}

	creds, err := storage.AssumeRoleForObject(s.cfg, fileKey)
	if err != nil {
		return nil, err
	}

	region := storage.NormalizeOSSRegion(s.cfg.OSS.Region, s.cfg.OSS.Bucket)
	if region == "" {
		region = storage.NormalizeOSSRegion(s.cfg.OSS.STSRegion, s.cfg.OSS.Bucket)
	}

	expireSec := int64(s.cfg.OSS.STSDurationSeconds)
	callbackURL := storage.BuildUploadCallbackURL(s.cfg, videoID, token)
	if callbackURL == "" {
		s.log.Warn("oss_upload_callback_base_url is empty; OSS upload callback disabled, rely on notify-transcode",
			zap.String("videoId", videoID),
		)
	}
	s.log.Info("sts credentials issued",
		zap.String("videoId", videoID),
		zap.String("fileKey", fileKey),
		zap.String("dramaId", dramaID),
		zap.String("episodeId", episodeID),
		zap.Bool("callbackEnabled", callbackURL != ""),
	)

	return &STSUploadOutput{
		VideoID:         videoID,
		FileKey:         fileKey,
		AccessKeyID:     creds.AccessKeyID,
		AccessKeySecret: creds.AccessKeySecret,
		SecurityToken:   creds.SecurityToken,
		Expiration:      creds.Expiration.UTC().Format("2006-01-02T15:04:05Z"),
		ExpiresIn:       expireSec,
		Region:          region,
		Bucket:          s.cfg.OSS.Bucket,
		Endpoint:        storage.BuildBucketEndpoint(s.cfg),
		CallbackURL:     callbackURL,
	}, nil
}
