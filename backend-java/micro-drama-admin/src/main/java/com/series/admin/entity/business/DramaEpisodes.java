package com.series.admin.entity.business;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

import java.util.Date;
import java.io.Serializable;

/**
 * <p>
 * 短剧剧集表
 * </p>
 *
 * @author djbo
 * @since 2026-04-15
 */
@Data
@TableName("t_drama_episodes")
public class DramaEpisodes extends Model<DramaEpisodes> {

    private static final long serialVersionUID = 1L;

    /**
     * 剧集ID(主键)
     */
    private Integer episodeId;

    /**
     * 关联短剧ID
     */
    private Long dramaId;

    /**
     * 剧集序号(1/2/3...)
     */
    private Integer episodeNum;

    /**
     * 剧集标题
     */
    private String episodeTitle;

    /**
     * 视频播放地址
     */
    private String videoUrl;

    /**
     * 单集购买价格(单位：TON)
     */
    private BigDecimal singleEpisodePrice;

    /**
     * 视频时长(单位：秒)
     */
    private Integer duration;

    /**
     * 创建时间
     */
    private Date createTime;
}
