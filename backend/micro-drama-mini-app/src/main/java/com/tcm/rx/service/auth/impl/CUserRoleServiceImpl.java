package com.tcm.rx.service.auth.impl;

import com.tcm.rx.entity.auth.CUserRole;
import com.tcm.rx.mapper.auth.CUserRoleMapper;
import com.tcm.rx.service.auth.ICUserRoleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 诊疗开方系统--用户角色关联表 服务实现类
 * </p>
 *
 * @author xph
 * @since 2025-07-08
 */
@Service
public class CUserRoleServiceImpl extends ServiceImpl<CUserRoleMapper, CUserRole> implements ICUserRoleService {

}
