package com.series.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.series.user.entity.AppUser;
import com.series.user.mapper.AppUserMapper;
import com.series.user.service.IAppUserService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

@Service
public class AppUserServiceImpl extends ServiceImpl<AppUserMapper, AppUser> implements IAppUserService {

    @Override
    public AppUser findByTelegramId(String telegramId) {
        return getOne(new LambdaQueryWrapper<AppUser>().eq(AppUser::getTelegramId, telegramId));
    }

    @Override
    public AppUser upsertTelegramUser(String telegramId, String nickname, String avatar) {
        AppUser user = findByTelegramId(telegramId);
        Date now = new Date();
        if (user == null) {
            user = new AppUser();
            user.setId(UUID.randomUUID());
            user.setTelegramId(telegramId);
            user.setNickname(emptyIfNull(nickname));
            user.setAvatar(limitLen(emptyIfNull(avatar), 1024));
            user.setBalance(BigDecimal.ZERO);
            user.setStatus(1);
            user.setAuthProvider("telegram");
            user.setCreateTime(now);
            user.setUpdateTime(now);
            save(user);
        } else {
            user.setNickname(emptyIfNull(nickname));
            user.setAvatar(limitLen(emptyIfNull(avatar), 1024));
            user.setUpdateTime(now);
            updateById(user);
        }
        return user;
    }

    @Override
    public AppUser getOrCreateDevUser(String telegramId, String nickname) {
        return upsertTelegramUser(telegramId, nickname != null ? nickname : "Dev User", "");
    }

    private static String emptyIfNull(String s) {
        return s == null ? "" : s;
    }

    private static String limitLen(String s, int max) {
        if (s.length() <= max) {
            return s;
        }
        return s.substring(0, max);
    }
}
