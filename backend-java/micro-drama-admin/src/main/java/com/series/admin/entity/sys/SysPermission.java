package com.series.admin.entity.sys;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 对应 platform_db.sys_permission
 */
@Data
@TableName("sys_permission")
public class SysPermission {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @TableField("perm_name")
    private String permName;

    @TableField("perm_code")
    private String permCode;

    @TableField("perm_type")
    private String permType;

    @TableField("created_at")
    private LocalDateTime createTime;
}
