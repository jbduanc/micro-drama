package com.series.admin.utils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.series.admin.entity.sys.SysUser;
import com.series.admin.service.sys.ISysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;

/**
 * 全局获取当前登录管理员信息工具类
 * 适配：Spring Security + JWT + PostgreSQL
 */
@Component
public class SecurityUserUtils {

    /**
     * 静态注入用户Service（用于查询数据库完整用户信息）
     */
    private static ISysUserService staticSysUserService;

    @Autowired
    private ISysUserService sysUserService;

    /**
     * 初始化静态Service
     */
    @PostConstruct
    public void init() {
        staticSysUserService = this.sysUserService;
    }

    // ====================== 核心方法 ======================

    /**
     * 获取当前登录用户的【Google邮箱】
     * @return 邮箱 | null(未登录)
     */
    public static String getCurrentUserEmail() {
        try {
            // 从Security上下文获取登录用户邮箱
            return (String) SecurityContextHolder.getContext()
                    .getAuthentication()
                    .getPrincipal();
        } catch (Exception e) {
            // 未登录/上下文异常返回null
            return null;
        }
    }

    /**
     * 获取当前登录的【完整管理员信息】(从PostgreSQL查询)
     * @return SysUser | null
     */
    public static SysUser getCurrentUser() {
        String email = getCurrentUserEmail();
        if (email == null) {
            return null;
        }

        // 根据邮箱查询数据库完整用户信息
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getGoogleEmail, email);
        return staticSysUserService.getOne(wrapper);
    }

    /**
     * 获取当前登录用户ID（platform_db.sys_user.id，UUID）
     */
    public static String getCurrentUserId() {
        SysUser user = getCurrentUser();
        return user == null ? null : user.getId();
    }

    /**
     * 判断是否已登录
     */
    public static boolean isLogin() {
        return getCurrentUserEmail() != null;
    }
}
