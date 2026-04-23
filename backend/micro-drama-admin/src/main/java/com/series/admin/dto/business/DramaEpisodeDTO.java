package com.series.admin.dto.business;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 剧集传输对象
 */
@Data
public class DramaEpisodeDTO {
    private Integer episodeId;       // 编辑时使用
    private Integer episodeNum;
    private String episodeTitle;
    private String videoUrl;
    private BigDecimal singleEpisodePrice;
    private Integer duration;
}