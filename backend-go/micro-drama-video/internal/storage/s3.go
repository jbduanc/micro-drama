package storage

import (
	"context"
	"fmt"

	"github.com/minio/minio-go/v7"
	"github.com/minio/minio-go/v7/pkg/credentials"

	"micro-drama-video/internal/config"
)

// NewMinioClient builds an S3-compatible client (Vultr Object Storage / MinIO).
func NewMinioClient(cfg *config.Config) (*minio.Client, error) {
	if cfg.S3.Endpoint == "" {
		return nil, fmt.Errorf("VIDEO_S3_ENDPOINT is required")
	}
	opts := &minio.Options{
		Creds:  credentials.NewStaticV4(cfg.S3.AccessKey, cfg.S3.SecretKey, ""),
		Secure: cfg.S3.UseSSL,
	}
	if cfg.S3.Region != "" {
		opts.Region = cfg.S3.Region
	}
	return minio.New(cfg.S3.Endpoint, opts)
}

// PresignGet is a placeholder helper for downloading source into worker tmp dir.
func PresignGet(ctx context.Context, cli *minio.Client, bucket, objectKey string) (string, error) {
	if cli == nil {
		return "", nil
	}
	u, err := cli.PresignedGetObject(ctx, bucket, objectKey, 3600, nil)
	if err != nil {
		return "", err
	}
	return u.String(), nil
}
