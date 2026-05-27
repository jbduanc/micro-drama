package kafka

import (
	"encoding/json"
	"fmt"
	"time"

	"github.com/IBM/sarama"
	"go.uber.org/zap"

	"micro-drama-transcoder/internal/config"
	"micro-drama-transcoder/internal/events"
)

type Producer struct {
	log  *zap.Logger
	cfg  *config.Config
	prod sarama.SyncProducer
}

func NewProducer(log *zap.Logger, cfg *config.Config) (*Producer, error) {
	if !cfg.Kafka.Enabled || len(cfg.Kafka.Brokers) == 0 {
		log.Warn("kafka producer disabled")
		return &Producer{log: log, cfg: cfg}, nil
	}
	saramaCfg := sarama.NewConfig()
	saramaCfg.Version = sarama.V2_8_0_0
	saramaCfg.Producer.Return.Successes = true

	prod, err := sarama.NewSyncProducer(cfg.Kafka.Brokers, saramaCfg)
	if err != nil {
		return nil, fmt.Errorf("kafka producer: %w", err)
	}
	return &Producer{log: log, cfg: cfg, prod: prod}, nil
}

func (p *Producer) Close() error {
	if p.prod == nil {
		return nil
	}
	return p.prod.Close()
}

func (p *Producer) PublishTranscodeCompleted(ev *events.VideoTranscodeCompletedEvent) error {
	if p.prod == nil {
		p.log.Warn("kafka skipped: producer not configured")
		return nil
	}
	body, err := json.Marshal(ev)
	if err != nil {
		return err
	}
	topic := p.cfg.Kafka.TopicTranscodeCompleted
	_, _, err = p.prod.SendMessage(&sarama.ProducerMessage{
		Topic:     topic,
		Key:       sarama.StringEncoder(ev.VideoID),
		Value:     sarama.ByteEncoder(body),
		Timestamp: time.Now(),
	})
	if err != nil {
		return fmt.Errorf("kafka send %s: %w", topic, err)
	}
	return nil
}

func (p *Producer) PublishTranscodeFailed(ev *events.VideoTranscodeFailedEvent) error {
	if p.prod == nil {
		p.log.Warn("kafka skipped: producer not configured")
		return nil
	}
	body, err := json.Marshal(ev)
	if err != nil {
		return err
	}
	topic := p.cfg.Kafka.TopicTranscodeFailed
	_, _, err = p.prod.SendMessage(&sarama.ProducerMessage{
		Topic:     topic,
		Key:       sarama.StringEncoder(ev.VideoID),
		Value:     sarama.ByteEncoder(body),
		Timestamp: time.Now(),
	})
	if err != nil {
		return fmt.Errorf("kafka send %s: %w", topic, err)
	}
	return nil
}
