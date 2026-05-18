// Package repository 封装 PostgreSQL 数据访问（类比 Java MyBatis / JPA Repository）。
//
// 数据库：video_db
// 表：video_asset、transcode_task
package repository

import (
	"context"
	"errors"
	"fmt"

	"github.com/jackc/pgx/v5"
	"github.com/jackc/pgx/v5/pgxpool"
	"go.uber.org/zap"

	"micro-drama-video/internal/config"
	"micro-drama-video/internal/model"
)

// VideoRepo 视频资产与转码任务的数据访问对象。
type VideoRepo struct {
	log  *zap.Logger
	pool *pgxpool.Pool
}

// NewVideoRepo 根据配置创建连接池并 Ping 探测连通性。
func NewVideoRepo(ctx context.Context, cfg *config.Config, log *zap.Logger) (*VideoRepo, error) {
	if log == nil {
		log = zap.NewNop()
	}
	dsn := cfg.DB.DSN()
	if dsn == "" {
		return nil, fmt.Errorf("database dsn is empty (set db_dsn or db_host/db_name/db_user in Consul KV)")
	}
	pool, err := pgxpool.New(ctx, dsn)
	if err != nil {
		return nil, fmt.Errorf("pgx pool: %w", err)
	}
	if err := pool.Ping(ctx); err != nil {
		pool.Close()
		return nil, fmt.Errorf("database ping: %w", err)
	}
	log.Info("database connected",
		zap.String("dbName", cfg.DB.Name),
		zap.String("host", cfg.DB.Host),
	)
	return &VideoRepo{log: log, pool: pool}, nil
}

// Close 关闭连接池，进程退出时调用。
func (r *VideoRepo) Close() {
	if r.pool != nil {
		r.pool.Close()
	}
}

// CreateVideoAssetParams 插入 video_asset 所需字段。
type CreateVideoAssetParams struct {
	ID        string  // 与 OSS 目录、Kafka videoId 一致
	DramaID   *string // 可选
	EpisodeID *string // 可选
	RawPath   string  // OSS object key
	SizeBytes int64
}

// CreateVideoAsset 上传 OSS 成功后写入视频资产表，状态 UPLOADED。
func (r *VideoRepo) CreateVideoAsset(ctx context.Context, p CreateVideoAssetParams) error {
	_, err := r.pool.Exec(ctx, `
		INSERT INTO video_asset (
			id, drama_id, episode_id, raw_path, size_bytes, status, created_at, updated_at
		) VALUES (
			$1, $2, $3, $4, $5, $6, NOW(), NOW()
		)`,
		p.ID,
		nullableUUID(p.DramaID),
		nullableUUID(p.EpisodeID),
		p.RawPath,
		p.SizeBytes,
		model.VideoStatusUploaded,
	)
	if err != nil {
		return err
	}
	r.log.Debug("CreateVideoAsset ok", zap.String("videoId", p.ID), zap.String("rawPath", p.RawPath))
	return nil
}

// CreateTranscodeTaskParams 插入 transcode_task 所需字段。
type CreateTranscodeTaskParams struct {
	VideoAssetID string
	InputPath    string // 原片 OSS 路径，一般等于 raw_path
	OutputPath   string // 预期 HLS 输出路径（transcoder 写入后回填 video_asset.hls_path）
}

// CreateTranscodeTask Kafka 发送成功后写入转码任务，并将视频状态改为 TRANSCODING。
func (r *VideoRepo) CreateTranscodeTask(ctx context.Context, p CreateTranscodeTaskParams) (string, error) {
	tx, err := r.pool.Begin(ctx)
	if err != nil {
		return "", err
	}
	defer tx.Rollback(ctx)

	var taskID string
	err = tx.QueryRow(ctx, `
		INSERT INTO transcode_task (
			video_asset_id, input_path, output_path, status, retry_count, created_at, updated_at
		) VALUES (
			$1, $2, $3, $4, 0, NOW(), NOW()
		)
		RETURNING id`,
		p.VideoAssetID,
		p.InputPath,
		p.OutputPath,
		model.TaskStatusPending,
	).Scan(&taskID)
	if err != nil {
		return "", err
	}

	_, err = tx.Exec(ctx, `
		UPDATE video_asset
		SET status = $2, updated_at = NOW()
		WHERE id = $1`,
		p.VideoAssetID,
		model.VideoStatusTranscoding,
	)
	if err != nil {
		return "", err
	}

	if err := tx.Commit(ctx); err != nil {
		return "", err
	}
	r.log.Debug("CreateTranscodeTask ok",
		zap.String("taskId", taskID),
		zap.String("videoId", p.VideoAssetID),
		zap.String("outputPath", p.OutputPath),
	)
	return taskID, nil
}

// ErrVideoNotFound 播放鉴权时视频不存在。
var ErrVideoNotFound = errors.New("video not found")

// ErrVideoNotReady 视频尚未转码完成或 hls_path 为空。
var ErrVideoNotReady = errors.New("video not ready for playback")

// GetVideoAssetByID 按主键查询视频资产（播放鉴权用）。
func (r *VideoRepo) GetVideoAssetByID(ctx context.Context, id string) (*model.VideoAsset, error) {
	row := r.pool.QueryRow(ctx, `
		SELECT
			id, drama_id, episode_id, raw_path, hls_path, cover_path, subtitle_path,
			duration, size_bytes, resolution, status, created_at, updated_at
		FROM video_asset
		WHERE id = $1`,
		id,
	)

	var a model.VideoAsset
	var dramaID, episodeID, hlsPath, coverPath, subtitlePath, resolution *string
	var duration *int
	var sizeBytes *int64

	err := row.Scan(
		&a.ID,
		&dramaID,
		&episodeID,
		&a.RawPath,
		&hlsPath,
		&coverPath,
		&subtitlePath,
		&duration,
		&sizeBytes,
		&resolution,
		&a.Status,
		&a.CreatedAt,
		&a.UpdatedAt,
	)
	if errors.Is(err, pgx.ErrNoRows) {
		return nil, ErrVideoNotFound
	}
	if err != nil {
		return nil, err
	}

	a.DramaID = dramaID
	a.EpisodeID = episodeID
	a.HlsPath = hlsPath
	a.CoverPath = coverPath
	a.SubtitlePath = subtitlePath
	a.Resolution = resolution
	a.Duration = duration
	a.SizeBytes = sizeBytes
	return &a, nil
}

// nullableUUID 将空字符串转为 nil，避免写入非法 UUID。
func nullableUUID(s *string) any {
	if s == nil || *s == "" {
		return nil
	}
	return *s
}
