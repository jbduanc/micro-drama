package config

import (
	"fmt"
	"os"
	"strings"

	"github.com/spf13/viper"
)

func loadFromFile(v *viper.Viper) error {
	path := configFilePath("CONFIG_FILE", "TRANSCODER_CONFIG_FILE")
	if path == "" {
		path = "/config/application.yaml"
	}
	v.SetConfigFile(path)
	v.SetConfigType("yaml")
	if err := v.ReadInConfig(); err != nil {
		v.SetConfigFile("config/application.yaml")
		if err2 := v.ReadInConfig(); err2 != nil {
			return fmt.Errorf("read config %q: %w", path, err)
		}
	}
	v.AutomaticEnv()
	v.SetEnvPrefix("TRANSCODER")
	v.SetEnvKeyReplacer(strings.NewReplacer(".", "_"))
	return nil
}

func configFilePath(keys ...string) string {
	for _, k := range keys {
		if v := strings.TrimSpace(os.Getenv(k)); v != "" {
			return v
		}
	}
	return ""
}
