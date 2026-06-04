package config

import (
	"fmt"
	"os"
	"strings"

	"github.com/spf13/viper"
	"go.uber.org/zap"

)

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
		GroupID                 string
		TopicUploadCompleted    string
		TopicTranscodeCompleted string
		TopicTranscodeFailed    string
	}

	OSS struct {
		Endpoint  string
		AccessKey string
		SecretKey string
		Bucket    string
		UseSSL    bool
		Region    string
	}

	Transcode struct {
		TempDir        string
		FFmpegPath     string
		SegmentSeconds int
		HlsPrefix      string
		Variants       []Variant
		KeepWorkDir    bool
	}
}

type Variant struct {
	Name        string
	Width       int
	Height      int
	BitrateKbps int
}

func Load(log *zap.Logger) (*Config, error) {
	if log == nil {
		log = zap.NewNop()
	}
	v := viper.New()
	setBootstrapDefaults(v)
	if err := loadFromFile(v); err != nil {
		return nil, err
	}
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
		zap.Bool("kafkaEnabled", c.Kafka.Enabled),
		zap.String("kafkaGroupId", c.Kafka.GroupID),
		zap.String("hlsPrefix", c.Transcode.HlsPrefix),
	)
	return c, nil
}

func (c *Config) Viper() *viper.Viper { return c.raw }

func setBootstrapDefaults(v *viper.Viper) {
	v.SetDefault("consul_enabled", true)
	v.SetDefault("consul_config_prefix", "config")
	v.SetDefault("consul_config_name", "micro-drama-transcoder")
	v.SetDefault("consul_config_data_key", "data")
	v.SetDefault("consul_config_fail_fast", true)

	v.SetDefault("consul_port", "8500")
	v.SetDefault("consul_token", "")
	v.SetDefault("consul_service_name", "micro-drama-transcoder")
	v.SetDefault("consul_health_interval", "10s")
	v.SetDefault("consul_discovery_enabled", true)
	v.SetDefault("consul_service_address", "")
}

func applyLocalConsulOverrides(v *viper.Viper) {
	if val, ok := envBool("TRANSCODER_CONSUL_ENABLED"); ok {
		v.Set("consul_enabled", val)
	}
	if val, ok := envBool("TRANSCODER_CONSUL_DISCOVERY_ENABLED"); ok {
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

	if v.GetBool("kafka_enabled") {
		if strings.TrimSpace(v.GetString("kafka_brokers")) == "" {
			missing = append(missing, "kafka_brokers")
		}
		require("kafka_group_id")
		if strings.TrimSpace(v.GetString("kafka_topic_upload_completed")) == "" {
			missing = append(missing, "kafka_topic_upload_completed")
		}
	}

	if len(missing) > 0 {
		return fmt.Errorf("missing required config in Consul KV (config/micro-drama-transcoder/data): %s", strings.Join(missing, ", "))
	}
	return nil
}

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
	c.Kafka.GroupID = strings.TrimSpace(v.GetString("kafka_group_id"))
	c.Kafka.TopicUploadCompleted = strings.TrimSpace(v.GetString("kafka_topic_upload_completed"))
	c.Kafka.TopicTranscodeCompleted = strings.TrimSpace(v.GetString("kafka_topic_transcode_completed"))
	c.Kafka.TopicTranscodeFailed = strings.TrimSpace(v.GetString("kafka_topic_transcode_failed"))
	if c.Kafka.TopicUploadCompleted == "" {
		c.Kafka.TopicUploadCompleted = "content.video.upload_completed"
	}
	if c.Kafka.TopicTranscodeCompleted == "" {
		c.Kafka.TopicTranscodeCompleted = "video.transcode.completed"
	}
	if c.Kafka.TopicTranscodeFailed == "" {
		c.Kafka.TopicTranscodeFailed = "video.transcode.failed"
	}

	c.OSS.Endpoint = v.GetString("oss_endpoint")
	c.OSS.AccessKey = v.GetString("oss_access_key")
	c.OSS.SecretKey = v.GetString("oss_secret_key")
	c.OSS.Bucket = v.GetString("oss_bucket")
	c.OSS.UseSSL = v.GetBool("oss_use_ssl")
	c.OSS.Region = v.GetString("oss_region")

	c.Transcode.TempDir = strings.TrimSpace(v.GetString("transcode_temp_dir"))
	if c.Transcode.TempDir == "" {
		c.Transcode.TempDir = os.TempDir()
	}
	c.Transcode.FFmpegPath = strings.TrimSpace(v.GetString("transcode_ffmpeg_path"))
	if c.Transcode.FFmpegPath == "" {
		c.Transcode.FFmpegPath = "ffmpeg"
	}
	c.Transcode.SegmentSeconds = v.GetInt("transcode_hls_segment_seconds")
	if c.Transcode.SegmentSeconds <= 0 {
		c.Transcode.SegmentSeconds = 6
	}
	c.Transcode.HlsPrefix = strings.Trim(v.GetString("transcode_hls_prefix"), "/")
	if c.Transcode.HlsPrefix == "" {
		c.Transcode.HlsPrefix = "hls"
	}
	c.Transcode.KeepWorkDir = v.GetBool("transcode_keep_work_dir")

	c.Transcode.Variants = parseVariants(v.GetString("transcode_variants"))
	if len(c.Transcode.Variants) == 0 {
		c.Transcode.Variants = []Variant{
			{Name: "480p", Width: 854, Height: 480, BitrateKbps: 800},
			{Name: "720p", Width: 1280, Height: 720, BitrateKbps: 2500},
			{Name: "1080p", Width: 1920, Height: 1080, BitrateKbps: 5000},
		}
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

// parseVariants parses: "480p:854x480:800,720p:1280x720:2500"
func parseVariants(s string) []Variant {
	s = strings.TrimSpace(s)
	if s == "" {
		return nil
	}
	items := strings.Split(s, ",")
	out := make([]Variant, 0, len(items))
	for _, it := range items {
		it = strings.TrimSpace(it)
		if it == "" {
			continue
		}
		parts := strings.Split(it, ":")
		if len(parts) != 3 {
			continue
		}
		name := strings.TrimSpace(parts[0])
		res := strings.TrimSpace(parts[1])
		br := strings.TrimSpace(parts[2])

		var w, h, kbps int
		if _, err := fmt.Sscanf(res, "%dx%d", &w, &h); err != nil {
			continue
		}
		if _, err := fmt.Sscanf(br, "%d", &kbps); err != nil {
			continue
		}
		if name == "" || w <= 0 || h <= 0 || kbps <= 0 {
			continue
		}
		out = append(out, Variant{Name: name, Width: w, Height: h, BitrateKbps: kbps})
	}
	return out
}
