package com.tcm.rx.service.msg.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.tcm.common.config.redis.RedisCache;
import com.tcm.common.core.UserContextHolder;
import com.tcm.common.entity.BaseUser;
import com.tcm.common.enums.BooleanEnum;
import com.tcm.common.exception.ServiceException;
import com.tcm.rx.entity.msg.Msg;
import com.tcm.rx.mapper.msg.MsgMapper;
import com.tcm.rx.service.msg.IMsgService;
import com.tcm.rx.vo.msg.request.MsgQueryVO;
import com.tcm.rx.vo.msg.request.MsgUpdateVO;
import com.tcm.rx.vo.msg.response.MsgVO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class MsgServiceImpl extends ServiceImpl<MsgMapper, Msg> implements IMsgService {

    @Resource
    private RedisCache redisCache;

    // Redis中消息哈希表的前缀
    private static final String MSG_HASH_PREFIX = "user:msg:";
    // 未读消息数量的前缀
    private static final String UNREAD_COUNT_PREFIX = "user:unread:count:";
    // 缓存过期时间（24小时，无消息用户可适当延长至7天）
    private static final long CACHE_EXPIRE_HOURS = 24;
    private static final long NO_MSG_EXPIRE_HOURS = 168; // 无消息用户缓存7天

    /**
     * 分页查询（从数据库查，用于列表页加载更多）
     */
    @Override
    public List<MsgVO> pageList(MsgQueryVO msgQueryVO) {
        BaseUser currentUser = UserContextHolder.getUserInfoContext();
        msgQueryVO.setMedicalGroupId(currentUser.getMedicalGroupId());
        msgQueryVO.setUserId(currentUser.getId());
        return this.getBaseMapper().selectByQuery(msgQueryVO);
    }

    /**
     * 修改消息（优化：添加缓存清理逻辑）
     *
     * @param msgUpdateVO 消息更新参数
     * @return 是否修改成功
     */
    public Boolean updateMsg(MsgUpdateVO msgUpdateVO) {
        // 参数校验
        if (ObjectUtil.isEmpty(msgUpdateVO.getIdList()) || !Lists.newArrayList(1, 2).contains(msgUpdateVO.getOptionType())) {
            throw new ServiceException("参数有误");
        }

        BaseUser currentUser = UserContextHolder.getUserInfoContext();
        Long userId = currentUser.getId(); // 获取当前用户ID，用于清理缓存

        // 执行消息更新操作
        if (msgUpdateVO.getOptionType() == 1) {
            // 标记已读：更新状态为1（已读）
            this.lambdaUpdate()
                    .in(Msg::getId, msgUpdateVO.getIdList())
                    .set(Msg::getStatus, BooleanEnum.TRUE.getNum())
                    .set(Msg::getUpdateBy, userId)
                    .set(Msg::getUpdateTime, new DateTime())
                    .update(); // 补充update()执行更新
        } else {
            // 标记删除：更新删除标识为1（已删除）
            this.lambdaUpdate()
                    .in(Msg::getId, msgUpdateVO.getIdList())
                    .set(Msg::getDelFlag, BooleanEnum.TRUE.getNum())
                    .set(Msg::getUpdateBy, userId)
                    .set(Msg::getUpdateTime, new DateTime())
                    .update(); // 补充update()执行更新
        }

        // 清理缓存：消息状态变更后，删除用户相关缓存，确保下次查询获取最新数据
        redisCache.deleteObject(UNREAD_COUNT_PREFIX + userId); // 清理未读数量缓存
        redisCache.deleteObject(MSG_HASH_PREFIX + userId);     // 清理消息列表缓存

        return true;
    }

    /**
     * 保存消息时，清除用户的空缓存（确保新消息能被查询到）
     */
    @Override
    public boolean save(Msg msg) {
        boolean result = super.save(msg);
        if (result) {
            Long userId = msg.getUserId();
            // 清除用户的未读计数缓存和消息列表缓存（触发下次查询时重新加载）
            redisCache.deleteObject(UNREAD_COUNT_PREFIX + userId);
            redisCache.deleteObject(MSG_HASH_PREFIX + userId);
        }
        return result;
    }

    /**
     * 获取用户未读消息数量（优化：缓存空结果）
     */
    @Override
    public Integer getUnreadCount(Long userId) {
        String unreadCountKey = UNREAD_COUNT_PREFIX + userId;
        Integer cacheCount = redisCache.getCacheMapValue(unreadCountKey, "count");

        // 1. Redis有缓存（包括0），直接返回
        if (cacheCount != null) {
            return cacheCount;
        }

        // 2. Redis无缓存，查询数据库
        LambdaQueryWrapper<Msg> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Msg::getUserId, userId)
                .eq(Msg::getStatus, BooleanEnum.FALSE.getNum()) // 未读
                .eq(Msg::getDelFlag, BooleanEnum.FALSE.getNum()); // 未删除
        int dbCount = this.count(queryWrapper);

        // 3. 缓存结果（无消息用户缓存时间更长）
        redisCache.setCacheMapValue(unreadCountKey, "count", dbCount);
        long expireHours = dbCount == 0 ? NO_MSG_EXPIRE_HOURS : CACHE_EXPIRE_HOURS;
        redisCache.expire(unreadCountKey, expireHours, TimeUnit.HOURS);

        return dbCount;
    }

    /**
     * 获取用户最新消息列表（优化：缓存空列表）
     */
    @Override
    public List<Msg> getLatestMessages(Long userId, int limit) {
        String msgHashKey = MSG_HASH_PREFIX + userId;
        Map<String, Object> cacheMap = redisCache.getCacheMap(msgHashKey);

        // 1. Redis有缓存（包括空列表），直接返回
        if (ObjectUtil.isNotEmpty(cacheMap)) {
            // 空列表缓存标记（避免Hash结构为空时被误判）
            if (cacheMap.containsKey("__EMPTY__")) {
                return Collections.emptyList();
            }
            return cacheMap.values().stream()
                    .map(msgJson -> JSONUtil.toBean(msgJson.toString(), Msg.class))
                    .sorted((m1, m2) -> m2.getCreateTime().compareTo(m1.getCreateTime()))
                    .limit(limit)
                    .collect(Collectors.toList());
        }

        // 2. Redis无缓存，查询数据库
        LambdaQueryWrapper<Msg> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Msg::getUserId, userId)
                .eq(Msg::getDelFlag, BooleanEnum.FALSE.getNum())
                .orderByDesc(Msg::getCreateTime)
                .last("limit " + limit);
        List<Msg> dbMsgList = this.list(queryWrapper);

        // 3. 缓存结果（无消息时存储空标记）
        if (dbMsgList.isEmpty()) {
            // 用特殊键标记空列表，避免Hash结构为空时被误判为未缓存
            redisCache.setCacheMapValue(msgHashKey, "__EMPTY__", "1");
        } else {
            dbMsgList.forEach(msg -> {
                redisCache.setCacheMapValue(msgHashKey, msg.getId().toString(), JSONUtil.toJsonStr(msg));
            });
        }
        long expireHours = dbMsgList.isEmpty() ? NO_MSG_EXPIRE_HOURS : CACHE_EXPIRE_HOURS;
        redisCache.expire(msgHashKey, expireHours, TimeUnit.HOURS);

        return dbMsgList;
    }

}