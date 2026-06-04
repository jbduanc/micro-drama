package grpcserver

import (
	"context"

	"go.uber.org/zap"
	"google.golang.org/grpc/codes"
	"google.golang.org/grpc/status"
	"google.golang.org/protobuf/types/known/emptypb"

	chainv1 "micro-drama-chain/api/gen/chain/v1"
	"micro-drama-chain/internal/service"
)

type Server struct {
	chainv1.UnimplementedChainServiceServer
	log *zap.Logger
	svc *service.ChainService
}

func New(log *zap.Logger, svc *service.ChainService) *Server {
	return &Server{log: log, svc: svc}
}

func (s *Server) Health(ctx context.Context, _ *emptypb.Empty) (*chainv1.HealthResponse, error) {
	_ = ctx
	return &chainv1.HealthResponse{
		Ok:           true,
		EthConnected: boolStr(s.svc.EthConnected()),
	}, nil
}

func (s *Server) CreatePendingOrder(ctx context.Context, req *chainv1.CreateOrderRequest) (*chainv1.OrderResponse, error) {
	if req == nil || req.OrderId == "" {
		return nil, status.Error(codes.InvalidArgument, "order_id required")
	}
	o, err := s.svc.CreatePendingOrder(ctx, req.OrderId)
	if err != nil {
		return nil, status.Errorf(codes.Internal, "create order: %v", err)
	}
	return &chainv1.OrderResponse{Id: o.ID, Status: o.Status}, nil
}

func (s *Server) SendRawTransaction(ctx context.Context, req *chainv1.SendRawTransactionRequest) (*chainv1.SendRawTransactionResponse, error) {
	if req == nil || req.SignedHex == "" {
		return nil, status.Error(codes.InvalidArgument, "signed_hex required")
	}
	hash, err := s.svc.SendRawTransaction(ctx, req.SignedHex)
	if err != nil {
		return nil, status.Errorf(codes.InvalidArgument, "%v", err)
	}
	return &chainv1.SendRawTransactionResponse{TxHash: hash}, nil
}

func boolStr(b bool) string {
	if b {
		return "true"
	}
	return "false"
}
