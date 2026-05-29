// Package service 是业务逻辑层（类比 Java @Service）。
package service

import (
	"context"
	"crypto/hmac"
	"crypto/sha256"
	"encoding/base64"
	"errors"
	"fmt"
	"strconv"
	"strings"
	"time"

	"github.com/google/uuid"
	"go.uber.org/zap"

	"micro-drama-video/internal/config"
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

// NotifyTranscode 校验原片已上传 OSS，写入 video_asset 并发送 Kafka 触发转码（兼容手动回调）。
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
	_ = in.UserID

	s.log.Info("notify transcode started", zap.String("videoId", videoID), zap.String("fileKey", fileKey))
	return s.triggerTranscode(ctx, videoID, fileKey, dramaID, episodeID, in.Etag, in.SizeBytes)
}

// --- 批量删除视频 ---

type DeleteVideoItem struct {
	VideoID string
	FileKey string // 资产未落库时用于删除 OSS 原片
}

type DeleteVideosInput struct {
	Items            []DeleteVideoItem
	PreserveRawPath  string // 不删除该 OSS 原片（同集覆盖上传时与最终 fileKey 相同）
	UserID           string
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

	preserveRaw := strings.TrimPrefix(strings.TrimSpace(in.PreserveRawPath), "/")

	out := &DeleteVideosOutput{Deleted: []string{}, Failed: []string{}}
	for _, id := range ids {
		if a, ok := assetByID[id]; ok {
			if err := s.deleteVideoAssetFully(ctx, a, preserveRaw); err != nil {
				out.Failed = append(out.Failed, id)
				continue
			}
			out.Deleted = append(out.Deleted, id)
			continue
		}
		if key := fileKeyByID[id]; key != "" && key != preserveRaw {
			if err := s.oss.RemoveObject(ctx, key); err != nil {
				s.log.Warn("oss remove failed", zap.String("objectKey", key), zap.Error(err))
			}
		}
		if err := s.repo.DeleteVideoAssetsByIDs(ctx, []string{id}); err != nil {
			out.Failed = append(out.Failed, id)
			continue
		}
		out.Deleted = append(out.Deleted, id)
	}
	if len(out.Deleted) == 0 && len(out.Failed) > 0 {
		return out, fmt.Errorf("delete video_asset failed")
	}
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

// PlayAuth 校验订单（预留）后返回 HLS 预签名播放地址；业务 token 单独返回，不可拼入 OSS URL（会破坏签名）。
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
	if base := strings.TrimRight(strings.TrimSpace(s.cfg.Playback.PublicBaseURL), "/"); base != "" {
		key := strings.TrimPrefix(strings.TrimSpace(hlsPath), "/")
		if key == "" {
			return "", fmt.Errorf("hls_path is empty")
		}
		return base + "/" + key, nil
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

// VerifyPlayToken 校验播放 token（供后续网关或 CDN 回调使用；勿拼入 OSS 预签名 URL）。
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
