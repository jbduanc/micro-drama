package com.series.admin.dto.business;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 剧集传输对象（对应 content_db.episode）
 */
@Data
public class DramaEpisodeDTO {
    /** 剧集主键 UUID，新建可为空 */
    private String id;
    private Integer episodeNum;
    private String title;
    /** 关联 video_db.video_asset.id */
    private String videoAssetId;
    private BigDecimal price;
    private Integer duration;
}
