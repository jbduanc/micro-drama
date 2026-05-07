package com.series.content.entity.business;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 短剧剧集表
 */
@Data
@TableName("t_drama_episodes")
public class DramaEpisodes extends Model<DramaEpisodes> implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer episodeId;
    private Long dramaId;
    private Integer episodeNum;
    private String episodeTitle;
    private String videoUrl;
    private BigDecimal singleEpisodePrice;
    private Integer duration;
    private Date createTime;
}

