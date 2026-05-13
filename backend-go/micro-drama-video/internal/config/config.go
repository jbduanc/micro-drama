package config

import (
	"strings"

	"github.com/spf13/viper"
)

// Load reads VIDEO_* environment variables.
func Load() (*Config, error) {
	v := viper.New()
	v.SetEnvPrefix("VIDEO")
	v.AutomaticEnv()
	v.SetEnvKeyReplacer(strings.NewReplacer(".", "_"))

	v.SetDefault("kafka_enabled", true)
	v.SetDefault("kafka_brokers", "localhost:9092")
	v.SetDefault("kafka_consumer_group", "micro-drama-video")
	v.SetDefault("kafka_topic_upload_completed", "content.video.upload_completed")
	v.SetDefault("kafka_topic_transcode_completed", "video.transcode.completed")
	v.SetDefault("kafka_topic_transcode_failed", "video.transcode.failed")

	v.SetDefault("s3_endpoint", "")
	v.SetDefault("s3_access_key", "")
	v.SetDefault("s3_secret_key", "")
	v.SetDefault("s3_bucket", "")
	v.SetDefault("s3_use_ssl", true)
	v.SetDefault("s3_region", "")

	v.SetDefault("ffmpeg_path", "ffmpeg")
	v.SetDefault("work_dir", "/tmp/micro-drama-video")

	var c Config
	c.Kafka.Enabled = v.GetBool("kafka_enabled")
	c.Kafka.Brokers = splitCSV(v.GetString("kafka_brokers"))
	c.Kafka.ConsumerGroup = v.GetString("kafka_consumer_group")
	c.Kafka.TopicUploadCompleted = v.GetString("kafka_topic_upload_completed")
	c.Kafka.TopicTranscodeCompleted = v.GetString("kafka_topic_transcode_completed")
	c.Kafka.TopicTranscodeFailed = v.GetString("kafka_topic_transcode_failed")

	c.S3.Endpoint = v.GetString("s3_endpoint")
	c.S3.AccessKey = v.GetString("s3_access_key")
	c.S3.SecretKey = v.GetString("s3_secret_key")
	c.S3.Bucket = v.GetString("s3_bucket")
	c.S3.UseSSL = v.GetBool("s3_use_ssl")
	c.S3.Region = v.GetString("s3_region")

	c.Worker.FFmpegPath = v.GetString("ffmpeg_path")
	c.Worker.WorkDir = v.GetString("work_dir")
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

type Config struct {
	Kafka struct {
		Enabled                 bool
		Brokers                 []string
		ConsumerGroup           string
		TopicUploadCompleted    string
		TopicTranscodeCompleted string
		TopicTranscodeFailed    string
	}
	S3 struct {
		Endpoint  string
		AccessKey string
		SecretKey string
		Bucket    string
		UseSSL    bool
		Region    string
	}
	Worker struct {
		FFmpegPath string
		WorkDir    string
	}
}
