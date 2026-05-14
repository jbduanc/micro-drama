package com.series.admin.entity.sys;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 对应 platform_db.sys_user_role
 */
@Data
@TableName("sys_user_role")
public class SysUserRole {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @TableField("user_id")
    private String userId;

    @TableField("role_id")
    private String roleId;
}
