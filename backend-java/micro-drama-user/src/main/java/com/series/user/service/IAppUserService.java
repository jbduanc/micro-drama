package com.series.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.series.user.entity.AppUser;

public interface IAppUserService extends IService<AppUser> {

    AppUser findByTelegramId(String telegramId);

    AppUser upsertTelegramUser(String telegramId, String nickname, String avatar);

    AppUser getOrCreateDevUser(String telegramId, String nickname);
}
