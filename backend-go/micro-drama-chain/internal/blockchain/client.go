package blockchain

import (
	"context"
	"encoding/hex"
	"fmt"
	"strings"

	"github.com/ethereum/go-ethereum/core/types"
	"github.com/ethereum/go-ethereum/ethclient"
	"github.com/ethereum/go-ethereum/rpc"

	"micro-drama-chain/internal/config"
)

// Client 基于 go-ethereum（海外 EVM 生态主流 SDK）的链上客户端。
type Client struct {
	eth *ethclient.Client
}

func Dial(cfg *config.Config) (*Client, error) {
	if cfg.EthRPCHTTP == "" {
		return &Client{}, nil
	}
	eth, err := ethclient.Dial(cfg.EthRPCHTTP)
	if err != nil {
		return nil, err
	}
	return &Client{eth: eth}, nil
}

func (c *Client) Connected() bool {
	return c != nil && c.eth != nil
}

func (c *Client) SendRawTransaction(ctx context.Context, signedHex string) (string, error) {
	if c.eth == nil {
		return "", fmt.Errorf("eth rpc not configured (set eth_rpc_http in config)")
	}
	raw, err := hex.DecodeString(stringsTrim0x(signedHex))
	if err != nil {
		return "", fmt.Errorf("invalid signed hex: %w", err)
	}
	var tx types.Transaction
	if err := tx.UnmarshalBinary(raw); err != nil {
		return "", fmt.Errorf("decode signed tx: %w", err)
	}
	if err := c.eth.SendTransaction(ctx, &tx); err != nil {
		return "", err
	}
	return tx.Hash().Hex(), nil
}

// SubscribeNewHead 预留 WS 监听（eth_ws 配置后可用）。
func (c *Client) SubscribeNewHead(ctx context.Context, wsURL string, ch chan<- *types.Header) (func(), error) {
	if wsURL == "" {
		return func() {}, nil
	}
	cli, err := rpc.DialContext(ctx, wsURL)
	if err != nil {
		return nil, err
	}
	sub, err := cli.EthSubscribe(ctx, ch, "newHeads")
	if err != nil {
		cli.Close()
		return nil, err
	}
	return func() {
		sub.Unsubscribe()
		cli.Close()
	}, nil
}

func stringsTrim0x(s string) string {
	s = strings.TrimSpace(s)
	if strings.HasPrefix(s, "0x") || strings.HasPrefix(s, "0X") {
		return s[2:]
	}
	return s
}
