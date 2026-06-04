package repository

import (
	"context"
	"sync"
)

type Order struct {
	ID     string
	Status string
}

type OrderRepository interface {
	Save(ctx context.Context, o *Order) error
	Get(ctx context.Context, id string) (*Order, error)
}

type MemoryRepo struct {
	mu sync.RWMutex
	m  map[string]*Order
}

func NewMemoryRepo() *MemoryRepo {
	return &MemoryRepo{m: make(map[string]*Order)}
}

func (r *MemoryRepo) Save(ctx context.Context, o *Order) error {
	_ = ctx
	r.mu.Lock()
	defer r.mu.Unlock()
	r.m[o.ID] = o
	return nil
}

func (r *MemoryRepo) Get(ctx context.Context, id string) (*Order, error) {
	_ = ctx
	r.mu.RLock()
	defer r.mu.RUnlock()
	if o, ok := r.m[id]; ok {
		return o, nil
	}
	return nil, nil
}
