package com.tcm.rx.service.auth;

import com.tcm.rx.entity.auth.CMenu;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tcm.rx.vo.auth.request.MenuQueryVO;
import com.tcm.rx.vo.auth.response.CMenuVO;
import com.tcm.rx.vo.auth.response.RouterVO;

import java.util.List;

/**
 * <p>
 * 诊疗开方系统--菜单表 服务类
 * </p>
 *
 * @author xph
 * @since 2025-07-08
 */
public interface ICMenuService extends IService<CMenu> {

    /**
     * 查询菜单的数据
     */
    List<CMenuVO> menuList(MenuQueryVO queryVO);

    /**
     * 新增菜单的数据
     */
    Long insertMenu(CMenuVO menuVO);

    /**
     * 更新菜单的数据
     */
    Long updateMenu(CMenuVO menuVO);

    /**
     * 删除菜单的数据
     */
    void deleteMenu(Long id);

    /**
     * 根据角色查询菜单信息的数据
     */
    List<CMenuVO> menuByRole(MenuQueryVO queryVO);

    /**
     * 根据用户查询菜单信息的数据
     */
    List<CMenuVO> menuByUser(MenuQueryVO queryVO);

    /**
     * 根据用户查询菜单树信息
     */
    List<CMenuVO> menuTreeByUser(MenuQueryVO queryVO);

    /**
     * 构建前端路由所需要的菜单
     */
    List<RouterVO> buildMenus(List<CMenuVO> menuList);

}
