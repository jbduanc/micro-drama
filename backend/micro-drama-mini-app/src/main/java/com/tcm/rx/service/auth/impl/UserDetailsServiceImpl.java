package com.tcm.rx.service.auth.impl;

import com.tcm.common.config.redis.RedisCache;
import com.tcm.common.config.sys.CurrSysConfig;
import com.tcm.common.entity.BaseUser;
import com.tcm.common.enums.BooleanEnum;
import com.tcm.common.exception.ServiceException;
import com.tcm.common.utils.AuthenticationContextHolder;
import com.tcm.rx.entity.auth.CUser;
import com.tcm.rx.service.auth.ICUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * 用户验证处理
 *
 * @author zzy
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    @Resource
    private ICUserService userService;

    @Resource
    private RedisCache redisCache;

    @Resource
    private CurrSysConfig currSys;

    @Override
    public UserDetails loadUserByUsername(String username) throws ServiceException {
        CUser user = userService.lambdaQuery().eq(CUser::getUserAccount, username).one();
        if (ObjectUtils.isEmpty(user)) {
            log.info("登录用户：{} 不存在.", username);
            throw new ServiceException("登录用户：" + username + " 不存在");
        } else if (BooleanEnum.TRUE.getNum().equals(user.getDelFlag())) {
            log.info("登录用户：{} 已被删除.", username);
            throw new ServiceException("对不起，您的账号：" + username + " 已被删除");
        } else if (BooleanEnum.FALSE.getNum().equals(user.getStatus())) {
            log.info("登录用户：{} 已被停用.", username);
            throw new ServiceException("对不起，您的账号：" + username + " 已停用");
        } else if (2 == user.getStatus()) {
            log.info("登录用户：{} 已离职.", username);
            throw new ServiceException("对不起，您的账号：" + username + " 已离职");
        }
        // 校验密码
        this.validate(user);
        // 创建登录用户对象
        return createLoginUser(user);
    }

    /**
     * 创建登录用户对象
     * @param user 数据库用户信息
     * @return 登录用户对象
     */
    public UserDetails createLoginUser(CUser user) {
        BaseUser baseUser = new BaseUser();
        baseUser.setUsername(user.getUserAccount());
        BeanUtils.copyProperties(user, baseUser);
        baseUser.setPlatformType(currSys.getType());
        baseUser.setPassword(user.getPassword());
        return baseUser;
    }

    /**
     * 校验密码
     *
     * @param user 用户信息
     */
    public void validate(CUser user) {
        Authentication authToken = AuthenticationContextHolder.getContext();
        String username = authToken.getPrincipal().toString();
        String password = authToken.getCredentials().toString();

        String redisKey = currSys.getTokenStart() + "_pwd_err_cnt:" + username;
        Integer retryCount = redisCache.getCacheObject(redisKey);

        if (retryCount == null) {
            retryCount = 0;
        }

        if (retryCount >= Integer.valueOf(5).intValue()) {
            throw new ServiceException("密码错误超过5次，账号锁定10分钟");
        }

        if (!new BCryptPasswordEncoder().matches(password, user.getPassword())) {
            retryCount = retryCount + 1;
            redisCache.setCacheObject(currSys.getTokenStart() + "_pwd_err_cnt:" + username,
                    retryCount, 10, TimeUnit.MINUTES);
            throw new ServiceException("密码错误");
        } else {
            if (redisCache.hasKey(redisKey)) {
                redisCache.deleteObject(redisKey);
            }
        }
    }

}
