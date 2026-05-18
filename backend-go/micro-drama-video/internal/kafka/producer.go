// Package kafka 封装 Kafka 消息发送（本服务仅生产者，不消费）。
//
// 使用 IBM Sarama 库，与 Java Spring Kafka 使用同一套 Kafka 协议。
// 上传成功后发送 VideoUploadCompletedEvent，由 micro-drama-transcoder 消费转码。
package kafka

import (
	"encoding/json"
	"fmt"
	"time"

	"github.com/IBM/sarama"
	"go.uber.org/zap"

	"micro-drama-video/internal/config"
	"micro-drama-video/internal/events"
)

// Producer Kafka 同步生产者包装。
// SyncProducer：发送后等待 broker 确认，适合上传完成这类必须可靠投递的场景。
type Producer struct {
	log  *zap.Logger
	cfg  *config.Config
	prod sarama.SyncProducer // nil 表示 Kafka 未启用或连接失败时降级
}

// NewProducer 创建 Kafka 生产者。
//
// 当 kafka_enabled=false 或 brokers 为空时，返回 prod=nil 的 Producer，Publish 时仅打日志不报错。
func NewProducer(log *zap.Logger, cfg *config.Config) (*Producer, error) {
	if !cfg.Kafka.Enabled || len(cfg.Kafka.Brokers) == 0 {
		log.Warn("kafka producer disabled")
		return &Producer{log: log, cfg: cfg}, nil
	}

	saramaCfg := sarama.NewConfig()
	saramaCfg.Version = sarama.V2_8_0_0 // 与集群版本匹配，避免协议不兼容
	// 同步生产者必须开启，否则 SendMessage 无法获知是否成功。
	saramaCfg.Producer.Return.Successes = true

	prod, err := sarama.NewSyncProducer(cfg.Kafka.Brokers, saramaCfg)
	if err != nil {
		return nil, fmt.Errorf("kafka producer: %w", err)
	}
	return &Producer{log: log, cfg: cfg, prod: prod}, nil
}

// Close 关闭与 Kafka 的连接，释放网络资源；main 中 defer 调用。
func (p *Producer) Close() error {
	if p.prod == nil {
		return nil
	}
	return p.prod.Close()
}

// PublishUploadCompleted 向 topic content.video.upload_completed 发送上传完成事件。
//
// 消息 Key 使用 videoId，保证同一视频的相关消息进入同一分区（若消费者按 key 有序处理）。
// Value 为 JSON，字段名与 Java VideoUploadCompletedEvent 一致。
func (p *Producer) PublishUploadCompleted(ev *events.VideoUploadCompletedEvent) error {
	if p.prod == nil {
		p.log.Warn("kafka skipped: producer not configured")
		return nil
	}
	if ev.UploadedAt == nil {
		ev.UploadedAt = time.Now().UnixMilli()
	}
	body, err := json.Marshal(ev)
	if err != nil {
		return err
	}
	topic := p.cfg.Kafka.TopicUploadCompleted
	_, _, err = p.prod.SendMessage(&sarama.ProducerMessage{
		Topic: topic,
		Key:   sarama.StringEncoder(ev.VideoID), // 分区键
		Value: sarama.ByteEncoder(body),          // 消息体 JSON
	})
	if err != nil {
		return fmt.Errorf("kafka send %s: %w", topic, err)
	}
	p.log.Info("kafka event published",
		zap.String("topic", topic),
		zap.String("videoId", ev.VideoID),
	)
	return nil
}
