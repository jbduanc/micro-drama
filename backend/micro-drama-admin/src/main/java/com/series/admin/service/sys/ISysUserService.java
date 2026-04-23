package com.series.admin.service.sys;

import com.series.admin.dto.sys.UserInfoDTO;
import com.series.admin.entity.sys.SysUser;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author djbo
 * @since 2026-04-13
 */
public interface ISysUserService extends IService<SysUser> {

    /**
     * 查询系统用户列表
     */
    public List<SysUser> list(UserInfoDTO queryVO);
}
