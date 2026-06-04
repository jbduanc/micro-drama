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
	v.SetConfigFile(path)
	v.SetConfigType("yaml")
	if err := v.ReadInConfig(); err != nil {
		v.SetConfigFile("config/application.yaml")
		if err2 := v.ReadInConfig(); err2 != nil {
			return fmt.Errorf("read config %q: %w", path, err)
		}
	}
	v.AutomaticEnv()
	v.SetEnvPrefix("VIDEO")
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
