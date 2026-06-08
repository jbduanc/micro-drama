package com.series.gatewayauth.service;

import com.series.common.auth.AuthAudience;
import com.series.common.auth.AuthTokenPair;
import com.series.common.auth.ValidatedToken;
import org.springframework.http.HttpStatus;

public class ForwardAuthResult {

    private final boolean ok;
    private final HttpStatus status;
    private final ValidatedToken token;
    private final String accessToken;
    private final AuthTokenPair refreshedPair;
    private final boolean refreshed;

    private ForwardAuthResult(boolean ok,
                              HttpStatus status,
                              ValidatedToken token,
                              String accessToken,
                              AuthTokenPair refreshedPair,
                              boolean refreshed) {
        this.ok = ok;
        this.status = status;
        this.token = token;
        this.accessToken = accessToken;
        this.refreshedPair = refreshedPair;
        this.refreshed = refreshed;
    }

    public static ForwardAuthResult ok(ValidatedToken token,
                                       String accessToken,
                                       AuthTokenPair refreshedPair,
                                       boolean refreshed) {
        return new ForwardAuthResult(true, HttpStatus.OK, token, accessToken, refreshedPair, refreshed);
    }

    public static ForwardAuthResult forbidden() {
        return new ForwardAuthResult(false, HttpStatus.FORBIDDEN, null, null, null, false);
    }

    public static ForwardAuthResult unauthorized() {
        return new ForwardAuthResult(false, HttpStatus.UNAUTHORIZED, null, null, null, false);
    }

    public boolean isOk() {
        return ok;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getSubject() {
        return token != null ? token.getSubject() : null;
    }

    public AuthAudience getAudience() {
        return token != null ? token.getAudience() : null;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public AuthTokenPair getRefreshedPair() {
        return refreshedPair;
    }

    public boolean isRefreshed() {
        return refreshed;
    }
}
