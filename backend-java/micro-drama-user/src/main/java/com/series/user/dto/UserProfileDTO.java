package com.series.user.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class UserProfileDTO {
    private String id;
    private String nickname;
    private String avatarUrl;
    private BigDecimal balance;
    private String telegramId;
    private String walletAddress;
    /** telegram | dev | web3（预留） */
    private String authProvider;
}
