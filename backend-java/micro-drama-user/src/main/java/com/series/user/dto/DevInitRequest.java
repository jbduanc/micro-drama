package com.series.user.dto;

import lombok.Data;

@Data
public class DevInitRequest {
    /** 开发环境模拟的 Telegram ID，默认 dev-local */
    private String telegramId;
    private String nickname;
}
