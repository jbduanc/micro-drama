// Package handler 是 HTTP 接入层（类比 Java @RestController）。
//
// 职责：
//   - 解析请求参数（Query、Header、multipart 文件）
//   - 调用 internal/service 执行业务
//   - 将结果封装为 response.Result JSON 返回
//   - 不包含 OSS/Kafka 等基础设施细节
//
// 路由前缀 /v1/video，经 Kong 可映射为 /api/v1/video/*。
package handler

import (
	"net/http"
	"strconv"
	"strings"

	"github.com/gin-gonic/gin"
	"go.uber.org/zap"

	"micro-drama-video/internal/response"
	"micro-drama-video/internal/service"
)

// Register 在 Gin Engine 上注册所有路由。
//
// 参数 r：Gin 根引擎；log：日志；svc：视频业务 Service（依赖注入，类似 @Autowired）。
func Register(r *gin.Engine, log *zap.Logger, svc *service.VideoService) {
	// 健康检查：Consul、K8s、Kong 探活使用，无业务逻辑。
	r.GET("/healthz", func(c *gin.Context) {
		c.JSON(http.StatusOK, gin.H{"ok": true})
	})

	// 业务 API 分组，便于版本管理与 Kong 路由配置。
	v1 := r.Group("/v1/video")
	{
		v1.POST("/upload", uploadHandler(log, svc))
		v1.GET("/play", playHandler(log, svc))
	}
}

// uploadHandler 处理视频上传接口。
//
// 路由：POST /v1/video/upload
// Content-Type：multipart/form-data
// 表单字段：
//   - file（必填）：视频文件二进制
//   - fileName（可选）：覆盖原始文件名
//   - dramaId（可选）：剧集 ID，写入 video_asset.drama_id
//   - episodeId（可选）：分集 ID，写入 video_asset.episode_id
//
// 请求头（占位）：
//   - X-User-Id：后续改为 JWT 解析出的用户 ID
//
// 成功响应：Result<UploadOutput>，含 videoId、sourceObjectKey 等。
func uploadHandler(log *zap.Logger, svc *service.VideoService) gin.HandlerFunc {
	// 返回闭包函数，Gin 每个请求调用一次；等价于 Java 里 Controller 方法处理单次请求。
	return func(c *gin.Context) {
		log.Info("http upload request",
			zap.String("clientIp", c.ClientIP()),
			zap.String("userId", c.GetHeader("X-User-Id")),
		)
		// FormFile 解析 multipart 中名为 file 的文件域。
		fileHeader, err := c.FormFile("file")
		if err != nil {
			c.JSON(http.StatusBadRequest, response.Fail[any](400, "file is required (multipart field name: file)"))
			return
		}

		// 打开上传流；必须 Close，defer 保证请求结束释放句柄。
		f, err := fileHeader.Open()
		if err != nil {
			c.JSON(http.StatusInternalServerError, response.Fail[any](500, "open upload file failed"))
			return
		}
		defer f.Close()

		// 优先使用表单中的 fileName，否则用浏览器上传时的文件名。
		fileName := c.PostForm("fileName")
		if fileName == "" {
			fileName = fileHeader.Filename
		}

		dramaID := optionalForm(c, "dramaId")
		episodeID := optionalForm(c, "episodeId")

		// 调用 Service 层：OSS → video_asset → Kafka → transcode_task。
		out, err := svc.Upload(c.Request.Context(), &service.UploadInput{
			FileName:    fileName,
			ContentType: fileHeader.Header.Get("Content-Type"),
			Size:        fileHeader.Size,
			Reader:      f,
			UserID:      c.GetHeader("X-User-Id"),
			DramaID:     dramaID,
			EpisodeID:   episodeID,
		})
		if err != nil {
			log.Error("upload", zap.Error(err))
			c.JSON(http.StatusInternalServerError, response.Fail[any](500, err.Error()))
			return
		}
		c.JSON(http.StatusOK, response.OK(out))
	}
}

// playHandler 处理播放鉴权接口。
//
// 路由：GET /v1/video/play?videoId=xxx
// 成功响应：Result<PlayOutput>，含 playUrl（OSS 预签名 HLS 地址）、expiresIn 等。
func playHandler(log *zap.Logger, svc *service.VideoService) gin.HandlerFunc {
	return func(c *gin.Context) {
		videoID := c.Query("videoId")
		log.Info("http play request",
			zap.String("videoId", videoID),
			zap.String("clientIp", c.ClientIP()),
		)
		out, err := svc.PlayAuth(c.Request.Context(), &service.PlayInput{
			VideoID: videoID,
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

// MaxUploadBytes 返回 Gin 中间件，限制请求体大小，防止恶意超大上传。
//
// 参数 maxMB：最大允许 MB；<=0 时使用默认 500MB。
// 原理：在 handler 执行前检查 Content-Length，超限则 413 并 Abort 后续链。
func MaxUploadBytes(maxMB int) gin.HandlerFunc {
	if maxMB <= 0 {
		maxMB = 500
	}
	limit := int64(maxMB) * 1024 * 1024
	return func(c *gin.Context) {
		// ContentLength 由客户端声明；未声明时可能为 -1，此时无法提前拦截。
		if c.Request.ContentLength > limit {
			c.JSON(http.StatusRequestEntityTooLarge, response.Fail[any](413, "file too large, max "+strconv.Itoa(maxMB)+"MB"))
			c.Abort() // 停止执行后续 handler
			return
		}
		c.Next() // 继续执行后续中间件和路由 handler
	}
}

// optionalForm 读取表单字段，空字符串返回 nil（数据库写入 NULL）。
func optionalForm(c *gin.Context, key string) *string {
	v := strings.TrimSpace(c.PostForm(key))
	if v == "" {
		return nil
	}
	return &v
}
