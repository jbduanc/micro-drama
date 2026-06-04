package com.series.payment.client;

import com.series.common.grpc.chain.v1.ChainServiceGrpc;
import com.series.common.grpc.chain.v1.CreateOrderRequest;
import com.series.common.grpc.chain.v1.OrderResponse;
import com.series.common.grpc.chain.v1.SendRawTransactionRequest;
import com.series.common.grpc.chain.v1.SendRawTransactionResponse;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

@Component
public class ChainGrpcClient {

    @GrpcClient("chain-service")
    private ChainServiceGrpc.ChainServiceBlockingStub chainStub;

    public OrderResponse createPendingOrder(String orderId) {
        return chainStub.createPendingOrder(
                CreateOrderRequest.newBuilder().setOrderId(orderId).build()
        );
    }

    public SendRawTransactionResponse sendRawTransaction(String signedHex) {
        return chainStub.sendRawTransaction(
                SendRawTransactionRequest.newBuilder().setSignedHex(signedHex).build()
        );
    }
}
