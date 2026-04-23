package com.tcm.rx.service.auth;

import com.tcm.rx.entity.auth.CRole;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tcm.rx.vo.auth.request.RoleQueryVO;
import com.tcm.rx.vo.auth.response.CRoleVO;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * <p>
 * 诊疗开方系统--角色表 服务类
 * </p>
 *
 * @author xph
 * @since 2025-07-08
 */
public interface ICRoleService extends IService<CRole> {

    /**
     * 查询角色信息的数据
     */
    List<CRoleVO> roleList(RoleQueryVO queryVO);

    /**
     * 根据id查询角色信息的数据
     */
    CRoleVO roleById(RoleQueryVO queryVO);

    /**
     * 新增角色信息的数据
     */
    Long insertRole(CRoleVO roleVO);

    /**
     * 更新角色信息的数据
     */
    Long updateRole(CRoleVO roleVO);

    /**
     * 批量删除角色信息的数据
     */
    Boolean batchDeleteRole(RoleQueryVO queryVO);

    /**
     * 批量更新角色信息的状态
     */
    Boolean batchUpdateStatus(RoleQueryVO queryVO);

    /**
     * 根据用户信息查询角色信息的数据
     */
    List<CRoleVO> rolesByUser(RoleQueryVO queryVO);

    /**
     * 角色管理--导出数据
     */
    void export(HttpServletResponse response, RoleQueryVO queryVO) throws IOException;

}
