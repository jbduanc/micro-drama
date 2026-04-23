package com.tcm.rx.service.auth.impl;

import com.tcm.common.captcha.CaptchaService;
import com.tcm.common.config.redis.RedisCache;
import com.tcm.common.config.sys.CurrSysConfig;
import com.tcm.common.constants.CommonConstants;
import com.tcm.common.entity.BaseUser;
import com.tcm.common.exception.ServiceException;
import com.tcm.common.utils.*;
import com.tcm.rx.vo.auth.response.CaptchaCodeVO;
import eu.bitwalker.useragentutils.UserAgent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 登录校验方法
 *
 * @author zzy
 */
@Slf4j
@Component
public class SysLoginService {

    @Resource
    private AuthenticationManager authenticationManager;

    @Resource
    private RedisCache redisCache;

    @Resource
    private CurrSysConfig currSys;

    @Resource
    private CaptchaService captchaService;

    private  static final String captchaKeyPrefix = "captcha_key_";

    /**
     * 登录验证
     *
     * @param username 用户名
     * @param password 密码
     * @param code     验证码
     * @param uuid     唯一标识
     * @return 结果
     */
    public String login(String username, String password, String code, String uuid) {
        // 校验图形验证码
        Boolean checkCaptcha = captchaService.checkCaptchaCodeByUuid(captchaKeyPrefix + uuid, code);
        if (!checkCaptcha) {
            throw new ServiceException("图形验证码有误");
        } else {
            redisCache.deleteObject(captchaKeyPrefix + uuid);
        }

        // 用户验证
        Authentication authentication = null;
        try {
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, password);
            AuthenticationContextHolder.setContext(authenticationToken);
            authentication = authenticationManager.authenticate(authenticationToken);
        } catch (Exception e) {
            e.printStackTrace();
            if (e.getCause().getClass() == ServiceException.class) {
                throw new ServiceException(((ServiceException)e.getCause()).getMsg());
            } else {
                throw new ServiceException("账号校验异常");
            }
        } finally {
            AuthenticationContextHolder.clearContext();
        }
        BaseUser loginUser = (BaseUser) authentication.getPrincipal();
        // 生成token
        return createToken(loginUser);
    }

    /**
     * 创建token
     *
     * @param loginUser
     * @return
     */
    public String createToken(BaseUser loginUser) {
        String uuid = IdUtils.fastUUID();
        loginUser.setToken(uuid);
        //设置登录用户信息
        UserAgent userAgent = UserAgent.parseUserAgentString(ServletUtils.getRequest().getHeader("User-Agent"));
        String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
        loginUser.setIpaddr(ip);
        loginUser.setLoginLocation(AddressUtils.getRealAddressByIP(ip));
        loginUser.setBrowser(userAgent.getBrowser().getName());
        loginUser.setOs(userAgent.getOperatingSystem().getName());
        //刷新令牌有效期
        loginUser.setLoginTime(System.currentTimeMillis());
        loginUser.setExpireTime(loginUser.getLoginTime() + currSys.currSys().getExpireTime() * CommonConstants.SysAuthConstants.MILLIS_MINUTE);
        // 根据uuid将loginUser缓存
        String userKey = SecurityUtils.getTokenKeyByUuid(uuid, currSys.currSys());
        redisCache.setCacheObject(userKey, loginUser, currSys.currSys().getExpireTime(), TimeUnit.MINUTES);
        //生成token
        return currSys.getTokenStart() + SecurityUtils.buildTokenByUuid(uuid, currSys.currSys());
    }

    /**
     * 生成图形验证码
     */
    public CaptchaCodeVO getCaptchaCode() {
        // 生成uuid（带前缀的）
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        // 生成png图形验证码
        String captcha = captchaService
                .generateCaptchaCodeByUuid(captchaKeyPrefix + uuid, 1, 130, 48, 4);

        CaptchaCodeVO result = new CaptchaCodeVO();
        result.setUuid(uuid);
        result.setCaptchaData(captcha);
        return result;
    }

    /**
     * 校验图形验证码
     */
    public Boolean checkCaptchaCode(CaptchaCodeVO captchaCodeVO) {
        return captchaService.checkCaptchaCodeByUuid(captchaKeyPrefix + captchaCodeVO.getUuid(),
                captchaCodeVO.getCode());
    }

}
