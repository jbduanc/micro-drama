package com.tcm.rx.entity.auth;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

/**
 * <p>
 * 诊疗开方系统--用户角色关联表
 * </p>
 *
 * @author xph
 * @since 2025-07-08
 */
@Data
@TableName("rx_c_user_role")
public class CUserRole extends Model<CUserRole> {

    private static final long serialVersionUID = -8026122307166427675L;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 角色id
     */
    private Long roleId;

}
