// Package config 负责加载应用配置。
//
// 约定：业务配置（HTTP/OSS/DB/Kafka 等）只来自 Consul KV：config/micro-drama-video/data
// 本地/容器仅通过环境变量提供 Consul 连接信息：
//   - CONSUL_HOST、CONSUL_PORT、CONSUL_TOKEN
//   - VIDEO_CONSUL_ENABLED（可选，默认 true）
//   - VIDEO_CONSUL_DISCOVERY_ENABLED（可选，本地调试建议 false）
package config

import (
	"fmt"
	"os"
	"strings"

	"github.com/spf13/viper"
	"go.uber.org/zap"

	"micro-drama-video/internal/consul"
)

// Load 加载配置：引导项 → 拉取 Consul KV → 校验必填项 → 映射结构体。
func Load(log *zap.Logger) (*Config, error) {
	if log == nil {
		log = zap.NewNop()
	}
	v := viper.New()
	setBootstrapDefaults(v)
	applyLocalConsulOverrides(v)

	if !v.GetBool("consul_enabled") {
		return nil, fmt.Errorf("consul_enabled must be true: business config is only loaded from Consul KV")
	}

	if err := consul.MergeRemoteConfig(v, log); err != nil {
		return nil, err
	}
	applyLocalConsulOverrides(v)

	if err := validateAfterConsul(v); err != nil {
		return nil, err
	}

	c, err := unmarshal(v)
	if err != nil {
		return nil, err
	}
	c.raw = v

	log.Info("application config ready",
		zap.String("httpAddr", c.HTTPAddr),
		zap.String("ossEndpoint", c.OSS.Endpoint),
		zap.String("ossBucket", c.OSS.Bucket),
		zap.String("dbName", c.DB.Name),
		zap.Bool("kafkaEnabled", c.Kafka.Enabled),
		zap.Bool("consulDiscovery", c.Consul.DiscoveryEnabled),
	)
	return c, nil
}

// Viper 返回底层 Viper，供 Consul 服务注册读取 discovery 等开关。
func (c *Config) Viper() *viper.Viper {
	return c.raw
}

// setBootstrapDefaults 仅保留连接 Consul 之前必须的默认值，不含业务配置。
func setBootstrapDefaults(v *viper.Viper) {
	v.SetDefault("consul_enabled", true)
	v.SetDefault("consul_config_prefix", "config")
	v.SetDefault("consul_config_name", "micro-drama-video")
	v.SetDefault("consul_config_data_key", "data")
	v.SetDefault("consul_config_fail_fast", true)

	v.SetDefault("consul_port", "8500")
	v.SetDefault("consul_token", "")
	v.SetDefault("consul_service_name", "micro-drama-video")
	v.SetDefault("consul_health_interval", "10s")
	v.SetDefault("consul_discovery_enabled", true)
	v.SetDefault("consul_service_address", "")
}

// applyLocalConsulOverrides 仅允许本地覆盖 Consul 连接与开关（不读取 VIDEO_OSS_* 等业务环境变量）。
func applyLocalConsulOverrides(v *viper.Viper) {
	if val, ok := envBool("VIDEO_CONSUL_ENABLED"); ok {
		v.Set("consul_enabled", val)
	}
	if val, ok := envBool("VIDEO_CONSUL_DISCOVERY_ENABLED"); ok {
		v.Set("consul_discovery_enabled", val)
	}
	if s := envFirst("CONSUL_HOST", "SPRING_CLOUD_CONSUL_HOST"); s != "" {
		v.Set("consul_host", s)
	}
	if s := envFirst("CONSUL_PORT", "SPRING_CLOUD_CONSUL_PORT"); s != "" {
		v.Set("consul_port", s)
	}
	if s := envFirst("CONSUL_TOKEN", "SPRING_CLOUD_CONSUL_TOKEN"); s != "" {
		v.Set("consul_token", s)
	}
}

func envFirst(keys ...string) string {
	for _, k := range keys {
		if v := strings.TrimSpace(os.Getenv(k)); v != "" {
			return v
		}
	}
	return ""
}

func envBool(key string) (bool, bool) {
	v := strings.TrimSpace(os.Getenv(key))
	if v == "" {
		return false, false
	}
	switch strings.ToLower(v) {
	case "1", "true", "yes", "on":
		return true, true
	case "0", "false", "no", "off":
		return false, true
	default:
		return false, false
	}
}

// validateAfterConsul 校验 Consul KV 中必须包含的业务配置项。
func validateAfterConsul(v *viper.Viper) error {
	missing := make([]string, 0)

	require := func(key string) {
		if strings.TrimSpace(v.GetString(key)) == "" {
			missing = append(missing, key)
		}
	}

	require("http_addr")
	require("oss_endpoint")
	require("oss_access_key")
	require("oss_secret_key")
	require("oss_bucket")

	if strings.TrimSpace(v.GetString("db_dsn")) == "" {
		require("db_host")
		require("db_name")
		require("db_user")
	}

	if v.GetBool("kafka_enabled") {
		if strings.TrimSpace(v.GetString("kafka_brokers")) == "" {
			missing = append(missing, "kafka_brokers")
		}
		if strings.TrimSpace(v.GetString("kafka_topic_upload_completed")) == "" {
			missing = append(missing, "kafka_topic_upload_completed")
		}
	}

	if len(missing) > 0 {
		return fmt.Errorf("missing required config in Consul KV (config/micro-drama-video/data): %s", strings.Join(missing, ", "))
	}
	return nil
}

// unmarshal 将 Viper 键值映射到 Config 结构体。
func unmarshal(v *viper.Viper) (*Config, error) {
	var c Config
	c.HTTPAddr = v.GetString("http_addr")

	c.Consul.Enabled = v.GetBool("consul_enabled")
	c.Consul.Host = v.GetString("consul_host")
	c.Consul.Port = v.GetString("consul_port")
	c.Consul.Token = v.GetString("consul_token")
	c.Consul.ConfigPrefix = v.GetString("consul_config_prefix")
	c.Consul.ConfigName = v.GetString("consul_config_name")
	c.Consul.ConfigDataKey = v.GetString("consul_config_data_key")
	c.Consul.ConfigFailFast = v.GetBool("consul_config_fail_fast")
	c.Consul.DiscoveryEnabled = v.GetBool("consul_discovery_enabled")
	c.Consul.ServiceName = v.GetString("consul_service_name")
	c.Consul.ServiceAddress = v.GetString("consul_service_address")
	c.Consul.HealthInterval = v.GetString("consul_health_interval")

	c.Kafka.Enabled = v.GetBool("kafka_enabled")
	c.Kafka.Brokers = splitCSV(v.GetString("kafka_brokers"))
	c.Kafka.TopicUploadCompleted = v.GetString("kafka_topic_upload_completed")

	c.OSS.Endpoint = v.GetString("oss_endpoint")
	c.OSS.AccessKey = v.GetString("oss_access_key")
	c.OSS.SecretKey = v.GetString("oss_secret_key")
	c.OSS.Bucket = v.GetString("oss_bucket")
	c.OSS.UseSSL = v.GetBool("oss_use_ssl")
	c.OSS.Region = v.GetString("oss_region")
	c.OSS.UploadPrefix = strings.Trim(v.GetString("oss_upload_prefix"), "/")
	c.OSS.UploadPresignExpireSeconds = v.GetInt("oss_upload_presign_expire_seconds")
	c.OSS.HLSPrefix = strings.Trim(v.GetString("oss_hls_prefix"), "/")

	c.Playback.URLExpireSeconds = v.GetInt("playback_url_expire_seconds")
	c.Playback.HLSKeyTemplate = v.GetString("playback_hls_key_template")
	c.Playback.RequireReady = v.GetBool("playback_require_ready")
	c.Playback.TokenSecret = v.GetString("playback_token_secret")

	c.DB.DSNRaw = v.GetString("db_dsn")
	c.DB.Host = v.GetString("db_host")
	c.DB.Port = v.GetInt("db_port")
	c.DB.User = v.GetString("db_user")
	c.DB.Password = v.GetString("db_password")
	c.DB.Name = v.GetString("db_name")
	c.DB.SSLMode = v.GetString("db_sslmode")

	if c.Playback.URLExpireSeconds <= 0 {
		c.Playback.URLExpireSeconds = 3600
	}
	if c.Playback.HLSKeyTemplate == "" {
		c.Playback.HLSKeyTemplate = "hls/{dramaId}/{episodeId}/index.m3u8"
	}
	if c.OSS.UploadPresignExpireSeconds <= 0 {
		c.OSS.UploadPresignExpireSeconds = 3600
	}
	if c.OSS.HLSPrefix == "" {
		c.OSS.HLSPrefix = "hls"
	}
	if c.DB.Port == 0 {
		c.DB.Port = 5432
	}
	if c.DB.SSLMode == "" {
		c.DB.SSLMode = "disable"
	}
	if c.OSS.UploadPrefix == "" {
		c.OSS.UploadPrefix = "raw"
	}
	if c.Kafka.TopicUploadCompleted == "" {
		c.Kafka.TopicUploadCompleted = "content.video.upload_completed"
	}

	if c.DB.DSN() == "" {
		return nil, fmt.Errorf("database config incomplete in Consul KV (set db_dsn or db_host/db_name/db_user)")
	}

	return &c, nil
}

func splitCSV(s string) []string {
	parts := strings.Split(s, ",")
	out := make([]string, 0, len(parts))
	for _, p := range parts {
		p = strings.TrimSpace(p)
		if p != "" {
			out = append(out, p)
		}
	}
	return out
}

// Config 应用运行时配置。
type Config struct {
	raw      *viper.Viper
	HTTPAddr string

	Consul struct {
		Enabled          bool
		Host             string
		Port             string
		Token            string
		ConfigPrefix     string
		ConfigName       string
		ConfigDataKey    string
		ConfigFailFast   bool
		DiscoveryEnabled bool
		ServiceName      string
		ServiceAddress   string
		HealthInterval   string
	}

	Kafka struct {
		Enabled              bool
		Brokers              []string
		TopicUploadCompleted string
	}

	OSS struct {
		Endpoint                   string
		AccessKey                  string
		SecretKey                  string
		Bucket                     string
		UseSSL                     bool
		Region                     string
		UploadPrefix               string // 原片前缀，默认 raw → raw/{dramaId}/{episodeId}.mp4
		UploadPresignExpireSeconds int
		HLSPrefix                  string // HLS 前缀，默认 hls
	}

	Playback struct {
		URLExpireSeconds int
		HLSKeyTemplate   string
		RequireReady     bool
		TokenSecret      string // 播放 token 签名密钥
	}

	DB DBConfig
}

// DBConfig 数据库连接配置。
type DBConfig struct {
	DSNRaw   string
	Host     string
	Port     int
	User     string
	Password string
	Name     string
	SSLMode  string
}

// DSN 返回 pgx 连接字符串。
func (d DBConfig) DSN() string {
	if strings.TrimSpace(d.DSNRaw) != "" {
		return strings.TrimSpace(d.DSNRaw)
	}
	if d.Host == "" || d.Name == "" {
		return ""
	}
	port := d.Port
	if port == 0 {
		port = 5432
	}
	sslmode := d.SSLMode
	if sslmode == "" {
		sslmode = "disable"
	}
	return fmt.Sprintf("host=%s port=%d user=%s password=%s dbname=%s sslmode=%s",
		d.Host, port, d.User, d.Password, d.Name, sslmode)
}
