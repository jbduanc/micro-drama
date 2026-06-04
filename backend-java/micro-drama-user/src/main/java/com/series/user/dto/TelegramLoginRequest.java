package com.series.user.dto;

import lombok.Data;

@Data
public class TelegramLoginRequest {
    /** Telegram WebApp initData 原始字符串 */
    private String initData;
    private String nickname;
    private String avatar;
}
