package com.series.admin.entity.sys;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统权限表(接口/菜单)实体
 */
@Data
@TableName("sys_permission")
public class SysPermission {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 权限名称(非空)
     */
    private String permName;

    /**
     * 权限编码(唯一非空)
     */
    private String permCode;

    /**
     * 权限类型 API/MENU
     */
    private String permType;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
