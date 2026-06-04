package service

import (
	"context"
	"fmt"

	"go.uber.org/zap"

	"micro-drama-chain/internal/blockchain"
	"micro-drama-chain/internal/repository"
)

type ChainService struct {
	log  *zap.Logger
	eth  *blockchain.Client
	repo repository.OrderRepository
}

func NewChainService(log *zap.Logger, eth *blockchain.Client, repo repository.OrderRepository) *ChainService {
	return &ChainService{log: log, eth: eth, repo: repo}
}

func (s *ChainService) CreatePendingOrder(ctx context.Context, id string) (*repository.Order, error) {
	o := &repository.Order{ID: id, Status: "pending"}
	if err := s.repo.Save(ctx, o); err != nil {
		return nil, err
	}
	return o, nil
}

func (s *ChainService) SendRawTransaction(ctx context.Context, signedHex string) (string, error) {
	if s.eth == nil || !s.eth.Connected() {
		return "", fmt.Errorf("eth client not configured (set eth_rpc_http)")
	}
	return s.eth.SendRawTransaction(ctx, signedHex)
}

func (s *ChainService) EthConnected() bool {
	return s.eth != nil && s.eth.Connected()
}
