// Package handler 是 HTTP 接入层（类比 Java @RestController）。
package handler

import (
	"net/http"
	"strings"

	"github.com/gin-gonic/gin"
	"go.uber.org/zap"

	"micro-drama-video/internal/response"
	"micro-drama-video/internal/service"
)

// Register 在 Gin Engine 上注册所有路由。
func Register(r *gin.Engine, log *zap.Logger, svc *service.VideoService) {
	r.GET("/healthz", func(c *gin.Context) {
		c.JSON(http.StatusOK, gin.H{"ok": true})
	})

	v1 := r.Group("/v1/video")
	{
		// 直传流程：先申请预签名 URL → 前端 PUT 到 OSS → 再调完成接口发 Kafka
		v1.POST("/upload-url", uploadURLHandler(log, svc))
		v1.POST("/upload-complete", uploadCompleteHandler(log, svc))
		v1.POST("/notify-transcode", notifyTranscodeHandler(log, svc))
		v1.POST("/delete", deleteVideosHandler(log, svc))
		v1.GET("/play", playHandler(log, svc))
	}
}

type uploadURLRequest struct {
	DramaID     string `json:"dramaId" binding:"required"`
	EpisodeID   string `json:"episodeId" binding:"required"`
	ContentType string `json:"contentType"`
}

// uploadURLHandler POST /v1/video/upload-url
func uploadURLHandler(log *zap.Logger, svc *service.VideoService) gin.HandlerFunc {
	return func(c *gin.Context) {
		var req uploadURLRequest
		if err := c.ShouldBindJSON(&req); err != nil {
			c.JSON(http.StatusBadRequest, response.Fail[any](400, "dramaId and episodeId are required"))
			return
		}
		log.Info("http upload-url request",
			zap.String("dramaId", req.DramaID),
			zap.String("episodeId", req.EpisodeID),
			zap.String("userId", c.GetHeader("X-User-Id")),
		)
		out, err := svc.CreateUploadURL(c.Request.Context(), &service.UploadURLInput{
			DramaID:     req.DramaID,
			EpisodeID:   req.EpisodeID,
			ContentType: req.ContentType,
			UserID:      c.GetHeader("X-User-Id"),
		})
		if err != nil {
			log.Error("upload-url", zap.Error(err))
			c.JSON(http.StatusInternalServerError, response.Fail[any](500, err.Error()))
			return
		}
		c.JSON(http.StatusOK, response.OK(out))
	}
}

type uploadCompleteRequest struct {
	VideoID   string `json:"videoId" binding:"required"`
	FileKey   string `json:"fileKey" binding:"required"`
	DramaID   string `json:"dramaId" binding:"required"`
	EpisodeID string `json:"episodeId" binding:"required"`
	Etag      string `json:"etag"`
	SizeBytes int64  `json:"sizeBytes"`
}

// uploadCompleteHandler POST /v1/video/upload-complete
func uploadCompleteHandler(log *zap.Logger, svc *service.VideoService) gin.HandlerFunc {
	return func(c *gin.Context) {
		var req uploadCompleteRequest
		if err := c.ShouldBindJSON(&req); err != nil {
			c.JSON(http.StatusBadRequest, response.Fail[any](400, "videoId, fileKey, dramaId and episodeId are required"))
			return
		}
		log.Info("http upload-complete request",
			zap.String("videoId", req.VideoID),
			zap.String("fileKey", req.FileKey),
		)
		out, err := svc.CompleteUpload(c.Request.Context(), &service.CompleteUploadInput{
			VideoID:   req.VideoID,
			FileKey:   req.FileKey,
			DramaID:   req.DramaID,
			EpisodeID: req.EpisodeID,
			Etag:      req.Etag,
			SizeBytes: req.SizeBytes,
			UserID:    c.GetHeader("X-User-Id"),
		})
		if err != nil {
			log.Error("upload-complete", zap.Error(err))
			c.JSON(http.StatusInternalServerError, response.Fail[any](500, err.Error()))
			return
		}
		c.JSON(http.StatusOK, response.OK(out))
	}
}

// notifyTranscodeHandler POST /v1/video/notify-transcode
func notifyTranscodeHandler(log *zap.Logger, svc *service.VideoService) gin.HandlerFunc {
	return func(c *gin.Context) {
		var req uploadCompleteRequest
		if err := c.ShouldBindJSON(&req); err != nil {
			c.JSON(http.StatusBadRequest, response.Fail[any](400, "videoId, fileKey, dramaId and episodeId are required"))
			return
		}
		log.Info("http notify-transcode request",
			zap.String("videoId", req.VideoID),
			zap.String("fileKey", req.FileKey),
		)
		out, err := svc.NotifyTranscode(c.Request.Context(), &service.NotifyTranscodeInput{
			VideoID:   req.VideoID,
			FileKey:   req.FileKey,
			DramaID:   req.DramaID,
			EpisodeID: req.EpisodeID,
			Etag:      req.Etag,
			SizeBytes: req.SizeBytes,
			UserID:    c.GetHeader("X-User-Id"),
		})
		if err != nil {
			log.Error("notify-transcode", zap.Error(err))
			c.JSON(http.StatusInternalServerError, response.Fail[any](500, err.Error()))
			return
		}
		c.JSON(http.StatusOK, response.OK(out))
	}
}

type deleteVideosRequest struct {
	Items []deleteVideoItemRequest `json:"items" binding:"required,min=1,dive"`
}

type deleteVideoItemRequest struct {
	VideoID string `json:"videoId" binding:"required"`
	FileKey string `json:"fileKey"`
}

// deleteVideosHandler POST /v1/video/delete
func deleteVideosHandler(log *zap.Logger, svc *service.VideoService) gin.HandlerFunc {
	return func(c *gin.Context) {
		var req deleteVideosRequest
		if err := c.ShouldBindJSON(&req); err != nil {
			c.JSON(http.StatusBadRequest, response.Fail[any](400, "items with videoId are required"))
			return
		}
		items := make([]service.DeleteVideoItem, 0, len(req.Items))
		for _, it := range req.Items {
			items = append(items, service.DeleteVideoItem{
				VideoID: it.VideoID,
				FileKey: it.FileKey,
			})
		}
		out, err := svc.DeleteVideos(c.Request.Context(), &service.DeleteVideosInput{
			Items:  items,
			UserID: c.GetHeader("X-User-Id"),
		})
		if err != nil {
			log.Error("delete videos", zap.Error(err))
			c.JSON(http.StatusInternalServerError, response.Fail[any](500, err.Error()))
			return
		}
		c.JSON(http.StatusOK, response.OK(out))
	}
}

// playHandler GET /v1/video/play?videoId=&orderId=
func playHandler(log *zap.Logger, svc *service.VideoService) gin.HandlerFunc {
	return func(c *gin.Context) {
		videoID := strings.TrimSpace(c.Query("videoId"))
		orderID := strings.TrimSpace(c.Query("orderId"))
		log.Info("http play request",
			zap.String("videoId", videoID),
			zap.String("orderId", orderID),
		)
		out, err := svc.PlayAuth(c.Request.Context(), &service.PlayInput{
			VideoID: videoID,
			OrderID: orderID,
			UserID:  c.GetHeader("X-User-Id"),
		})
		if err != nil {
			log.Warn("play auth", zap.Error(err), zap.String("videoId", videoID))
			c.JSON(http.StatusBadRequest, response.Fail[any](400, err.Error()))
			return
		}
		c.JSON(http.StatusOK, response.OK(out))
	}
}
