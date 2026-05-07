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

	"micro-drama-payment/internal/blockchain"
	"micro-drama-payment/internal/config"
	"micro-drama-payment/internal/handler"
	"micro-drama-payment/internal/listener"
	"micro-drama-payment/internal/repository"
	"micro-drama-payment/internal/service"
)

func main() {
	log, _ := zap.NewProduction()
	defer log.Sync()

	cfg, err := config.Load()
	if err != nil {
		log.Fatal("config", zap.Error(err))
	}

	ctx, cancel := context.WithCancel(context.Background())
	defer cancel()

	ethCli, err := blockchain.DialHTTP(ctx, cfg)
	if err != nil {
		log.Warn("eth dial failed", zap.Error(err))
		ethCli = nil
	}

	repo := repository.NewMemoryRepo()
	svc := service.NewPaymentService(log, ethCli, repo)
	listener.StartLogWatcher(ctx, log, cfg, ethCli)

	gin.SetMode(gin.ReleaseMode)
	r := gin.New()
	r.Use(gin.Recovery())
	handler.Register(r, log, svc)

	srv := &http.Server{Addr: cfg.HTTPAddr, Handler: r}

	go func() {
		ch := make(chan os.Signal, 1)
		signal.Notify(ch, syscall.SIGINT, syscall.SIGTERM)
		<-ch
		cancel()
		shutdownCtx, shutdownCancel := context.WithTimeout(context.Background(), 10*time.Second)
		defer shutdownCancel()
		_ = srv.Shutdown(shutdownCtx)
	}()

	log.Info("payment listening", zap.String("addr", cfg.HTTPAddr))
	if err := srv.ListenAndServe(); err != nil && err != http.ErrServerClosed {
		log.Fatal("listen", zap.Error(err))
	}
}
