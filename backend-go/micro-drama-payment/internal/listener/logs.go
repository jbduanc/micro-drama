package listener

import (
	"context"
	"time"

	"github.com/ethereum/go-ethereum/ethclient"
	"go.uber.org/zap"

	"micro-drama-payment/internal/config"
)

// StartLogWatcher polls chain head periodically; replace with SubscribeFilterLogs + WS for events.
func StartLogWatcher(ctx context.Context, log *zap.Logger, cfg *config.Config, cli *ethclient.Client) {
	_ = cfg
	if cli == nil {
		log.Warn("eth client nil, log watcher not started")
		return
	}
	go func() {
		t := time.NewTicker(30 * time.Second)
		defer t.Stop()
		for {
			select {
			case <-ctx.Done():
				return
			case <-t.C:
				head, err := cli.HeaderByNumber(ctx, nil)
				if err != nil {
					log.Warn("header", zap.Error(err))
					continue
				}
				log.Debug("chain head", zap.Uint64("number", head.Number.Uint64()))
			}
		}
	}()
}
