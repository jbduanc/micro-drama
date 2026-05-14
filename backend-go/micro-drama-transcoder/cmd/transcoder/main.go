package main

import (
	"net/http"

	"github.com/gin-gonic/gin"
	"go.uber.org/zap"
)

func main() {
	logger, _ := zap.NewProduction()
	defer logger.Sync()

	r := gin.New()
	r.Use(gin.Recovery())

	r.GET("/healthz", func(c *gin.Context) {
		c.JSON(http.StatusOK, gin.H{"ok": true})
	})

	// TODO: GET /transcoder/video/play
	// - middleware: auth/ratelimit
	// - service: entitlement check + sign m3u8 url
	r.GET("/transcoder/video/play", func(c *gin.Context) {
		c.JSON(http.StatusOK, gin.H{
			"videoId":  c.Query("videoId"),
			"playUrl":  "TODO",
			"expires":  300,
			"token":    "TODO",
			"trace_id": "",
		})
	})

	addr := ":8080"
	logger.Info("transcoder listening", zap.String("addr", addr))
	_ = r.Run(addr)
}
