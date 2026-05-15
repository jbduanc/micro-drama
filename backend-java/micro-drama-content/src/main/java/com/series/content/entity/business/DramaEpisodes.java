package com.series.content.entity.business;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.series.common.typehandler.UuidTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

/**
 * 对应 content_db.episode
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName(value = "episode", autoResultMap = true)
public class DramaEpisodes extends Model<DramaEpisodes> implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.INPUT)
    @TableField(value = "id", typeHandler = UuidTypeHandler.class)
    private UUID id;

    @TableField(value = "drama_id", typeHandler = UuidTypeHandler.class)
    private UUID dramaId;

    @TableField("episode_num")
    private Integer episodeNum;

    private String title;

    @TableField(value = "video_asset_id", typeHandler = UuidTypeHandler.class)
    private UUID videoAssetId;

    private BigDecimal price;

    private Integer duration;

    @TableField("created_at")
    private Date createTime;
}
