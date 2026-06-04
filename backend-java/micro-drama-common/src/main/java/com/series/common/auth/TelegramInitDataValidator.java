package com.series.common.auth;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 校验 Telegram Mini App {@code initData}（WebAppData HMAC）。
 *
 * @see <a href="https://core.telegram.org/bots/webapps#validating-data-received-via-the-mini-app">Telegram WebApp validation</a>
 */
public final class TelegramInitDataValidator {

    private TelegramInitDataValidator() {
    }

    public static boolean validate(String initData, String botToken) {
        if (initData == null || initData.isEmpty() || botToken == null || botToken.isEmpty()) {
            return false;
        }
        try {
            Map<String, String> params = parseQuery(initData);
            String hash = params.remove("hash");
            if (hash == null || hash.isEmpty()) {
                return false;
            }
            List<String> lines = new ArrayList<>();
            for (Map.Entry<String, String> e : new TreeMap<>(params).entrySet()) {
                lines.add(e.getKey() + "=" + e.getValue());
            }
            String dataCheckString = String.join("\n", lines);

            byte[] secretKey = hmacSha256("WebAppData".getBytes(StandardCharsets.UTF_8), botToken.getBytes(StandardCharsets.UTF_8));
            byte[] calculated = hmacSha256(secretKey, dataCheckString.getBytes(StandardCharsets.UTF_8));
            String calculatedHex = bytesToHex(calculated);
            return MessageDigest.isEqual(calculatedHex.getBytes(StandardCharsets.UTF_8), hash.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            return false;
        }
    }

    public static Long parseTelegramUserId(String initData) {
        Map<String, String> params = parseQuery(initData);
        String userJson = params.get("user");
        if (userJson == null || userJson.isEmpty()) {
            return null;
        }
        int idIdx = userJson.indexOf("\"id\":");
        if (idIdx < 0) {
            idIdx = userJson.indexOf("\"id\"");
        }
        if (idIdx < 0) {
            return null;
        }
        int start = userJson.indexOf(':', idIdx) + 1;
        int end = userJson.indexOf(',', start);
        if (end < 0) {
            end = userJson.indexOf('}', start);
        }
        if (end < 0) {
            return null;
        }
        String idStr = userJson.substring(start, end).trim();
        try {
            return Long.parseLong(idStr);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Map<String, String> parseQuery(String initData) {
        Map<String, String> map = new TreeMap<>();
        String[] pairs = initData.split("&");
        for (String pair : pairs) {
            int eq = pair.indexOf('=');
            if (eq <= 0) {
                continue;
            }
            String key = URLDecoder.decode(pair.substring(0, eq), "UTF-8");
            String value = URLDecoder.decode(pair.substring(eq + 1), "UTF-8");
            map.put(key, value);
        }
        return map;
    }

    private static byte[] hmacSha256(byte[] key, byte[] data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(key, "HmacSHA256"));
        return mac.doFinal(data);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
