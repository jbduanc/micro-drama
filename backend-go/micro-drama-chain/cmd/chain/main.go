package main

import (
	"context"
	"net"
	"os/signal"
	"syscall"

	"go.uber.org/zap"
	"google.golang.org/grpc"

	chainv1 "micro-drama-chain/api/gen/chain/v1"
	"micro-drama-chain/internal/blockchain"
	"micro-drama-chain/internal/config"
	"micro-drama-chain/internal/grpcserver"
	"micro-drama-chain/internal/repository"
	"micro-drama-chain/internal/service"
)

func main() {
	log, _ := zap.NewProduction()
	defer log.Sync()

	cfg, err := config.Load()
	if err != nil {
		log.Fatal("config", zap.Error(err))
	}

	eth, err := blockchain.Dial(cfg)
	if err != nil {
		log.Warn("eth dial failed", zap.Error(err))
	}

	repo := repository.NewMemoryRepo()
	svc := service.NewChainService(log, eth, repo)

	lis, err := net.Listen("tcp", cfg.GRPCAddr)
	if err != nil {
		log.Fatal("listen", zap.Error(err))
	}

	gs := grpc.NewServer()
	chainv1.RegisterChainServiceServer(gs, grpcserver.New(log, svc))

	ctx, stop := signal.NotifyContext(context.Background(), syscall.SIGINT, syscall.SIGTERM)
	defer stop()

	go func() {
		<-ctx.Done()
		gs.GracefulStop()
	}()

	log.Info("micro-drama-chain gRPC listening", zap.String("addr", cfg.GRPCAddr))
	if err := gs.Serve(lis); err != nil {
		log.Fatal("serve", zap.Error(err))
	}
}

