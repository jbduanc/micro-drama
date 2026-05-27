package kafka

import (
	"context"
	"encoding/json"
	"fmt"
	"sync/atomic"
	"time"

	"github.com/IBM/sarama"
	"go.uber.org/zap"

	"micro-drama-transcoder/internal/config"
	"micro-drama-transcoder/internal/events"
)

type MessageHandler func(ctx context.Context, ev *events.VideoUploadCompletedEvent) error

type Consumer struct {
	log     *zap.Logger
	cfg     *config.Config
	group   sarama.ConsumerGroup
	started atomic.Bool
}

func NewConsumer(log *zap.Logger, cfg *config.Config) (*Consumer, error) {
	if !cfg.Kafka.Enabled || len(cfg.Kafka.Brokers) == 0 {
		log.Warn("kafka consumer disabled")
		return &Consumer{log: log, cfg: cfg}, nil
	}
	saramaCfg := sarama.NewConfig()
	saramaCfg.Version = sarama.V2_8_0_0
	saramaCfg.Consumer.Offsets.Initial = sarama.OffsetOldest
	saramaCfg.Consumer.Group.Rebalance.Strategy = sarama.NewBalanceStrategyRange()
	saramaCfg.Consumer.Group.Session.Timeout = 30 * time.Second
	saramaCfg.Consumer.Group.Heartbeat.Interval = 3 * time.Second

	group, err := sarama.NewConsumerGroup(cfg.Kafka.Brokers, cfg.Kafka.GroupID, saramaCfg)
	if err != nil {
		return nil, fmt.Errorf("kafka consumer group: %w", err)
	}
	return &Consumer{log: log, cfg: cfg, group: group}, nil
}

func (c *Consumer) Close() error {
	if c.group == nil {
		return nil
	}
	return c.group.Close()
}

func (c *Consumer) Run(ctx context.Context, handler MessageHandler) error {
	if c.group == nil {
		c.log.Warn("kafka run skipped: consumer not configured")
		<-ctx.Done()
		return ctx.Err()
	}
	if handler == nil {
		return fmt.Errorf("handler is required")
	}
	c.started.Store(true)

	topics := []string{c.cfg.Kafka.TopicUploadCompleted}
	h := &groupHandler{log: c.log, handler: handler}
	for {
		if err := c.group.Consume(ctx, topics, h); err != nil {
			if ctx.Err() != nil {
				return ctx.Err()
			}
			c.log.Error("kafka consume loop error", zap.Error(err))
			time.Sleep(2 * time.Second)
			continue
		}
		if ctx.Err() != nil {
			return ctx.Err()
		}
	}
}

type groupHandler struct {
	log     *zap.Logger
	handler MessageHandler
}

func (h *groupHandler) Setup(_ sarama.ConsumerGroupSession) error   { return nil }
func (h *groupHandler) Cleanup(_ sarama.ConsumerGroupSession) error { return nil }

func (h *groupHandler) ConsumeClaim(session sarama.ConsumerGroupSession, claim sarama.ConsumerGroupClaim) error {
	for msg := range claim.Messages() {
		var ev events.VideoUploadCompletedEvent
		if err := json.Unmarshal(msg.Value, &ev); err != nil {
			h.log.Error("kafka message json unmarshal failed",
				zap.String("topic", msg.Topic),
				zap.Int32("partition", msg.Partition),
				zap.Int64("offset", msg.Offset),
				zap.Error(err),
			)
			// poison message: commit to avoid infinite retry storm
			session.MarkMessage(msg, "bad_json")
			continue
		}

		if err := h.handler(session.Context(), &ev); err != nil {
			h.log.Error("transcode handler failed",
				zap.String("videoId", ev.VideoID),
				zap.String("sourceObjectKey", ev.SourceObjectKey),
				zap.Error(err),
			)
			// do not commit -> let it retry (at-least-once)
			continue
		}

		session.MarkMessage(msg, "ok")
	}
	return nil
}
