// Package handler 是 HTTP 接入层（类比 Java @RestController）。
package handler

import (
	"io"
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
		// STS 直传：申请临时凭证 → 前端 ali-oss 上传 → OSS 事件回调自动转码
		v1.POST("/sts", stsUploadHandler(log, svc))
		v1.POST("/oss-event", ossEventHandler(log, svc))
		// 兼容：预签名 PUT + 手动 notify-transcode
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

// stsUploadHandler POST /v1/video/sts
func stsUploadHandler(log *zap.Logger, svc *service.VideoService) gin.HandlerFunc {
	return func(c *gin.Context) {
		var req uploadURLRequest
		if err := c.ShouldBindJSON(&req); err != nil {
			c.JSON(http.StatusBadRequest, response.Fail[any](400, "dramaId and episodeId are required"))
			return
		}
		log.Info("http sts request",
			zap.String("dramaId", req.DramaID),
			zap.String("episodeId", req.EpisodeID),
		)
		out, err := svc.CreateSTSUploadCredentials(c.Request.Context(), &service.STSUploadInput{
			DramaID:     req.DramaID,
			EpisodeID:   req.EpisodeID,
			ContentType: req.ContentType,
			UserID:      c.GetHeader("X-User-Id"),
		})
		if err != nil {
			log.Error("sts", zap.Error(err))
			c.JSON(http.StatusInternalServerError, response.Fail[any](500, err.Error()))
			return
		}
		c.JSON(http.StatusOK, response.OK(out))
	}
}

// ossEventHandler POST /v1/video/oss-event — OSS 事件通知 / 上传回调，自动触发转码。
func ossEventHandler(log *zap.Logger, svc *service.VideoService) gin.HandlerFunc {
	return func(c *gin.Context) {
		body, _ := io.ReadAll(c.Request.Body)
		_ = c.Request.ParseForm()
		in, err := service.ParseOSSEventBody(c.ContentType(), body, c.Request.PostForm)
		if err != nil {
			c.JSON(http.StatusBadRequest, response.Fail[any](400, err.Error()))
			return
		}
		in.Secret = strings.TrimSpace(c.Query("token"))
		if in.Secret == "" {
			in.Secret = strings.TrimSpace(c.GetHeader("X-Oss-Callback-Token"))
		}
		if in.VideoID == "" {
			in.VideoID = strings.TrimSpace(c.Query("videoId"))
		}
		log.Info("http oss-event",
			zap.String("objectKey", in.ObjectKey),
			zap.String("eventName", in.EventName),
		)
		out, err := svc.HandleOSSEvent(c.Request.Context(), in)
		if err != nil {
			log.Error("oss-event", zap.Error(err))
			if service.IsOSSUploadCallback(c.ContentType(), body, c.Request.PostForm) {
				c.JSON(http.StatusInternalServerError, gin.H{"Status": "Error", "Message": err.Error()})
				return
			}
			c.JSON(http.StatusInternalServerError, response.Fail[any](500, err.Error()))
			return
		}
		// OSS 上传回调要求响应 {"Status":"Ok"}，否则客户端会报 CallbackFailed
		if service.IsOSSUploadCallback(c.ContentType(), body, c.Request.PostForm) {
			c.JSON(http.StatusOK, gin.H{"Status": "Ok"})
			return
		}
		c.JSON(http.StatusOK, response.OK(out))
	}
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
	Items           []deleteVideoItemRequest `json:"items" binding:"required,min=1,dive"`
	PreserveRawPath string                   `json:"preserveRawPath"`
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
			Items:           items,
			PreserveRawPath: req.PreserveRawPath,
			UserID:          c.GetHeader("X-User-Id"),
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
