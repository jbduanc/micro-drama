// Package storage 封装对象存储（阿里云 OSS，S3 兼容 API）操作。
//
// 使用 minio-go 客户端（支持 AWS S3、阿里云 OSS、MinIO 等），
// 本服务只做：上传原片（PutObject）、签发播放 URL（PresignGet）。
// 转码后的 HLS 文件由 micro-drama-transcoder 写入同一 Bucket。
package storage

import (
	"context"
	"fmt"
	"io"
	"strings"
	"time"

	"github.com/minio/minio-go/v7"
	"github.com/minio/minio-go/v7/pkg/credentials"

	"micro-drama-video/internal/config"
)

// OSS 对象存储客户端包装，绑定 bucket 名，避免每次调用重复传参。
type OSS struct {
	cli    *minio.Client // 底层 S3 兼容 SDK 客户端
	bucket string        // 桶名，所有对象都在此 bucket 下
}

// NewOSS 根据配置创建 OSS 客户端。
//
// Endpoint 不要带 https:// 前缀；UseSSL=true 时使用 HTTPS。
// Region 阿里云必填，否则部分区域签名可能失败。
func NewOSS(cfg *config.Config) (*OSS, error) {
	opts := &minio.Options{
		Creds:  credentials.NewStaticV4(cfg.OSS.AccessKey, cfg.OSS.SecretKey, ""),
		Secure: cfg.OSS.UseSSL,
	}
	if cfg.OSS.Region != "" {
		opts.Region = cfg.OSS.Region
	}
	cli, err := minio.New(cfg.OSS.Endpoint, opts)
	if err != nil {
		return nil, err
	}
	return &OSS{cli: cli, bucket: cfg.OSS.Bucket}, nil
}

// PutObject 将 reader 中的数据上传到指定 objectKey。
//
// 参数：
//   - objectKey：桶内路径，如 uploads/uuid/demo.mp4
//   - size：对象大小（字节），流式上传时 SDK 需要
//   - contentType：MIME，影响浏览器/OSS 元数据
//
// 返回 ETag（OSS 生成的对象校验标识），写入 Kafka 事件供下游校验。
func (o *OSS) PutObject(ctx context.Context, objectKey string, reader io.Reader, size int64, contentType string) (string, error) {
	info, err := o.cli.PutObject(ctx, o.bucket, objectKey, reader, size, minio.PutObjectOptions{
		ContentType: contentType,
	})
	if err != nil {
		return "", err
	}
	return info.ETag, nil
}

// PresignPut 生成限时有效的 PUT 预签名 URL，供前端直传原片。
func (o *OSS) PresignPut(ctx context.Context, objectKey, contentType string, expire time.Duration) (string, error) {
	opts := minio.PutObjectOptions{}
	if strings.TrimSpace(contentType) != "" {
		opts.ContentType = contentType
	}
	u, err := o.cli.PresignedPutObject(ctx, o.bucket, objectKey, expire)
	if err != nil {
		return "", err
	}
	return u.String(), nil
}

// StatObject 校验对象是否存在并返回大小与 ETag。
func (o *OSS) StatObject(ctx context.Context, objectKey string) (size int64, etag string, err error) {
	info, err := o.cli.StatObject(ctx, o.bucket, objectKey, minio.StatObjectOptions{})
	if err != nil {
		return 0, "", err
	}
	return info.Size, info.ETag, nil
}

// PresignGet 生成限时有效的 GET 预签名 URL。
//
// 前端/播放器用此 URL 直接访问 OSS，无需把 AccessKey 暴露给浏览器。
// expire：URL 过期时间，过期后 OSS 返回 403。
func (o *OSS) PresignGet(ctx context.Context, objectKey string, expire time.Duration) (string, error) {
	u, err := o.cli.PresignedGetObject(ctx, o.bucket, objectKey, expire, nil)
	if err != nil {
		return "", err
	}
	return u.String(), nil
}

// BuildRawKey 原片直传路径：raw/{dramaId}/{episodeId}.mp4
func BuildRawKey(prefix, dramaID, episodeID string) string {
	prefix = strings.Trim(prefix, "/")
	dramaID = strings.Trim(dramaID, "/")
	episodeID = strings.Trim(episodeID, "/")
	if prefix == "" {
		return fmt.Sprintf("raw/%s/%s.mp4", dramaID, episodeID)
	}
	return fmt.Sprintf("%s/%s/%s.mp4", prefix, dramaID, episodeID)
}

// BuildHLSKey 预期 HLS 主清单路径：hls/{dramaId}/{episodeId}/index.m3u8
func BuildHLSKey(prefix, dramaID, episodeID string) string {
	prefix = strings.Trim(prefix, "/")
	dramaID = strings.Trim(dramaID, "/")
	episodeID = strings.Trim(episodeID, "/")
	if prefix == "" {
		return fmt.Sprintf("hls/%s/%s/index.m3u8", dramaID, episodeID)
	}
	return fmt.Sprintf("%s/%s/%s/index.m3u8", prefix, dramaID, episodeID)
}
