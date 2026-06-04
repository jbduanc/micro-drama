package com.series.payment.service;

import com.series.common.grpc.chain.v1.OrderResponse;
import com.series.common.grpc.chain.v1.SendRawTransactionResponse;
import com.series.payment.client.ChainGrpcClient;
import io.grpc.StatusRuntimeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class PaymentOrchestrationService {

    @Autowired
    private ChainGrpcClient chainGrpcClient;

    public Map<String, Object> createWeb3Order(String orderId) {
        if (orderId == null || orderId.isEmpty()) {
            orderId = UUID.randomUUID().toString();
        }
        try {
            OrderResponse resp = chainGrpcClient.createPendingOrder(orderId);
            Map<String, Object> out = new HashMap<>();
            out.put("id", resp.getId());
            out.put("status", resp.getStatus());
            return out;
        } catch (StatusRuntimeException e) {
            throw new IllegalStateException("chain service: " + e.getStatus().getDescription(), e);
        }
    }

    public Map<String, String> submitWeb3SignedTx(String signedHex) {
        try {
            SendRawTransactionResponse resp = chainGrpcClient.sendRawTransaction(signedHex);
            Map<String, String> out = new HashMap<>();
            out.put("txHash", resp.getTxHash());
            return out;
        } catch (StatusRuntimeException e) {
            throw new IllegalStateException("chain service: " + e.getStatus().getDescription(), e);
        }
    }

    /**
     * Web2 支付占位（Stripe / Telegram Stars 等后续对接）。
     */
    public Map<String, Object> createWeb2OrderPlaceholder(String orderId) {
        Map<String, Object> out = new HashMap<>();
        out.put("id", orderId != null ? orderId : UUID.randomUUID().toString());
        out.put("status", "pending");
        out.put("channel", "web2");
        return out;
    }
}
