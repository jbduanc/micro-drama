package config

import (
	"fmt"
	"os"
	"strings"

	"github.com/spf13/viper"
)

// loadFromFile 从外挂配置文件加载（Docker / K8s ConfigMap 挂载 /config/application.yaml）。
func loadFromFile(v *viper.Viper) error {
	path := configFilePath("CONFIG_FILE", "VIDEO_CONFIG_FILE")
	if path == "" {
		path = "/config/application.yaml"
	}
	if err := readConfigFile(v, path); err != nil {
		var lastErr = err
		for _, fallback := range []string{
			"/config/application.yml",
			"config/application.yaml",
			"config/application.yml",
		} {
			if fallback == path {
				continue
			}
			if err2 := readConfigFile(v, fallback); err2 == nil {
				lastErr = nil
				break
			} else {
				lastErr = err2
			}
		}
		if lastErr != nil {
			return fmt.Errorf("read config: %w", lastErr)
		}
	}
	v.AutomaticEnv()
	v.SetEnvPrefix("VIDEO")
	v.SetEnvKeyReplacer(strings.NewReplacer(".", "_"))
	return nil
}

func readConfigFile(v *viper.Viper, path string) error {
	v.SetConfigFile(path)
	ext := strings.TrimPrefix(strings.ToLower(path[strings.LastIndex(path, "."):]), ".")
	if ext == "yml" || ext == "yaml" {
		v.SetConfigType(ext)
	} else {
		v.SetConfigType("yaml")
	}
	return v.ReadInConfig()
}

func configFilePath(keys ...string) string {
	for _, k := range keys {
		if v := strings.TrimSpace(os.Getenv(k)); v != "" {
			return v
		}
	}
	return ""
}
