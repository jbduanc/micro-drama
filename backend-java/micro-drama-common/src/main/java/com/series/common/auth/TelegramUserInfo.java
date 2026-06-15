package com.series.common.auth;

import lombok.Data;

@Data
public class TelegramUserInfo {
    private Long id;
    private String firstName;
    private String lastName;
    private String username;
    private String photoUrl;

    public String displayName() {
        if (username != null && !username.isEmpty()) {
            return username;
        }
        if (firstName != null && lastName != null && !lastName.isEmpty()) {
            return firstName + " " + lastName;
        }
        return firstName != null ? firstName : "";
    }
}
