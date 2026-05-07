package repository

import (
	"context"
	"sync"
)

// Order is a minimal placeholder; replace with PostgreSQL / GORM.
type Order struct {
	ID     string `json:"id"`
	TxHash string `json:"txHash,omitempty"`
	Status string `json:"status"`
}

type OrderRepository interface {
	Save(ctx context.Context, o *Order) error
	Get(ctx context.Context, id string) (*Order, error)
}

type memoryRepo struct {
	mu sync.RWMutex
	m  map[string]*Order
}

func NewMemoryRepo() OrderRepository {
	return &memoryRepo{m: make(map[string]*Order)}
}

func (r *memoryRepo) Save(ctx context.Context, o *Order) error {
	_ = ctx
	r.mu.Lock()
	defer r.mu.Unlock()
	r.m[o.ID] = o
	return nil
}

func (r *memoryRepo) Get(ctx context.Context, id string) (*Order, error) {
	_ = ctx
	r.mu.RLock()
	defer r.mu.RUnlock()
	return r.m[id], nil
}
