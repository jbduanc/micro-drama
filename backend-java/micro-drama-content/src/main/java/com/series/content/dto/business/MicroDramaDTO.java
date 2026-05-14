package com.series.content.dto.business;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 短剧传输对象（对应 content_db.drama + episode）
 */
@Data
public class MicroDramaDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer page;
    private Integer size;

    private String id;
    private String title;
    private String coverUrl;
    private String description;
    private Integer totalEpisodes;
    private BigDecimal price;
    private Integer status;
    private Integer sort;

    private List<DramaEpisodeDTO> episodes;
}
