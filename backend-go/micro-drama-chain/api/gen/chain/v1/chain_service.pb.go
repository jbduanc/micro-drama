// Package chainv1 — protobuf messages (hand-maintained; run protoc when available).
package chainv1

type HealthResponse struct {
	Ok           bool   `protobuf:"varint,1,opt,name=ok,proto3" json:"ok,omitempty"`
	EthConnected string `protobuf:"bytes,2,opt,name=eth_connected,json=ethConnected,proto3" json:"eth_connected,omitempty"`
}

type CreateOrderRequest struct {
	OrderId string `protobuf:"bytes,1,opt,name=order_id,json=orderId,proto3" json:"order_id,omitempty"`
}

type OrderResponse struct {
	Id     string `protobuf:"bytes,1,opt,name=id,proto3" json:"id,omitempty"`
	Status string `protobuf:"bytes,2,opt,name=status,proto3" json:"status,omitempty"`
}

type SendRawTransactionRequest struct {
	SignedHex string `protobuf:"bytes,1,opt,name=signed_hex,json=signedHex,proto3" json:"signed_hex,omitempty"`
}

type SendRawTransactionResponse struct {
	TxHash string `protobuf:"bytes,1,opt,name=tx_hash,json=txHash,proto3" json:"tx_hash,omitempty"`
}
