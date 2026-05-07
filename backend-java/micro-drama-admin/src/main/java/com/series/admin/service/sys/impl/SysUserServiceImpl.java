package com.series.admin.service.sys.impl;

import com.series.admin.dto.sys.UserInfoDTO;
import com.series.admin.entity.sys.SysUser;
import com.series.admin.mapper.sys.SysUserMapper;
import com.series.admin.service.sys.ISysUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author djbo
 * @since 2026-04-13
 */
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {

    public List<SysUser> list(UserInfoDTO queryVO) {
        return this.lambdaQuery().orderByDesc(SysUser::getUpdateTime).list();
    }
}
