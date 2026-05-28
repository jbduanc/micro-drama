// Package service 是业务逻辑层（类比 Java @Service）。
package service

import (
	"context"
	"crypto/hmac"
	"crypto/sha256"
	"encoding/base64"
	"errors"
	"fmt"
	"net/url"
	"strconv"
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

// VideoService 视频相关业务实现。
type VideoService struct {
	log   *zap.Logger
	cfg   *config.Config
	oss   *storage.OSS
	kafka *kafka.Producer
	repo  *repository.VideoRepo
}

// NewVideoService 构造 VideoService。
func NewVideoService(log *zap.Logger, cfg *config.Config, oss *storage.OSS, producer *kafka.Producer, repo *repository.VideoRepo) *VideoService {
	return &VideoService{log: log, cfg: cfg, oss: oss, kafka: producer, repo: repo}
}

// --- 直传：申请预签名 URL ---

type UploadURLInput struct {
	DramaID     string
	EpisodeID   string
	ContentType string
	UserID      string
}

type UploadURLOutput struct {
	VideoID   string `json:"videoId"`
	UploadURL string `json:"uploadUrl"`
	FileKey   string `json:"fileKey"`
	ExpiresIn int64  `json:"expiresIn"`
}

// CreateUploadURL 生成 raw/{dramaId}/{episodeId}.mp4 的 PUT 预签名 URL，前端直传 OSS。
func (s *VideoService) CreateUploadURL(ctx context.Context, in *UploadURLInput) (*UploadURLOutput, error) {
	if in == nil {
		return nil, fmt.Errorf("invalid request")
	}
	dramaID := strings.TrimSpace(in.DramaID)
	episodeID := strings.TrimSpace(in.EpisodeID)
	if dramaID == "" || episodeID == "" {
		return nil, fmt.Errorf("dramaId and episodeId are required")
	}
	_ = in.UserID // TODO: JWT 鉴权

	videoID := uuid.NewString()
	fileKey := storage.BuildRawKey(s.cfg.OSS.UploadPrefix, dramaID, episodeID)
	contentType := strings.TrimSpace(in.ContentType)
	if contentType == "" {
		contentType = "video/mp4"
	}

	expireSec := s.cfg.OSS.UploadPresignExpireSeconds
	uploadURL, err := s.oss.PresignPut(ctx, fileKey, contentType, time.Duration(expireSec)*time.Second)
	if err != nil {
		return nil, fmt.Errorf("presign upload url: %w", err)
	}

	s.log.Info("upload url created",
		zap.String("videoId", videoID),
		zap.String("fileKey", fileKey),
		zap.String("dramaId", dramaID),
		zap.String("episodeId", episodeID),
	)

	return &UploadURLOutput{
		VideoID:   videoID,
		UploadURL: uploadURL,
		FileKey:   fileKey,
		ExpiresIn: int64(expireSec),
	}, nil
}

// --- 直传：上传完成回调 ---

type CompleteUploadInput struct {
	VideoID   string
	FileKey   string
	DramaID   string
	EpisodeID string
	Etag      string
	SizeBytes int64
	UserID    string
}

type CompleteUploadOutput struct {
	VideoID         string `json:"videoId"`
	SourceObjectKey string `json:"sourceObjectKey"`
	SourceEtag      string `json:"sourceEtag,omitempty"`
	TranscodeTaskID string `json:"transcodeTaskId,omitempty"`
}

// CompleteUpload 直传 OSS 后的完成回调（与 NotifyTranscode 相同逻辑，保留兼容旧客户端）。
func (s *VideoService) CompleteUpload(ctx context.Context, in *CompleteUploadInput) (*CompleteUploadOutput, error) {
	return s.NotifyTranscode(ctx, in)
}

// NotifyTranscodeInput 通知转码（管理端保存剧集时调用）：校验 OSS → 落库 → Kafka → 转码任务。
type NotifyTranscodeInput = CompleteUploadInput

// NotifyTranscode 校验原片已上传 OSS，写入 video_asset 并发送 Kafka 触发转码。
func (s *VideoService) NotifyTranscode(ctx context.Context, in *NotifyTranscodeInput) (*CompleteUploadOutput, error) {
	if in == nil {
		return nil, fmt.Errorf("invalid request")
	}
	videoID := strings.TrimSpace(in.VideoID)
	fileKey := strings.TrimSpace(in.FileKey)
	dramaID := strings.TrimSpace(in.DramaID)
	episodeID := strings.TrimSpace(in.EpisodeID)
	if videoID == "" || fileKey == "" || dramaID == "" || episodeID == "" {
		return nil, fmt.Errorf("videoId, fileKey, dramaId and episodeId are required")
	}
	_ = in.UserID // TODO: JWT 鉴权

	expectedKey := storage.BuildRawKey(s.cfg.OSS.UploadPrefix, dramaID, episodeID)
	if fileKey != expectedKey {
		return nil, fmt.Errorf("fileKey mismatch, expected %s", expectedKey)
	}

	size, etag, err := s.oss.StatObject(ctx, fileKey)
	if err != nil {
		s.log.Error("oss stat object failed", zap.String("fileKey", fileKey), zap.Error(err))
		return nil, fmt.Errorf("object not found in storage, upload may have failed")
	}
	if in.SizeBytes > 0 {
		size = in.SizeBytes
	}
	if strings.TrimSpace(in.Etag) != "" {
		etag = strings.TrimSpace(in.Etag)
	}

	s.log.Info("notify transcode started",
		zap.String("videoId", videoID),
		zap.String("fileKey", fileKey),
		zap.Int64("sizeBytes", size),
	)

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
			s.log.Error("insert video_asset failed", zap.String("videoId", videoID), zap.Error(err))
			return nil, fmt.Errorf("insert video_asset: %w", err)
		}
	} else if asset.Status == model.VideoStatusReady {
		s.log.Info("notify transcode skipped: video already ready", zap.String("videoId", videoID))
		return &CompleteUploadOutput{
			VideoID:         videoID,
			SourceObjectKey: fileKey,
			SourceEtag:      etag,
		}, nil
	}

	outputPath := storage.BuildHLSKey(s.cfg.OSS.HLSPrefix, dramaID, episodeID)

	ev := &events.VideoUploadCompletedEvent{
		VideoID:         videoID,
		DramaID:         &dramaID,
		EpisodeID:       &episodeID,
		SourceObjectKey: fileKey,
		SourceEtag:      etag,
		UploadedAt:      time.Now().UnixMilli(),
	}
	if err := s.kafka.PublishUploadCompleted(ev); err != nil {
		s.log.Error("kafka publish failed", zap.String("videoId", videoID), zap.Error(err))
		return nil, fmt.Errorf("kafka publish: %w", err)
	}

	taskID, err := s.repo.CreateTranscodeTask(ctx, repository.CreateTranscodeTaskParams{
		VideoAssetID: videoID,
		InputPath:    fileKey,
		OutputPath:   outputPath,
	})
	if err != nil {
		s.log.Error("insert transcode_task failed", zap.String("videoId", videoID), zap.Error(err))
		return nil, fmt.Errorf("insert transcode_task: %w", err)
	}

	s.log.Info("notify transcode finished",
		zap.String("videoId", videoID),
		zap.String("transcodeTaskId", taskID),
		zap.String("expectedHlsPath", outputPath),
	)

	return &CompleteUploadOutput{
		VideoID:         videoID,
		SourceObjectKey: fileKey,
		SourceEtag:      etag,
		TranscodeTaskID: taskID,
	}, nil
}

// --- 批量删除视频 ---

type DeleteVideoItem struct {
	VideoID string
	FileKey string // 资产未落库时用于删除 OSS 原片
}

type DeleteVideosInput struct {
	Items  []DeleteVideoItem
	UserID string
}

type DeleteVideosOutput struct {
	Deleted []string `json:"deleted"`
	Failed  []string `json:"failed,omitempty"`
}

// DeleteVideos 批量删除视频：OSS 原片/HLS + 数据库记录。
func (s *VideoService) DeleteVideos(ctx context.Context, in *DeleteVideosInput) (*DeleteVideosOutput, error) {
	if in == nil || len(in.Items) == 0 {
		return nil, fmt.Errorf("items are required")
	}
	_ = in.UserID

	ids := make([]string, 0, len(in.Items))
	fileKeyByID := make(map[string]string, len(in.Items))
	for _, it := range in.Items {
		id := strings.TrimSpace(it.VideoID)
		if id == "" {
			continue
		}
		ids = append(ids, id)
		if k := strings.TrimSpace(it.FileKey); k != "" {
			fileKeyByID[id] = k
		}
	}
	if len(ids) == 0 {
		return nil, fmt.Errorf("videoId is required in items")
	}

	assets, err := s.repo.ListVideoAssetsByIDs(ctx, ids)
	if err != nil {
		return nil, err
	}
	assetByID := make(map[string]*model.VideoAsset, len(assets))
	for _, a := range assets {
		assetByID[a.ID] = a
	}

	out := &DeleteVideosOutput{Deleted: []string{}, Failed: []string{}}
	for _, id := range ids {
		if a, ok := assetByID[id]; ok {
			keys := []string{a.RawPath}
			if p := s.resolveHLSPath(a); p != "" && !strings.HasPrefix(strings.ToLower(p), "http") {
				keys = append(keys, p)
			}
			for _, key := range keys {
				if err := s.oss.RemoveObject(ctx, key); err != nil {
					s.log.Warn("oss remove failed", zap.String("objectKey", key), zap.Error(err))
				}
			}
		} else if key := fileKeyByID[id]; key != "" {
			if err := s.oss.RemoveObject(ctx, key); err != nil {
				s.log.Warn("oss remove failed", zap.String("objectKey", key), zap.Error(err))
			}
		}
	}

	if err := s.repo.DeleteVideoAssetsByIDs(ctx, ids); err != nil {
		return nil, fmt.Errorf("delete video_asset: %w", err)
	}
	out.Deleted = ids
	s.log.Info("videos deleted", zap.Strings("videoIds", ids))
	return out, nil
}

// --- 播放鉴权 ---

type PlayInput struct {
	VideoID string
	OrderID string
	UserID  string
}

type PlayOutput struct {
	VideoID   string `json:"videoId"`
	PlayURL   string `json:"playUrl"`
	Token     string `json:"token"`
	ExpiresIn int64  `json:"expiresIn"`
	Status    string `json:"status"`
	HlsPath   string `json:"hlsPath"`
}

// PlayAuth 校验订单（预留）后返回带 token 的 HLS 预签名播放地址。
func (s *VideoService) PlayAuth(ctx context.Context, in *PlayInput) (*PlayOutput, error) {
	if in == nil || strings.TrimSpace(in.VideoID) == "" {
		return nil, fmt.Errorf("videoId is required")
	}
	userID := strings.TrimSpace(in.UserID)

	if err := s.validateOrder(ctx, userID, in.OrderID, in.VideoID); err != nil {
		return nil, err
	}

	s.log.Info("play auth started", zap.String("videoId", in.VideoID), zap.String("orderId", in.OrderID))

	asset, err := s.repo.GetVideoAssetByID(ctx, in.VideoID)
	if err != nil {
		if errors.Is(err, repository.ErrVideoNotFound) {
			return nil, fmt.Errorf("video not found")
		}
		return nil, err
	}

	if s.cfg.Playback.RequireReady && asset.Status != model.VideoStatusReady {
		return nil, fmt.Errorf("video status is %s, not ready for playback", asset.Status)
	}

	hlsPath := s.resolveHLSPath(asset)
	if hlsPath == "" {
		return nil, fmt.Errorf("hls_path is empty, video may still be transcoding")
	}

	expireSec := s.cfg.Playback.URLExpireSeconds
	playURL, err := s.resolvePlayURL(ctx, hlsPath)
	if err != nil {
		return nil, err
	}

	token := s.signPlayToken(in.VideoID, userID, expireSec)
	playURL = appendQueryParam(playURL, "token", token)

	s.log.Info("play auth succeeded",
		zap.String("videoId", in.VideoID),
		zap.String("status", asset.Status),
		zap.Int64("expiresIn", int64(expireSec)),
	)

	return &PlayOutput{
		VideoID:   in.VideoID,
		PlayURL:   playURL,
		Token:     token,
		ExpiresIn: int64(expireSec),
		Status:    asset.Status,
		HlsPath:   hlsPath,
	}, nil
}

// validateOrder 校验用户是否已购买/有权播放（预留，对接支付服务）。
func (s *VideoService) validateOrder(ctx context.Context, userID, orderID, videoID string) error {
	_ = ctx
	_ = userID
	_ = orderID
	_ = videoID
	// TODO: 调用 micro-drama-payment 校验订单支付状态
	// if orderID == "" { return fmt.Errorf("orderId is required") }
	return nil
}

func (s *VideoService) resolveHLSPath(asset *model.VideoAsset) string {
	if asset.HlsPath != nil && strings.TrimSpace(*asset.HlsPath) != "" {
		return strings.TrimSpace(*asset.HlsPath)
	}
	if asset.DramaID != nil && asset.EpisodeID != nil {
		return storage.BuildHLSKey(s.cfg.OSS.HLSPrefix, *asset.DramaID, *asset.EpisodeID)
	}
	return ""
}

func (s *VideoService) resolvePlayURL(ctx context.Context, hlsPath string) (string, error) {
	lower := strings.ToLower(hlsPath)
	if strings.HasPrefix(lower, "http://") || strings.HasPrefix(lower, "https://") {
		return hlsPath, nil
	}
	expire := time.Duration(s.cfg.Playback.URLExpireSeconds) * time.Second
	u, err := s.oss.PresignGet(ctx, hlsPath, expire)
	if err != nil {
		return "", fmt.Errorf("presign play url: %w", err)
	}
	return u, nil
}

func (s *VideoService) signPlayToken(videoID, userID string, expireSec int) string {
	secret := strings.TrimSpace(s.cfg.Playback.TokenSecret)
	if secret == "" {
		secret = "micro-drama-play-token-dev"
	}
	exp := time.Now().Add(time.Duration(expireSec) * time.Second).Unix()
	payload := fmt.Sprintf("%s|%s|%d", videoID, userID, exp)
	mac := hmac.New(sha256.New, []byte(secret))
	mac.Write([]byte(payload))
	sig := base64.RawURLEncoding.EncodeToString(mac.Sum(nil))
	return fmt.Sprintf("%s.%s", base64.RawURLEncoding.EncodeToString([]byte(payload)), sig)
}

func appendQueryParam(rawURL, key, value string) string {
	u, err := url.Parse(rawURL)
	if err != nil {
		return rawURL + "&" + key + "=" + url.QueryEscape(value)
	}
	q := u.Query()
	q.Set(key, value)
	u.RawQuery = q.Encode()
	return u.String()
}

// VerifyPlayToken 校验播放 token（供后续网关或 CDN 回调使用，当前播放接口已内嵌签发）。
func (s *VideoService) VerifyPlayToken(token, videoID, userID string) bool {
	secret := strings.TrimSpace(s.cfg.Playback.TokenSecret)
	if secret == "" {
		secret = "micro-drama-play-token-dev"
	}
	parts := strings.Split(token, ".")
	if len(parts) != 2 {
		return false
	}
	payloadBytes, err := base64.RawURLEncoding.DecodeString(parts[0])
	if err != nil {
		return false
	}
	sigBytes, err := base64.RawURLEncoding.DecodeString(parts[1])
	if err != nil {
		return false
	}
	mac := hmac.New(sha256.New, []byte(secret))
	mac.Write(payloadBytes)
	if !hmac.Equal(sigBytes, mac.Sum(nil)) {
		return false
	}
	seg := strings.Split(string(payloadBytes), "|")
	if len(seg) != 3 {
		return false
	}
	if seg[0] != videoID {
		return false
	}
	if userID != "" && seg[1] != userID {
		return false
	}
	exp, err := strconv.ParseInt(seg[2], 10, 64)
	if err != nil || time.Now().Unix() > exp {
		return false
	}
	return true
}
