package com.series.common.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

@Component
public class AuthCookieSupport {

    @Value("${auth.cookie.domain:}")
    private String cookieDomain;

    @Value("${auth.cookie.secure:true}")
    private boolean cookieSecure;

    public void writeTokenCookies(HttpServletResponse response, AuthTokenPair pair) {
        for (String header : buildSetCookieHeaders(pair)) {
            response.addHeader("Set-Cookie", header);
        }
    }

    public List<String> buildSetCookieHeaders(AuthTokenPair pair) {
        List<String> headers = new ArrayList<>();
        if (pair == null) {
            return headers;
        }
        if (pair.getAccessToken() != null && !pair.getAccessToken().isEmpty()) {
            headers.add(buildCookieHeader(AuthCookieNames.ACCESS, pair.getAccessToken(), 7200));
        }
        if (pair.getRefreshToken() != null && !pair.getRefreshToken().isEmpty()) {
            headers.add(buildCookieHeader(AuthCookieNames.REFRESH, pair.getRefreshToken(), 2592000));
        }
        return headers;
    }

    public void clearTokenCookies(HttpServletResponse response) {
        for (String header : buildClearCookieHeaders()) {
            response.addHeader("Set-Cookie", header);
        }
    }

    public List<String> buildClearCookieHeaders() {
        List<String> headers = new ArrayList<>();
        headers.add(buildCookieHeader(AuthCookieNames.ACCESS, "", 0));
        headers.add(buildCookieHeader(AuthCookieNames.REFRESH, "", 0));
        return headers;
    }

    public String readCookie(javax.servlet.http.HttpServletRequest request, String name) {
        javax.servlet.http.Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (javax.servlet.http.Cookie cookie : cookies) {
            if (name.equals(cookie.getName())) {
                String value = cookie.getValue();
                if (value != null && !value.trim().isEmpty()) {
                    return value.trim();
                }
            }
        }
        return null;
    }

    private String buildCookieHeader(String name, String value, int maxAgeSeconds) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(maxAgeSeconds);
        if (cookieDomain != null && !cookieDomain.trim().isEmpty()) {
            builder.domain(cookieDomain.trim());
        }
        return builder.build().toString();
    }
}
