// Package main 是 micro-drama-video 微服务的程序入口。
//
// 本服务职责（与 micro-drama-transcoder 分工）：
//   - 提供 HTTP API：视频上传（写入阿里云 OSS + 发 Kafka 通知转码）
//   - 提供 HTTP API：播放鉴权（返回 HLS 预签名 URL，前端直连 OSS 播放）
//   - 注册到 Consul，供 Kong / Java 服务发现
//
// 启动方式：go run ./cmd/video-api  或运行编译后的 video-api 二进制。
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

	"micro-drama-video/internal/config"
	"micro-drama-video/internal/consul"
	"micro-drama-video/internal/handler"
	"micro-drama-video/internal/kafka"
	"micro-drama-video/internal/repository"
	"micro-drama-video/internal/service"
	"micro-drama-video/internal/storage"
)

// main 组装依赖并启动 HTTP 服务，等价于 Java 的 SpringApplication.run。
func main() {
	log, _ := zap.NewProduction()
	defer log.Sync()

	log.Info("micro-drama-video starting")

	// ---------- 1. 加载配置（Consul KV + 环境变量 CONSUL_* / VIDEO_*）----------
	cfg, err := config.Load(log)
	if err != nil {
		log.Fatal("startup failed at config load", zap.Error(err))
	}

	// ---------- 2. 初始化基础设施客户端 ----------
	log.Info("initializing oss client", zap.String("endpoint", cfg.OSS.Endpoint), zap.String("bucket", cfg.OSS.Bucket))
	ossCli, err := storage.NewOSS(cfg)
	if err != nil {
		log.Fatal("startup failed at oss init", zap.Error(err))
	}
	log.Info("oss client ready")

	log.Info("connecting database", zap.String("dbName", cfg.DB.Name))
	repo, err := repository.NewVideoRepo(context.Background(), cfg, log)
	if err != nil {
		log.Fatal("startup failed at database", zap.Error(err))
	}
	defer repo.Close()

	log.Info("initializing kafka producer", zap.Bool("enabled", cfg.Kafka.Enabled), zap.Strings("brokers", cfg.Kafka.Brokers))
	producer, err := kafka.NewProducer(log, cfg)
	if err != nil {
		log.Fatal("startup failed at kafka", zap.Error(err))
	}
	defer producer.Close()

	// ---------- 3. 业务层（Service）与 HTTP 层（Handler）----------
	svc := service.NewVideoService(log, cfg, ossCli, producer, repo)

	// Gin：Go 的 Web 框架，类似 Spring MVC。
	gin.SetMode(gin.ReleaseMode) // 关闭 debug 路由表等开发输出
	r := gin.New()
	// Recovery：捕获 handler 内 panic，避免整个进程崩溃。
	r.Use(gin.Recovery())
	// 限制单次上传体积，防止超大文件打满内存/磁盘。
	r.Use(handler.MaxUploadBytes(0)) // 0 表示使用默认 500MB
	handler.Register(r, log, svc)

	// ---------- 4. Consul 服务注册（本地调试可设 VIDEO_CONSUL_DISCOVERY_ENABLED=false 跳过）----------
	deregister, err := consul.RegisterService(cfg.Viper(), log, cfg.HTTPAddr, "/healthz")
	if err != nil {
		log.Fatal("startup failed at consul register", zap.Error(err))
	}
	// 进程退出时从 Consul 注销，避免 Kong 继续转发到已下线实例。
	defer deregister()

	// ---------- 5. 启动 HTTP Server ----------
	// 使用标准库 http.Server，便于优雅关闭（Graceful Shutdown）。
	srv := &http.Server{Addr: cfg.HTTPAddr, Handler: r}

	// 在独立 goroutine 中监听 SIGINT/SIGTERM（Ctrl+C、K8s 停止 Pod）。
	go func() {
		ch := make(chan os.Signal, 1)
		signal.Notify(ch, syscall.SIGINT, syscall.SIGTERM)
		<-ch // 阻塞直到收到退出信号
		// 最多等待 10 秒处理完进行中的请求再退出。
		ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
		defer cancel()
		_ = srv.Shutdown(ctx)
	}()

	log.Info("micro-drama-video api started",
		zap.String("addr", cfg.HTTPAddr),
		zap.String("oss_endpoint", cfg.OSS.Endpoint),
		zap.String("oss_bucket", cfg.OSS.Bucket),
		zap.String("db_name", cfg.DB.Name),
		zap.Bool("kafka_enabled", cfg.Kafka.Enabled),
	)
	// ListenAndServe 阻塞运行；Shutdown 后返回 http.ErrServerClosed，属正常退出。
	if err := srv.ListenAndServe(); err != nil && err != http.ErrServerClosed {
		log.Fatal("listen", zap.Error(err))
	}
	log.Info("shutdown complete")
}
