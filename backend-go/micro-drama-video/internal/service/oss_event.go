package service

import (
	"context"
	"encoding/json"
	"errors"
	"fmt"
	"net/url"
	"strconv"
	"strings"

	"go.uber.org/zap"

	"micro-drama-video/internal/model"
	"micro-drama-video/internal/repository"
)

// OSSEventInput OSS 事件通知 / 上传回调入参。
type OSSEventInput struct {
	Bucket    string
	ObjectKey string
	EventName string
	Etag      string
	SizeBytes int64
	VideoID   string // 上传回调 customValue / query
	Secret    string // query 中的一次性 callback token
}

// HandleOSSEvent 处理 OSS ObjectCreated 类事件，自动触发转码（无需前端调用 notify-transcode）。
func (s *VideoService) HandleOSSEvent(ctx context.Context, in *OSSEventInput) (*CompleteUploadOutput, error) {
	if in == nil {
		return nil, fmt.Errorf("invalid request")
	}

	objectKey := strings.TrimPrefix(strings.TrimSpace(in.ObjectKey), "/")
	if objectKey == "" {
		return nil, fmt.Errorf("objectKey is required")
	}
	eventName := strings.TrimSpace(in.EventName)
	if eventName != "" && !strings.Contains(eventName, "ObjectCreated") {
		s.log.Info("oss event ignored", zap.String("eventName", eventName))
		return &CompleteUploadOutput{SourceObjectKey: objectKey}, nil
	}

	prefix := strings.Trim(s.cfg.OSS.UploadPrefix, "/")
	if prefix != "" && !strings.HasPrefix(objectKey, prefix+"/") {
		s.log.Debug("oss event skipped: not raw upload prefix", zap.String("objectKey", objectKey))
		return &CompleteUploadOutput{SourceObjectKey: objectKey}, nil
	}

	var asset *model.VideoAsset
	token := strings.TrimSpace(in.Secret)
	videoID := strings.TrimSpace(in.VideoID)

	if token != "" && videoID != "" {
		consumed, err := s.repo.ConsumeCallbackToken(ctx, videoID, token)
		if err != nil {
			if errors.Is(err, repository.ErrCallbackTokenInvalid) {
				asset, idemErr := s.idempotentAssetAfterCallback(ctx, videoID, objectKey)
				if idemErr != nil {
					return nil, fmt.Errorf("invalid or expired callback token")
				}
				if asset != nil {
					return &CompleteUploadOutput{
						VideoID:         asset.ID,
						SourceObjectKey: objectKey,
					}, nil
				}
				return nil, fmt.Errorf("invalid or expired callback token")
			}
			return nil, err
		}
		if consumed.RawPath != objectKey {
			return nil, fmt.Errorf("object key mismatch for video %s", videoID)
		}
		asset = consumed
	} else if global := strings.TrimSpace(s.cfg.OSS.EventCallbackSecret); global != "" {
		if token != global {
			return nil, fmt.Errorf("invalid oss event callback secret")
		}
		var err error
		asset, err = s.repo.GetVideoAssetByRawPath(ctx, objectKey)
		if err != nil {
			if errors.Is(err, repository.ErrVideoNotFound) {
				return nil, fmt.Errorf("no pending video_asset for object %s", objectKey)
			}
			return nil, err
		}
	} else {
		return nil, fmt.Errorf("callback token is required")
	}

	dramaID, episodeID, err := parseRawObjectKey(objectKey, prefix)
	if err != nil {
		return nil, err
	}
	if asset.DramaID != nil && strings.TrimSpace(*asset.DramaID) != "" {
		dramaID = strings.TrimSpace(*asset.DramaID)
	}
	if asset.EpisodeID != nil && strings.TrimSpace(*asset.EpisodeID) != "" {
		episodeID = strings.TrimSpace(*asset.EpisodeID)
	}

	if asset.Status == model.VideoStatusReady || asset.Status == model.VideoStatusTranscoding {
		s.log.Info("oss event skipped: already processing or ready",
			zap.String("videoId", asset.ID),
			zap.String("status", asset.Status),
		)
		return &CompleteUploadOutput{
			VideoID:         asset.ID,
			SourceObjectKey: objectKey,
		}, nil
	}

	s.log.Info("oss event received",
		zap.String("videoId", asset.ID),
		zap.String("objectKey", objectKey),
		zap.String("bucket", in.Bucket),
	)

	return s.triggerTranscode(ctx, asset.ID, objectKey, dramaID, episodeID, in.Etag, in.SizeBytes)
}

func (s *VideoService) idempotentAssetAfterCallback(ctx context.Context, videoID, objectKey string) (*model.VideoAsset, error) {
	asset, err := s.repo.GetVideoAssetByID(ctx, videoID)
	if err != nil {
		return nil, err
	}
	if asset.RawPath != objectKey {
		return nil, repository.ErrCallbackTokenInvalid
	}
	if asset.Status == model.VideoStatusTranscoding || asset.Status == model.VideoStatusReady {
		s.log.Info("oss callback idempotent skip",
			zap.String("videoId", videoID),
			zap.String("status", asset.Status),
		)
		return asset, nil
	}
	return nil, repository.ErrCallbackTokenInvalid
}

// parseRawObjectKey 从 raw/{dramaId}/{episodeId}.mp4 解析 dramaId、episodeId。
func parseRawObjectKey(objectKey, prefix string) (dramaID, episodeID string, err error) {
	rest := objectKey
	if prefix != "" {
		p := prefix + "/"
		if !strings.HasPrefix(rest, p) {
			return "", "", fmt.Errorf("unexpected object key: %s", objectKey)
		}
		rest = strings.TrimPrefix(rest, p)
	}
	parts := strings.Split(rest, "/")
	if len(parts) != 2 {
		return "", "", fmt.Errorf("unexpected object key: %s", objectKey)
	}
	dramaID = strings.TrimSpace(parts[0])
	name := strings.TrimSpace(parts[1])
	if !strings.HasSuffix(strings.ToLower(name), ".mp4") {
		return "", "", fmt.Errorf("expected .mp4 object: %s", objectKey)
	}
	episodeID = strings.TrimSuffix(name, ".mp4")
	episodeID = strings.TrimSuffix(episodeID, ".MP4")
	if dramaID == "" || episodeID == "" {
		return "", "", fmt.Errorf("invalid drama/episode in key: %s", objectKey)
	}
	return dramaID, episodeID, nil
}

// ParseOSSEventBody 解析阿里云 OSS 事件通知 JSON 或上传回调表单。
func ParseOSSEventBody(contentType string, body []byte, form url.Values) (*OSSEventInput, error) {
	ct := strings.ToLower(strings.TrimSpace(contentType))
	if strings.Contains(ct, "application/json") || (len(body) > 0 && body[0] == '{') {
		return parseOSSEventJSON(body)
	}
	if form != nil {
		return parseOSSEventForm(form), nil
	}
	return nil, fmt.Errorf("unsupported oss event content type")
}

// IsOSSUploadCallback 是否为 OSS PutObject 上传回调（表单），区别于 MNS/事件通知 JSON。
func IsOSSUploadCallback(contentType string, body []byte, form url.Values) bool {
	ct := strings.ToLower(strings.TrimSpace(contentType))
	if strings.Contains(ct, "application/x-www-form-urlencoded") {
		return true
	}
	if form != nil {
		if form.Get("object") != "" || form.Get("filename") != "" {
			return true
		}
	}
	_ = body
	return false
}

type ossEventNotification struct {
	Events []struct {
		EventName string `json:"eventName"`
		OSS       struct {
			Bucket struct {
				Name string `json:"name"`
			} `json:"bucket"`
			Object struct {
				Key  string `json:"key"`
				ETag string `json:"eTag"`
				Size int64  `json:"size"`
			} `json:"object"`
		} `json:"oss"`
	} `json:"events"`
}

type ossEventSimple struct {
	Bucket    string `json:"bucket"`
	ObjectKey string `json:"objectKey"`
	EventName string `json:"eventName"`
	Etag      string `json:"etag"`
	SizeBytes int64  `json:"sizeBytes"`
}

func parseOSSEventJSON(body []byte) (*OSSEventInput, error) {
	var wrap ossEventNotification
	if err := json.Unmarshal(body, &wrap); err == nil && len(wrap.Events) > 0 {
		ev := wrap.Events[0]
		key, _ := url.QueryUnescape(ev.OSS.Object.Key)
		return &OSSEventInput{
			Bucket:    ev.OSS.Bucket.Name,
			ObjectKey: key,
			EventName: ev.EventName,
			Etag:      strings.Trim(ev.OSS.Object.ETag, `"`),
			SizeBytes: ev.OSS.Object.Size,
		}, nil
	}
	var simple ossEventSimple
	if err := json.Unmarshal(body, &simple); err != nil {
		return nil, fmt.Errorf("parse oss event json: %w", err)
	}
	return &OSSEventInput{
		Bucket:    simple.Bucket,
		ObjectKey: simple.ObjectKey,
		EventName: simple.EventName,
		Etag:      simple.Etag,
		SizeBytes: simple.SizeBytes,
	}, nil
}

func parseOSSEventForm(form url.Values) *OSSEventInput {
	objectKey := form.Get("object")
	if objectKey == "" {
		objectKey = form.Get("filename")
	}
	objectKey, _ = url.QueryUnescape(objectKey)
	size, _ := strconv.ParseInt(form.Get("size"), 10, 64)
	return &OSSEventInput{
		Bucket:    form.Get("bucket"),
		ObjectKey: objectKey,
		EventName: "ObjectCreated:PutObject",
		Etag:      strings.Trim(form.Get("etag"), `"`),
		SizeBytes: size,
		VideoID:   strings.TrimSpace(form.Get("videoId")),
	}
}
