package com.series.payment.utils;

import com.series.common.auth.GatewayAuthContext;
import com.series.common.auth.JwtPrincipal;

import java.util.Optional;

/**
 * 支付服务获取当前 C 端用户（网关注入 {@code X-Auth-Subject} 或本地 JWT 校验后写入上下文）。
 */
public final class PaymentSecurityUtils {

    private PaymentSecurityUtils() {
    }

    public static Optional<String> getCurrentUserId() {
        return GatewayAuthContext.getUserId();
    }

    public static Optional<JwtPrincipal> getCurrentPrincipal() {
        return GatewayAuthContext.currentPrincipal();
    }
}
