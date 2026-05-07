package config

import (
	"strings"

	"github.com/spf13/viper"
)

func Load() (*Config, error) {
	v := viper.New()
	v.SetEnvPrefix("PAYMENT")
	v.AutomaticEnv()
	v.SetEnvKeyReplacer(strings.NewReplacer(".", "_"))

	v.SetDefault("http_addr", ":8080")
	v.SetDefault("eth_rpc_http", "")
	v.SetDefault("eth_ws", "")

	var c Config
	c.HTTPAddr = v.GetString("http_addr")
	c.EthRPCHTTP = v.GetString("eth_rpc_http")
	c.EthWS = v.GetString("eth_ws")
	return &c, nil
}

type Config struct {
	HTTPAddr   string
	EthRPCHTTP string
	EthWS      string
}
