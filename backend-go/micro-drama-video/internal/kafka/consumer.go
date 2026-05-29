package kafka

import (
	"context"
	"fmt"
	"strings"

	"github.com/IBM/sarama"
	"go.uber.org/zap"

	"micro-drama-video/internal/config"
)

// TranscodeHandler 转码结果消息处理函数。
type TranscodeHandler func(ctx context.Context, payload []byte) error

// Consumer 消费 transcoder 发出的转码完成/失败事件。
type Consumer struct {
	log      *zap.Logger
	cfg      *config.Config
	group    sarama.ConsumerGroup
	handlers map[string]TranscodeHandler
}

// NewConsumer 创建 Kafka 消费者；kafka 未启用时返回 nil。
func NewConsumer(log *zap.Logger, cfg *config.Config) (*Consumer, error) {
	if !cfg.Kafka.Enabled || len(cfg.Kafka.Brokers) == 0 {
		log.Warn("kafka consumer disabled")
		return nil, nil
	}
	saramaCfg := sarama.NewConfig()
	saramaCfg.Version = sarama.V2_8_0_0
	saramaCfg.Consumer.Group.Rebalance.Strategy = sarama.NewBalanceStrategyRoundRobin()
	saramaCfg.Consumer.Offsets.Initial = sarama.OffsetNewest

	group, err := sarama.NewConsumerGroup(cfg.Kafka.Brokers, cfg.Kafka.ConsumerGroup, saramaCfg)
	if err != nil {
		return nil, fmt.Errorf("kafka consumer group: %w", err)
	}
	return &Consumer{
		log:   log,
		cfg:   cfg,
		group: group,
		handlers: map[string]TranscodeHandler{
			cfg.Kafka.TopicTranscodeCompleted: nil,
			cfg.Kafka.TopicTranscodeFailed:    nil,
		},
	}, nil
}

// RegisterHandlers 注册各 topic 的处理函数。
func (c *Consumer) RegisterHandlers(completed, failed TranscodeHandler) {
	if c == nil {
		return
	}
	c.handlers[c.cfg.Kafka.TopicTranscodeCompleted] = completed
	c.handlers[c.cfg.Kafka.TopicTranscodeFailed] = failed
}

// Run 阻塞消费，ctx 取消时退出。
func (c *Consumer) Run(ctx context.Context) error {
	if c == nil || c.group == nil {
		return nil
	}
	topics := make([]string, 0, len(c.handlers))
	for t := range c.handlers {
		if strings.TrimSpace(t) != "" {
			topics = append(topics, t)
		}
	}
	if len(topics) == 0 {
		return nil
	}
	h := &consumerGroupHandler{parent: c}
	for {
		if err := c.group.Consume(ctx, topics, h); err != nil {
			return err
		}
		if ctx.Err() != nil {
			return ctx.Err()
		}
	}
}

// Close 关闭消费者。
func (c *Consumer) Close() error {
	if c == nil || c.group == nil {
		return nil
	}
	return c.group.Close()
}

type consumerGroupHandler struct {
	parent *Consumer
}

func (h *consumerGroupHandler) Setup(_ sarama.ConsumerGroupSession) error   { return nil }
func (h *consumerGroupHandler) Cleanup(_ sarama.ConsumerGroupSession) error { return nil }

func (h *consumerGroupHandler) ConsumeClaim(sess sarama.ConsumerGroupSession, claim sarama.ConsumerGroupClaim) error {
	for msg := range claim.Messages() {
		topic := msg.Topic
		handler := h.parent.handlers[topic]
		if handler != nil {
			if err := handler(sess.Context(), msg.Value); err != nil {
				h.parent.log.Error("kafka message handler failed",
					zap.String("topic", topic),
					zap.Error(err),
				)
			}
		}
		sess.MarkMessage(msg, "")
	}
	return nil
}
