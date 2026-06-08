// Package config 负责加载应用配置（外挂 YAML + 环境变量，无 Consul）。
package config

import (
	"fmt"
	"os"
	"strings"

	"github.com/spf13/viper"
	"go.uber.org/zap"
)

// Load 加载配置：外挂 YAML → 校验 → 映射结构体。
func Load(log *zap.Logger) (*Config, error) {
	if log == nil {
		log = zap.NewNop()
	}
	v := viper.New()
	setBootstrapDefaults(v)
	if err := loadFromFile(v); err != nil {
		return nil, err
	}
	if err := validateRequired(v); err != nil {
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

// validateRequired 校验必须包含的业务配置项。
func validateRequired(v *viper.Viper) error {
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
		return fmt.Errorf("missing required config in application.yaml: %s", strings.Join(missing, ", "))
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
	c.Kafka.TopicTranscodeCompleted = v.GetString("kafka_topic_transcode_completed")
	c.Kafka.TopicTranscodeFailed = v.GetString("kafka_topic_transcode_failed")
	c.Kafka.ConsumerGroup = v.GetString("kafka_consumer_group")

	c.OSS.Endpoint = v.GetString("oss_endpoint")
	c.OSS.AccessKey = v.GetString("oss_access_key")
	c.OSS.SecretKey = v.GetString("oss_secret_key")
	c.OSS.Bucket = v.GetString("oss_bucket")
	c.OSS.UseSSL = v.GetBool("oss_use_ssl")
	c.OSS.Region = v.GetString("oss_region")
	c.OSS.UploadPrefix = strings.Trim(v.GetString("oss_upload_prefix"), "/")
	c.OSS.UploadPresignExpireSeconds = v.GetInt("oss_upload_presign_expire_seconds")
	c.OSS.HLSPrefix = strings.Trim(v.GetString("oss_hls_prefix"), "/")
	c.OSS.STSRoleARN = v.GetString("oss_sts_role_arn")
	c.OSS.STSRegion = v.GetString("oss_sts_region")
	c.OSS.STSSessionName = v.GetString("oss_sts_session_name")
	c.OSS.STSDurationSeconds = v.GetInt("oss_sts_duration_seconds")
	c.OSS.EventCallbackSecret = v.GetString("oss_event_callback_secret")
	c.OSS.UploadCallbackBaseURL = strings.TrimRight(v.GetString("oss_upload_callback_base_url"), "/")

	c.Playback.URLExpireSeconds = v.GetInt("playback_url_expire_seconds")
	c.Playback.HLSKeyTemplate = v.GetString("playback_hls_key_template")
	c.Playback.RequireReady = v.GetBool("playback_require_ready")
	c.Playback.TokenSecret = v.GetString("playback_token_secret")
	c.Playback.PublicBaseURL = strings.TrimRight(v.GetString("playback_public_base_url"), "/")

	c.DB.DSNRaw = v.GetString("db_dsn")
	c.DB.Host = v.GetString("db_host")
	c.DB.Port = v.GetInt("db_port")
	c.DB.User = v.GetString("db_user")
	c.DB.Password = v.GetString("db_password")
	c.DB.Name = v.GetString("db_name")
	c.DB.SSLMode = v.GetString("db_sslmode")

	c.JWT.Secret = firstNonEmpty(
		v.GetString("jwt_secret"),
		os.Getenv("JWT_SECRET"),
	)

	c.Auth.GatewayMode = firstNonEmpty(
		v.GetString("auth_gateway_mode"),
		os.Getenv("AUTH_GATEWAY_MODE"),
	)
	c.Redis.Host = firstNonEmpty(v.GetString("redis_host"), os.Getenv("REDIS_HOST"), "localhost")
	c.Redis.Port = v.GetInt("redis_port")
	c.Redis.Password = firstNonEmpty(v.GetString("redis_password"), os.Getenv("REDIS_PASSWORD"))
	c.Redis.Database = v.GetInt("redis_database")

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
	if c.OSS.STSDurationSeconds <= 0 {
		c.OSS.STSDurationSeconds = 3600
	}
	if c.Kafka.TopicUploadCompleted == "" {
		c.Kafka.TopicUploadCompleted = "content.video.upload_completed"
	}
	if c.Kafka.TopicTranscodeCompleted == "" {
		c.Kafka.TopicTranscodeCompleted = "video.transcode.completed"
	}
	if c.Kafka.TopicTranscodeFailed == "" {
		c.Kafka.TopicTranscodeFailed = "video.transcode.failed"
	}
	if c.Kafka.ConsumerGroup == "" {
		c.Kafka.ConsumerGroup = "micro-drama-video"
	}
	if c.Redis.Port == 0 {
		c.Redis.Port = 6379
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
		Enabled                 bool
		Brokers                 []string
		TopicUploadCompleted    string
		TopicTranscodeCompleted string
		TopicTranscodeFailed    string
		ConsumerGroup           string
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
		STSRoleARN                 string // RAM 角色 ARN，STS 直传必填
		STSRegion                  string // STS API 区域，默认同 oss_region
		STSSessionName             string
		STSDurationSeconds         int
		EventCallbackSecret        string // 可选：MNS/事件通知全局密钥（上传回调已改用一次性 token）
		UploadCallbackBaseURL      string // 公网 video-api 根路径，如 https://api.xxx.com/video-api
	}

	Playback struct {
		URLExpireSeconds int
		HLSKeyTemplate   string
		RequireReady     bool
		TokenSecret      string // 播放 token 签名密钥
		PublicBaseURL    string // 公网播放域名（CDN/Cloudflare），如 https://video.dramadjbo.com
	}

	JWT struct {
		Secret string // 与 Java 服务 jwt.secret 一致，本地直连时校验 Bearer
	}

	Auth struct {
		GatewayMode string // off | gateway；gateway 时信任 ForwardAuth 头并查 Redis 会话
	}

	Redis struct {
		Host     string
		Port     int
		Password string
		Database int
	}

	DB DBConfig
}

func firstNonEmpty(values ...string) string {
	for _, s := range values {
		if strings.TrimSpace(s) != "" {
			return strings.TrimSpace(s)
		}
	}
	return ""
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
