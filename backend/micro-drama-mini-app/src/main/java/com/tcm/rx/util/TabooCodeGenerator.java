package com.tcm.rx.util;

import com.tcm.common.config.redis.RedisCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 禁忌编号生成器（基于Redis实现）
 */
@Component
public class TabooCodeGenerator {

    private static final Logger logger = LoggerFactory.getLogger(TabooCodeGenerator.class);
    // Redis中存储计数器的键
    private static final String TABOO_CODE_COUNTER_KEY = "sequence:taboo:code";
    // 禁忌编号前缀
    private static final String TABOO_CODE_PREFIX = "JJ";
    // 数字部分的长度（6位）
    private static final int NUMBER_LENGTH = 6;
    // 最大允许的数字值（999999）
    private static final long MAX_NUMBER = 999999;

    @Resource
    private RedisCache redisCache;

    /**
     * 生成唯一禁忌编号（格式：JJ+6位数字，从JJ000001开始）
     * @return 生成的禁忌编号字符串
     */
    public String generateTabooCode() {
        // 调用Redis自增方法，从1开始递增
        long incrementId = redisCache.hIncrBy(TABOO_CODE_COUNTER_KEY, "counter", 1);
        return formatTabooCode(incrementId);
    }

    /**
     * 格式化禁忌编号
     * @param incrementId 自增ID值
     * @return 格式化后的禁忌编号（JJ+6位数字）
     */
    private String formatTabooCode(long incrementId) {
        // 检查是否超过最大允许值
        if (incrementId > MAX_NUMBER) {
            throw new IllegalStateException("禁忌编号计数器已超过最大值（" + TABOO_CODE_PREFIX + MAX_NUMBER + "）");
        }
        // 格式化数字部分为6位，不足补0
        String numberStr = String.format("%0" + NUMBER_LENGTH + "d", incrementId);
        // 拼接前缀和数字部分
        return TABOO_CODE_PREFIX + numberStr;
    }
}
