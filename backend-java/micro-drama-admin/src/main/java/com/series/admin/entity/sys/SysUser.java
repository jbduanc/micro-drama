package com.series.admin.entity.sys;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 对应 platform_db.sys_user
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("sys_user")
public class SysUser extends Model<SysUser> {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @TableField("google_email")
    private String googleEmail;

    private String nickname;

    private String avatar;

    private Integer status;

    @TableField("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @TableField("updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}
