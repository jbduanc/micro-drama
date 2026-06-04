package com.series.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.series.common.typehandler.UuidTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

/**
 * user_db.app_user
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("app_user")
public class AppUser extends Model<AppUser> {

    @TableId(value = "id", type = IdType.INPUT)
    @TableField(value = "id", typeHandler = UuidTypeHandler.class)
    private UUID id;

    @TableField("telegram_id")
    private String telegramId;

    private String nickname;

    private String avatar;

    private BigDecimal balance;

    @TableField("wallet_address")
    private String walletAddress;

    private Integer status;

    @TableField("auth_provider")
    private String authProvider;

    @TableField("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @TableField("updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}
