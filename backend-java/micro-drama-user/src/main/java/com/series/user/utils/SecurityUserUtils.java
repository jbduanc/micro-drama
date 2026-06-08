package com.series.user.utils;

import com.series.common.auth.GatewayAuthContext;
import com.series.user.entity.AppUser;
import com.series.user.service.IAppUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.UUID;

/**
 * 获取当前小程序用户。身份来自网关注入的 {@code X-Auth-Subject}（用户 UUID）。
 */
@Component
public class SecurityUserUtils {

    private static IAppUserService staticAppUserService;

    @Autowired
    private IAppUserService appUserService;

    @PostConstruct
    public void init() {
        staticAppUserService = this.appUserService;
    }

    public static String getCurrentUserId() {
        return GatewayAuthContext.getUserId().orElse(null);
    }

    public static AppUser getCurrentUser() {
        String id = getCurrentUserId();
        if (id == null) {
            return null;
        }
        try {
            return staticAppUserService.getById(UUID.fromString(id));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
