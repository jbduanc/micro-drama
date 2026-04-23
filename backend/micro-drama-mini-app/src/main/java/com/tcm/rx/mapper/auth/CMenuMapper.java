package com.tcm.rx.mapper.auth;

import com.tcm.rx.entity.auth.CMenu;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tcm.rx.vo.auth.request.MenuQueryVO;
import com.tcm.rx.vo.auth.response.CMenuVO;

import java.util.List;

/**
 * <p>
 * 诊疗开方系统--菜单表 Mapper 接口
 * </p>
 *
 * @author xph
 * @since 2025-07-08
 */
public interface CMenuMapper extends BaseMapper<CMenu> {

    /**
     * 查询菜单的数据
     */
    List<CMenuVO> menuList(MenuQueryVO queryVO);

    /**
     * 根据角色查询菜单信息的数据
     */
    List<CMenuVO> menuByRole(MenuQueryVO queryVO);

    /**
     * 根据用户查询菜单信息的数据
     */
    List<CMenuVO> menuByUser(MenuQueryVO queryVO);

}
