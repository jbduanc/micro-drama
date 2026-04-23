package com.tcm.rx.util;

import com.tcm.common.config.redis.RedisCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 处方ID生成器（基于Redis实现）
 */
@Component
public class RxIdGenerator {

    private static final Logger logger = LoggerFactory.getLogger(RxIdGenerator.class);
    private static final String RX_ID_COUNTER_KEY = "sequence:rx:id";
    private static final String BASE_ID = "1000000000";

    @Resource
    private RedisCache redisCache;

    /**
     * 生成唯一处方ID（10位数，从1000000001开始）
     * @return 生成的处方ID字符串
     */
    public String generateRxId() {
        return formatRxId(redisCache.hIncrBy(RX_ID_COUNTER_KEY, "counter", 1));
    }

    /**
     * 格式化处方ID
     * @param incrementId 自增ID值
     * @return 10位处方ID字符串
     */
    private String formatRxId(long incrementId) {
        String idStr = String.valueOf(incrementId);
        int zeroCount = BASE_ID.length() - idStr.length();
        if (zeroCount < 0) {
            throw new IllegalStateException("处方ID计数器已超过最大值");
        }
        // 3. 组合最终ID
        return BASE_ID.substring(0, zeroCount) + idStr;
    }
}