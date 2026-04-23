package com.tcm.rx.mapper.auth;

import com.tcm.rx.entity.auth.CRole;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tcm.rx.vo.auth.request.RoleQueryVO;
import com.tcm.rx.vo.auth.response.CRoleVO;

import java.util.List;

/**
 * <p>
 * 诊疗开方系统--角色表 Mapper 接口
 * </p>
 *
 * @author xph
 * @since 2025-07-08
 */
public interface CRoleMapper extends BaseMapper<CRole> {

    /**
     * 查询角色信息的数据
     */
    List<CRoleVO> roleList(RoleQueryVO queryVO);

    /**
     * 根据用户信息查询角色信息的数据
     */
    List<CRoleVO> rolesByUser(RoleQueryVO queryVO);

}
