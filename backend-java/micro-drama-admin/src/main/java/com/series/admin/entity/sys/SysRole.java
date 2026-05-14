package com.series.admin.entity.sys;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 对应 platform_db.sys_role
 */
@Data
@TableName("sys_role")
public class SysRole {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @TableField("role_name")
    private String roleName;

    @TableField("role_code")
    private String roleCode;

    private String remark;

    @TableField("created_at")
    private LocalDateTime createTime;
}
