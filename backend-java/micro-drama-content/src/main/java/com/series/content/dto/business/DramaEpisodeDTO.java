package com.series.content.dto.business;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class DramaEpisodeDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private Integer episodeNum;
    private String title;
    private String videoAssetId;
    private BigDecimal price;
    private Integer duration;
}
