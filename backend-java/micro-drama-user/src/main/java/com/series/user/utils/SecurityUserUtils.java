package com.series.user.utils;

import com.series.user.entity.AppUser;
import com.series.user.service.IAppUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.UUID;

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
        try {
            return (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        } catch (Exception e) {
            return null;
        }
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
