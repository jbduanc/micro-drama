// Code generated for micro-drama-chain. DO NOT EDIT manually.

package chainv1

import (
	context "context"

	grpc "google.golang.org/grpc"
	codes "google.golang.org/grpc/codes"
	status "google.golang.org/grpc/status"
	emptypb "google.golang.org/protobuf/types/known/emptypb"
)

const _ = grpc.SupportPackageIsVersion7

const (
	ChainService_Health_FullMethodName              = "/microdrama.chain.v1.ChainService/Health"
	ChainService_CreatePendingOrder_FullMethodName  = "/microdrama.chain.v1.ChainService/CreatePendingOrder"
	ChainService_SendRawTransaction_FullMethodName  = "/microdrama.chain.v1.ChainService/SendRawTransaction"
)

type ChainServiceClient interface {
	Health(ctx context.Context, in *emptypb.Empty, opts ...grpc.CallOption) (*HealthResponse, error)
	CreatePendingOrder(ctx context.Context, in *CreateOrderRequest, opts ...grpc.CallOption) (*OrderResponse, error)
	SendRawTransaction(ctx context.Context, in *SendRawTransactionRequest, opts ...grpc.CallOption) (*SendRawTransactionResponse, error)
}

type chainServiceClient struct {
	cc grpc.ClientConnInterface
}

func NewChainServiceClient(cc grpc.ClientConnInterface) ChainServiceClient {
	return &chainServiceClient{cc}
}

func (c *chainServiceClient) Health(ctx context.Context, in *emptypb.Empty, opts ...grpc.CallOption) (*HealthResponse, error) {
	out := new(HealthResponse)
	err := c.cc.Invoke(ctx, ChainService_Health_FullMethodName, in, out, opts...)
	return out, err
}

func (c *chainServiceClient) CreatePendingOrder(ctx context.Context, in *CreateOrderRequest, opts ...grpc.CallOption) (*OrderResponse, error) {
	out := new(OrderResponse)
	err := c.cc.Invoke(ctx, ChainService_CreatePendingOrder_FullMethodName, in, out, opts...)
	return out, err
}

func (c *chainServiceClient) SendRawTransaction(ctx context.Context, in *SendRawTransactionRequest, opts ...grpc.CallOption) (*SendRawTransactionResponse, error) {
	out := new(SendRawTransactionResponse)
	err := c.cc.Invoke(ctx, ChainService_SendRawTransaction_FullMethodName, in, out, opts...)
	return out, err
}

type ChainServiceServer interface {
	Health(context.Context, *emptypb.Empty) (*HealthResponse, error)
	CreatePendingOrder(context.Context, *CreateOrderRequest) (*OrderResponse, error)
	SendRawTransaction(context.Context, *SendRawTransactionRequest) (*SendRawTransactionResponse, error)
	mustEmbedUnimplementedChainServiceServer()
}

type UnimplementedChainServiceServer struct{}

func (UnimplementedChainServiceServer) Health(context.Context, *emptypb.Empty) (*HealthResponse, error) {
	return nil, status.Errorf(codes.Unimplemented, "method Health not implemented")
}
func (UnimplementedChainServiceServer) CreatePendingOrder(context.Context, *CreateOrderRequest) (*OrderResponse, error) {
	return nil, status.Errorf(codes.Unimplemented, "method CreatePendingOrder not implemented")
}
func (UnimplementedChainServiceServer) SendRawTransaction(context.Context, *SendRawTransactionRequest) (*SendRawTransactionResponse, error) {
	return nil, status.Errorf(codes.Unimplemented, "method SendRawTransaction not implemented")
}
func (UnimplementedChainServiceServer) mustEmbedUnimplementedChainServiceServer() {}

func RegisterChainServiceServer(s grpc.ServiceRegistrar, srv ChainServiceServer) {
	s.RegisterService(&ChainService_ServiceDesc, srv)
}

func _ChainService_Health_Handler(srv interface{}, ctx context.Context, dec func(interface{}) error, interceptor grpc.UnaryServerInterceptor) (interface{}, error) {
	in := new(emptypb.Empty)
	if err := dec(in); err != nil {
		return nil, err
	}
	if interceptor == nil {
		return srv.(ChainServiceServer).Health(ctx, in)
	}
	info := &grpc.UnaryServerInfo{Server: srv, FullMethod: ChainService_Health_FullMethodName}
	handler := func(ctx context.Context, req interface{}) (interface{}, error) {
		return srv.(ChainServiceServer).Health(ctx, req.(*emptypb.Empty))
	}
	return interceptor(ctx, in, info, handler)
}

func _ChainService_CreatePendingOrder_Handler(srv interface{}, ctx context.Context, dec func(interface{}) error, interceptor grpc.UnaryServerInterceptor) (interface{}, error) {
	in := new(CreateOrderRequest)
	if err := dec(in); err != nil {
		return nil, err
	}
	if interceptor == nil {
		return srv.(ChainServiceServer).CreatePendingOrder(ctx, in)
	}
	info := &grpc.UnaryServerInfo{Server: srv, FullMethod: ChainService_CreatePendingOrder_FullMethodName}
	handler := func(ctx context.Context, req interface{}) (interface{}, error) {
		return srv.(ChainServiceServer).CreatePendingOrder(ctx, req.(*CreateOrderRequest))
	}
	return interceptor(ctx, in, info, handler)
}

func _ChainService_SendRawTransaction_Handler(srv interface{}, ctx context.Context, dec func(interface{}) error, interceptor grpc.UnaryServerInterceptor) (interface{}, error) {
	in := new(SendRawTransactionRequest)
	if err := dec(in); err != nil {
		return nil, err
	}
	if interceptor == nil {
		return srv.(ChainServiceServer).SendRawTransaction(ctx, in)
	}
	info := &grpc.UnaryServerInfo{Server: srv, FullMethod: ChainService_SendRawTransaction_FullMethodName}
	handler := func(ctx context.Context, req interface{}) (interface{}, error) {
		return srv.(ChainServiceServer).SendRawTransaction(ctx, req.(*SendRawTransactionRequest))
	}
	return interceptor(ctx, in, info, handler)
}

var ChainService_ServiceDesc = grpc.ServiceDesc{
	ServiceName: "microdrama.chain.v1.ChainService",
	HandlerType: (*ChainServiceServer)(nil),
	Methods: []grpc.MethodDesc{
		{MethodName: "Health", Handler: _ChainService_Health_Handler},
		{MethodName: "CreatePendingOrder", Handler: _ChainService_CreatePendingOrder_Handler},
		{MethodName: "SendRawTransaction", Handler: _ChainService_SendRawTransaction_Handler},
	},
	Streams:  []grpc.StreamDesc{},
	Metadata: "chain/v1/chain_service.proto",
}
