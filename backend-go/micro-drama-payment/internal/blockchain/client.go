package blockchain

import (
	"context"

	"github.com/ethereum/go-ethereum/ethclient"

	"micro-drama-payment/internal/config"
)

func DialHTTP(ctx context.Context, cfg *config.Config) (*ethclient.Client, error) {
	if cfg.EthRPCHTTP == "" {
		return nil, nil
	}
	return ethclient.DialContext(ctx, cfg.EthRPCHTTP)
}
