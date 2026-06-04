package com.series.user.dto;

import lombok.Data;

/**
 * Web3 登录预留：后续对接签名挑战 + 链上地址绑定。
 */
@Data
public class Web3LoginPlaceholderDTO {
    private String status = "not_implemented";
    private String message = "Web3 login will verify wallet signature and link wallet_address on app_user";
}
