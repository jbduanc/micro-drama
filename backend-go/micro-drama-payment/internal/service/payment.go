package service

import (
	"context"
	"fmt"

	"github.com/ethereum/go-ethereum/ethclient"
	"go.uber.org/zap"

	"micro-drama-payment/internal/repository"
)

type PaymentService struct {
	log  *zap.Logger
	eth  *ethclient.Client
	repo repository.OrderRepository
}

func NewPaymentService(log *zap.Logger, eth *ethclient.Client, repo repository.OrderRepository) *PaymentService {
	return &PaymentService{log: log, eth: eth, repo: repo}
}

// SendRawTransaction placeholder: decode signed tx hex and send (implement when you have chain id + gas).
func (s *PaymentService) SendRawTransaction(ctx context.Context, rawHex string) (string, error) {
	if s.eth == nil {
		return "", fmt.Errorf("eth client not configured (set PAYMENT_ETH_RPC_HTTP)")
	}
	_ = ctx
	_ = rawHex
	return "", fmt.Errorf("not implemented: pass signed RLP hex and ethclient.SendTransaction")
}

func (s *PaymentService) CreateOrder(ctx context.Context, id string) (*repository.Order, error) {
	o := &repository.Order{ID: id, Status: "pending"}
	if err := s.repo.Save(ctx, o); err != nil {
		return nil, err
	}
	return o, nil
}
