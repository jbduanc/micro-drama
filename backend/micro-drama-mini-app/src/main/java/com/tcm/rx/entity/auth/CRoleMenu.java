package com.tcm.rx.entity.auth;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

/**
 * <p>
 * 诊疗开方系统--角色菜单关联表
 * </p>
 *
 * @author xph
 * @since 2025-07-08
 */
@Data
@TableName("rx_c_role_menu")
public class CRoleMenu extends Model<CRoleMenu> {

    private static final long serialVersionUID = -2917407817123030088L;

    /**
     * 角色id
     */
    private Long roleId;

    /**
     * 菜单id
     */
    private Long menuId;

}
