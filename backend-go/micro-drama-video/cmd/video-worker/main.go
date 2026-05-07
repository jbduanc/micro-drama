package main

import (
	"context"
	"os"
	"os/signal"
	"sync"
	"syscall"

	"go.uber.org/zap"

	"micro-drama-video/internal/config"
	"micro-drama-video/internal/consumer"
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

	go func() {
		ch := make(chan os.Signal, 1)
		signal.Notify(ch, syscall.SIGINT, syscall.SIGTERM)
		<-ch
		cancel()
	}()

	var wg sync.WaitGroup
	if err := consumer.RunGroup(ctx, log, cfg, &wg); err != nil {
		log.Fatal("consumer", zap.Error(err))
	}

	log.Info("micro-drama-video worker started",
		zap.Strings("brokers", cfg.Kafka.Brokers),
		zap.String("topic", cfg.Kafka.TopicUploadCompleted),
	)
	wg.Wait()
	log.Info("shutdown complete")
}
