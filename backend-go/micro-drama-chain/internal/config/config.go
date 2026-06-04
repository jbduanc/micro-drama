package config

import (
	"fmt"
	"os"
	"strings"

	"github.com/spf13/viper"
)

type Config struct {
	GRPCAddr    string
	EthRPCHTTP  string
	EthWS       string
}

func Load() (*Config, error) {
	v := viper.New()
	v.SetDefault("grpc_addr", ":9092")
	v.SetDefault("eth_rpc_http", "")
	v.SetDefault("eth_ws", "")

	path := envFirst("CONFIG_FILE", "CHAIN_CONFIG_FILE")
	if path == "" {
		path = "/config/application.yaml"
	}
	v.SetConfigFile(path)
	v.SetConfigType("yaml")
	if err := v.ReadInConfig(); err != nil {
		local := "config/application.yaml"
		v.SetConfigFile(local)
		if err2 := v.ReadInConfig(); err2 != nil {
			return nil, fmt.Errorf("read config %q: %w", path, err)
		}
	}

	v.AutomaticEnv()
	v.SetEnvPrefix("CHAIN")
	v.SetEnvKeyReplacer(strings.NewReplacer(".", "_"))

	return &Config{
		GRPCAddr:   v.GetString("grpc_addr"),
		EthRPCHTTP: v.GetString("eth_rpc_http"),
		EthWS:      v.GetString("eth_ws"),
	}, nil
}

func envFirst(keys ...string) string {
	for _, k := range keys {
		if v := strings.TrimSpace(os.Getenv(k)); v != "" {
			return v
		}
	}
	return ""
}
