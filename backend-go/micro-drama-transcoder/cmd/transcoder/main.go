package main

import (
	"context"
	"net/http"
	"os"
	"os/signal"
	"syscall"
	"time"

	"github.com/gin-gonic/gin"
	"go.uber.org/zap"

	"micro-drama-transcoder/internal/config"
	"micro-drama-transcoder/internal/consul"
	"micro-drama-transcoder/internal/kafka"
	"micro-drama-transcoder/internal/service"
	"micro-drama-transcoder/internal/storage"
)

func main() {
	logger, _ := zap.NewProduction()
	defer logger.Sync()

	cfg, err := config.Load(logger)
	if err != nil {
		logger.Fatal("load config failed", zap.Error(err))
	}

	oss, err := storage.NewOSS(cfg)
	if err != nil {
		logger.Fatal("init oss failed", zap.Error(err))
	}

	prod, err := kafka.NewProducer(logger, cfg)
	if err != nil {
		logger.Fatal("init kafka producer failed", zap.Error(err))
	}
	defer prod.Close()

	consumer, err := kafka.NewConsumer(logger, cfg)
	if err != nil {
		logger.Fatal("init kafka consumer failed", zap.Error(err))
	}
	defer consumer.Close()

	svc := service.New(logger, cfg, oss, prod)

	ctx, cancel := context.WithCancel(context.Background())
	defer cancel()

	// Consul service discovery registration (optional via consul_discovery_enabled)
	deregister, err := consul.RegisterService(cfg.Viper(), logger, cfg.HTTPAddr, "/healthz")
	if err != nil {
		logger.Fatal("consul register failed", zap.Error(err))
	}
	defer deregister()

	// Run consumer loop
	go func() {
		logger.Info("kafka consumer started",
			zap.Strings("brokers", cfg.Kafka.Brokers),
			zap.String("groupId", cfg.Kafka.GroupID),
			zap.String("topic", cfg.Kafka.TopicUploadCompleted),
		)
		if err := consumer.Run(ctx, svc.HandleUploadCompleted); err != nil && ctx.Err() == nil {
			logger.Fatal("kafka consumer stopped unexpectedly", zap.Error(err))
		}
	}()

	// HTTP server: healthz only (kept to match infra expectations)
	r := gin.New()
	r.Use(gin.Recovery())
	r.GET("/healthz", func(c *gin.Context) { c.JSON(http.StatusOK, gin.H{"ok": true}) })

	srv := &http.Server{Addr: cfg.HTTPAddr, Handler: r}
	go func() {
		logger.Info("http listening", zap.String("addr", cfg.HTTPAddr))
		if err := srv.ListenAndServe(); err != nil && err != http.ErrServerClosed {
			logger.Fatal("http server error", zap.Error(err))
		}
	}()

	// graceful shutdown
	ch := make(chan os.Signal, 2)
	signal.Notify(ch, syscall.SIGINT, syscall.SIGTERM)
	<-ch
	logger.Info("shutdown signal received")
	cancel()

	shutdownCtx, shutdownCancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer shutdownCancel()
	_ = srv.Shutdown(shutdownCtx)
}
