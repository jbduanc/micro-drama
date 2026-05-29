package storage

import (
	"crypto/rand"
	"encoding/hex"
	"fmt"
	"net/url"
	"strings"

	"micro-drama-video/internal/config"
)

// NewCallbackToken 生成 32 字节随机十六进制字符串，用作一次性 OSS 回调 token。
func NewCallbackToken() (string, error) {
	b := make([]byte, 32)
	if _, err := rand.Read(b); err != nil {
		return "", fmt.Errorf("generate callback token: %w", err)
	}
	return hex.EncodeToString(b), nil
}

// BuildUploadCallbackURL 上传回调公网地址，携带 videoId + 一次性 token。
func BuildUploadCallbackURL(cfg *config.Config, videoID, callbackToken string) string {
	base := strings.TrimRight(strings.TrimSpace(cfg.OSS.UploadCallbackBaseURL), "/")
	videoID = strings.TrimSpace(videoID)
	callbackToken = strings.TrimSpace(callbackToken)
	if base == "" || videoID == "" || callbackToken == "" {
		return ""
	}
	q := url.Values{}
	q.Set("videoId", videoID)
	q.Set("token", callbackToken)
	return base + "/v1/video/oss-event?" + q.Encode()
}
