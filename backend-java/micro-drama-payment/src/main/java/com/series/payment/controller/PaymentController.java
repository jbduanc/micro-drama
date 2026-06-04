package com.series.payment.controller;

import com.series.common.entity.Result;
import com.series.payment.service.PaymentOrchestrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class PaymentController {

    @Autowired
    private PaymentOrchestrationService paymentService;

    @GetMapping("/healthz")
    public Map<String, Object> healthz() {
        return java.util.Collections.singletonMap("ok", true);
    }

    @PostMapping("/v1/orders")
    public Result<Map<String, Object>> createOrder(@RequestBody Map<String, String> body) {
        String id = body != null ? body.get("id") : null;
        if (id == null || id.isEmpty()) {
            return Result.error("id required");
        }
        return Result.ok(paymentService.createWeb3Order(id));
    }

    @PostMapping("/v1/tx/raw")
    public Result<Map<String, String>> sendRaw(@RequestBody Map<String, String> body) {
        String signedHex = body != null ? body.get("signedHex") : null;
        if (signedHex == null || signedHex.isEmpty()) {
            return Result.error("signedHex required");
        }
        return Result.ok(paymentService.submitWeb3SignedTx(signedHex));
    }

    /** Web2 下单占位 */
    @PostMapping("/v1/web2/orders")
    public Result<Map<String, Object>> createWeb2Order(@RequestBody(required = false) Map<String, String> body) {
        String id = body != null ? body.get("id") : null;
        return Result.ok(paymentService.createWeb2OrderPlaceholder(id));
    }
}
