// Package repository 封装 PostgreSQL 数据访问（类比 Java MyBatis / JPA Repository）。
//
// 数据库：video_db
// 表：video_asset、transcode_task
package repository

import (
	"context"
	"errors"
	"fmt"
	"strings"

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
	ID            string
	DramaID       *string
	EpisodeID     *string
	RawPath       string
	SizeBytes     int64
	CallbackToken *string // 仅 UPLOADING 预创建时设置
}

// CreateVideoAsset 上传 OSS 成功后写入视频资产表，状态 UPLOADED。
func (r *VideoRepo) CreateVideoAsset(ctx context.Context, p CreateVideoAssetParams) error {
	return r.createVideoAssetWithStatus(ctx, p, model.VideoStatusUploaded)
}

// CreatePendingVideoAsset 签发 STS 时预创建资产，状态 UPLOADING。
func (r *VideoRepo) CreatePendingVideoAsset(ctx context.Context, p CreateVideoAssetParams) error {
	return r.createVideoAssetWithStatus(ctx, p, model.VideoStatusUploading)
}

func (r *VideoRepo) createVideoAssetWithStatus(ctx context.Context, p CreateVideoAssetParams, status string) error {
	_, err := r.pool.Exec(ctx, `
		INSERT INTO video_asset (
			id, drama_id, episode_id, raw_path, size_bytes, status, callback_token, created_at, updated_at
		) VALUES (
			$1, $2, $3, $4, $5, $6, $7, NOW(), NOW()
		)`,
		p.ID,
		nullableUUID(p.DramaID),
		nullableUUID(p.EpisodeID),
		p.RawPath,
		p.SizeBytes,
		status,
		nullableString(p.CallbackToken),
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

// ErrVideoAssetConflict 同一 raw_path 已存在其他视频记录。
var ErrVideoAssetConflict = errors.New("video asset conflict")

// ErrCallbackTokenInvalid 一次性回调 token 无效或已使用。
var ErrCallbackTokenInvalid = errors.New("invalid or expired callback token")

// ConsumeCallbackToken 校验并消费一次性 token（原子清空，仅 UPLOADING 状态可用）。
func (r *VideoRepo) ConsumeCallbackToken(ctx context.Context, videoID, token string) (*model.VideoAsset, error) {
	videoID = strings.TrimSpace(videoID)
	token = strings.TrimSpace(token)
	if videoID == "" || token == "" {
		return nil, ErrCallbackTokenInvalid
	}
	row := r.pool.QueryRow(ctx, `
		UPDATE video_asset
		SET callback_token = NULL, updated_at = NOW()
		WHERE id = $1 AND callback_token = $2 AND status = $3
		RETURNING
			id, drama_id, episode_id, raw_path, hls_path, cover_path, subtitle_path,
			duration, size_bytes, resolution, status, created_at, updated_at`,
		videoID,
		token,
		model.VideoStatusUploading,
	)
	asset, err := scanVideoAssetRow(row)
	if errors.Is(err, pgx.ErrNoRows) {
		return nil, ErrCallbackTokenInvalid
	}
	if err != nil {
		return nil, err
	}
	r.log.Debug("callback token consumed", zap.String("videoId", videoID))
	return asset, nil
}

// GetVideoAssetByRawPath 按原片 OSS 路径查询（OSS 事件回调用）。
func (r *VideoRepo) GetVideoAssetByRawPath(ctx context.Context, rawPath string) (*model.VideoAsset, error) {
	rawPath = strings.TrimPrefix(strings.TrimSpace(rawPath), "/")
	row := r.pool.QueryRow(ctx, `
		SELECT
			id, drama_id, episode_id, raw_path, hls_path, cover_path, subtitle_path,
			duration, size_bytes, resolution, status, created_at, updated_at
		FROM video_asset
		WHERE raw_path = $1
		ORDER BY created_at DESC
		LIMIT 1`,
		rawPath,
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

// MarkTranscodeSuccess 转码完成：更新 video_asset 为 READY 并回填 hls_path，转码任务 SUCCESS。
func (r *VideoRepo) MarkTranscodeSuccess(ctx context.Context, videoID, hlsPath string) error {
	tx, err := r.pool.Begin(ctx)
	if err != nil {
		return err
	}
	defer tx.Rollback(ctx)

	_, err = tx.Exec(ctx, `
		UPDATE video_asset
		SET status = $2, hls_path = $3, updated_at = NOW()
		WHERE id = $1`,
		videoID,
		model.VideoStatusReady,
		hlsPath,
	)
	if err != nil {
		return err
	}
	_, err = tx.Exec(ctx, `
		UPDATE transcode_task
		SET status = $2, updated_at = NOW()
		WHERE video_asset_id = $1`,
		videoID,
		model.TaskStatusSuccess,
	)
	if err != nil {
		return err
	}
	return tx.Commit(ctx)
}

// MarkTranscodeFailed 转码失败：video_asset FAILED，转码任务 FAILED。
func (r *VideoRepo) MarkTranscodeFailed(ctx context.Context, videoID, errMsg string) error {
	tx, err := r.pool.Begin(ctx)
	if err != nil {
		return err
	}
	defer tx.Rollback(ctx)

	_, err = tx.Exec(ctx, `
		UPDATE video_asset
		SET status = $2, updated_at = NOW()
		WHERE id = $1`,
		videoID,
		model.VideoStatusFailed,
	)
	if err != nil {
		return err
	}
	_, err = tx.Exec(ctx, `
		UPDATE transcode_task
		SET status = $2, error_msg = $3, updated_at = NOW()
		WHERE video_asset_id = $1`,
		videoID,
		model.TaskStatusFailed,
		errMsg,
	)
	if err != nil {
		return err
	}
	return tx.Commit(ctx)
}

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

// ListVideoAssetsByIDs 批量查询视频资产（删除前获取 OSS 路径）。
func (r *VideoRepo) ListVideoAssetsByIDs(ctx context.Context, ids []string) ([]*model.VideoAsset, error) {
	if len(ids) == 0 {
		return nil, nil
	}
	rows, err := r.pool.Query(ctx, `
		SELECT
			id, drama_id, episode_id, raw_path, hls_path, cover_path, subtitle_path,
			duration, size_bytes, resolution, status, created_at, updated_at
		FROM video_asset
		WHERE id = ANY($1)`,
		ids,
	)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var out []*model.VideoAsset
	for rows.Next() {
		var a model.VideoAsset
		var dramaID, episodeID, hlsPath, coverPath, subtitlePath, resolution *string
		var duration *int
		var sizeBytes *int64
		if err := rows.Scan(
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
		); err != nil {
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
		out = append(out, &a)
	}
	return out, rows.Err()
}

// DeleteVideoAssetsByIDs 删除视频资产及关联转码任务。
func (r *VideoRepo) DeleteVideoAssetsByIDs(ctx context.Context, ids []string) error {
	if len(ids) == 0 {
		return nil
	}
	tx, err := r.pool.Begin(ctx)
	if err != nil {
		return err
	}
	defer tx.Rollback(ctx)

	if _, err := tx.Exec(ctx, `DELETE FROM transcode_task WHERE video_asset_id = ANY($1)`, ids); err != nil {
		return err
	}
	if _, err := tx.Exec(ctx, `DELETE FROM video_asset WHERE id = ANY($1)`, ids); err != nil {
		return err
	}
	return tx.Commit(ctx)
}

// nullableUUID 将空字符串转为 nil，避免写入非法 UUID。
func nullableUUID(s *string) any {
	if s == nil || *s == "" {
		return nil
	}
	return *s
}

// nullableString 将空字符串指针转为 nil。
func nullableString(s *string) any {
	if s == nil || strings.TrimSpace(*s) == "" {
		return nil
	}
	return strings.TrimSpace(*s)
}

func scanVideoAssetRow(row pgx.Row) (*model.VideoAsset, error) {
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
