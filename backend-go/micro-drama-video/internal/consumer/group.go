package consumer

import (
	"context"
	"encoding/json"
	"sync"

	"github.com/IBM/sarama"
	"go.uber.org/zap"

	"micro-drama-video/internal/config"
	"micro-drama-video/internal/events"
	"micro-drama-video/internal/worker"
)

type Handler struct {
	log    *zap.Logger
	cfg    *config.Config
	prod   sarama.SyncProducer
	onDone func(*events.VideoTranscodeCompletedEvent)
	onFail func(*events.VideoTranscodeFailedEvent)
}

func NewHandler(log *zap.Logger, cfg *config.Config, prod sarama.SyncProducer) *Handler {
	return &Handler{log: log, cfg: cfg, prod: prod}
}

func (h *Handler) Setup(sarama.ConsumerGroupSession) error   { return nil }
func (h *Handler) Cleanup(sarama.ConsumerGroupSession) error { return nil }

func (h *Handler) ConsumeClaim(sess sarama.ConsumerGroupSession, claim sarama.ConsumerGroupClaim) error {
	for msg := range claim.Messages() {
		if msg.Topic != h.cfg.Kafka.TopicUploadCompleted {
			sess.MarkMessage(msg, "")
			continue
		}
		var ev events.VideoUploadCompletedEvent
		if err := json.Unmarshal(msg.Value, &ev); err != nil {
			h.log.Error("unmarshal event", zap.Error(err), zap.ByteString("raw", msg.Value))
			sess.MarkMessage(msg, "")
			continue
		}
		done, fail := worker.ProcessUploadCompleted(context.Background(), h.log, h.cfg, &ev)
		if fail != nil {
			h.publishJSON(h.cfg.Kafka.TopicTranscodeFailed, fail)
		} else {
			h.publishJSON(h.cfg.Kafka.TopicTranscodeCompleted, done)
		}
		sess.MarkMessage(msg, "")
	}
	return nil
}

func (h *Handler) publishJSON(topic string, v any) {
	if h.prod == nil || topic == "" {
		return
	}
	b, err := json.Marshal(v)
	if err != nil {
		h.log.Error("marshal out event", zap.Error(err))
		return
	}
	_, _, err = h.prod.SendMessage(&sarama.ProducerMessage{Topic: topic, Value: sarama.ByteEncoder(b)})
	if err != nil {
		h.log.Error("kafka produce", zap.String("topic", topic), zap.Error(err))
	}
}

func RunGroup(ctx context.Context, log *zap.Logger, cfg *config.Config, wg *sync.WaitGroup) error {
	saramaCfg := sarama.NewConfig()
	saramaCfg.Version = sarama.V2_8_0_0
	saramaCfg.Consumer.Group.Rebalance.Strategy = sarama.NewBalanceStrategyRoundRobin()
	saramaCfg.Consumer.Offsets.Initial = sarama.OffsetOldest
	saramaCfg.Producer.Return.Successes = true

	prod, err := sarama.NewSyncProducer(cfg.Kafka.Brokers, saramaCfg)
	if err != nil {
		log.Warn("kafka producer disabled", zap.Error(err))
		prod = nil
	}

	group, err := sarama.NewConsumerGroup(cfg.Kafka.Brokers, cfg.Kafka.ConsumerGroup, saramaCfg)
	if err != nil {
		return err
	}
	handler := NewHandler(log, cfg, prod)

	wg.Add(1)
	go func() {
		defer wg.Done()
		for {
			if err := group.Consume(ctx, []string{cfg.Kafka.TopicUploadCompleted}, handler); err != nil {
				log.Error("consume", zap.Error(err))
			}
			if ctx.Err() != nil {
				return
			}
		}
	}()
	return nil
}
