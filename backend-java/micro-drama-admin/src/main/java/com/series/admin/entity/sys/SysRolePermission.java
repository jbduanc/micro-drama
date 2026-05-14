package com.series.admin.entity.sys;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 对应 platform_db.sys_role_permission
 */
@Data
@TableName("sys_role_permission")
public class SysRolePermission {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @TableField("role_id")
    private String roleId;

    @TableField("permission_id")
    private String permissionId;
}
