package com.series.content.dto.business;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class DramaEpisodeDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer episodeId;
    private Integer episodeNum;
    private String episodeTitle;
    private String videoUrl;
    private BigDecimal singleEpisodePrice;
    private Integer duration;
}

