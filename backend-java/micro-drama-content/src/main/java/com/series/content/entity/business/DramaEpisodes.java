package com.series.content.entity.business;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 对应 content_db.episode
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("episode")
public class DramaEpisodes extends Model<DramaEpisodes> implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @TableField("drama_id")
    private String dramaId;

    @TableField("episode_num")
    private Integer episodeNum;

    private String title;

    @TableField("video_asset_id")
    private String videoAssetId;

    private BigDecimal price;

    private Integer duration;

    @TableField("created_at")
    private Date createTime;
}
