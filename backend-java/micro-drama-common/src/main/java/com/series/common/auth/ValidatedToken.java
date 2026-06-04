package com.series.common.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ValidatedToken {
    private final String subject;
    private final AuthAudience audience;
}
