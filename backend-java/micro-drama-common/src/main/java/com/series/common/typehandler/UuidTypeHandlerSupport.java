package com.series.common.typehandler;

import java.util.UUID;

/**
 * UUID 字符串解析（含 MyBatis-Plus 32 位无连字符格式）。
 */
public final class UuidTypeHandlerSupport {

    private UuidTypeHandlerSupport() {
    }

    public static UUID toUuid(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        if (trimmed.length() == 32 && !trimmed.contains("-")) {
            return UUID.fromString(
                    trimmed.substring(0, 8) + "-"
                            + trimmed.substring(8, 12) + "-"
                            + trimmed.substring(12, 16) + "-"
                            + trimmed.substring(16, 20) + "-"
                            + trimmed.substring(20, 32));
        }
        return UUID.fromString(trimmed);
    }

    public static String toCanonicalString(Object val) {
        if (val == null) {
            return null;
        }
        if (val instanceof UUID) {
            return val.toString();
        }
        return toUuid(val.toString()).toString();
    }
}
