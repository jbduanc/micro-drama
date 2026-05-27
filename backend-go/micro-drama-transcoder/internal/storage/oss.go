package storage

import (
	"context"
	"io"
	"net/url"
	"path"
	"strings"
	"time"

	"github.com/minio/minio-go/v7"
	"github.com/minio/minio-go/v7/pkg/credentials"

	"micro-drama-transcoder/internal/config"
)

type OSS struct {
	cli    *minio.Client
	bucket string
}

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

func (o *OSS) GetObject(ctx context.Context, objectKey string) (io.ReadCloser, error) {
	obj, err := o.cli.GetObject(ctx, o.bucket, objectKey, minio.GetObjectOptions{})
	if err != nil {
		return nil, err
	}
	return obj, nil
}

func (o *OSS) PutObject(ctx context.Context, objectKey string, reader io.Reader, size int64, contentType string) (string, error) {
	info, err := o.cli.PutObject(ctx, o.bucket, objectKey, reader, size, minio.PutObjectOptions{ContentType: contentType})
	if err != nil {
		return "", err
	}
	return info.ETag, nil
}

func (o *OSS) PresignGet(ctx context.Context, objectKey string, expire time.Duration) (string, error) {
	u, err := o.cli.PresignedGetObject(ctx, o.bucket, objectKey, expire, nil)
	if err != nil {
		return "", err
	}
	return u.String(), nil
}

func IsHTTPURL(s string) bool {
	l := strings.ToLower(strings.TrimSpace(s))
	return strings.HasPrefix(l, "http://") || strings.HasPrefix(l, "https://")
}

func ObjectKeyFromURL(raw string) (string, bool) {
	u, err := url.Parse(strings.TrimSpace(raw))
	if err != nil || u == nil {
		return "", false
	}
	if u.Scheme != "http" && u.Scheme != "https" && u.Scheme != "s3" && u.Scheme != "oss" {
		return "", false
	}
	// best-effort: treat path as objectKey.
	key := strings.TrimPrefix(u.Path, "/")
	if key == "" {
		return "", false
	}
	return key, true
}

func Join(parts ...string) string {
	clean := make([]string, 0, len(parts))
	for _, p := range parts {
		p = strings.Trim(p, "/")
		if p != "" {
			clean = append(clean, p)
		}
	}
	return path.Join(clean...)
}
