package com.series.content.entity.business;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.series.common.typehandler.UuidTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

/**
 * 对应 content_db.drama
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("drama")
public class MicroDramas extends Model<MicroDramas> implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_UUID)
    @TableField(value = "id", typeHandler = UuidTypeHandler.class)
    private UUID id;

    private String title;

    @TableField("cover_url")
    private String coverUrl;

    private String description;

    @TableField("total_episodes")
    private Integer totalEpisodes;

    private BigDecimal price;

    private Integer status;

    private Integer sort;

    @TableField("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @TableField("updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}
