package com.series.content.dto.business;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 短剧传输对象（用于接收和返回短剧及关联剧集信息）
 */
@Data
public class MicroDramaDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    // 分页参数
    private Integer page;
    private Integer size;

    // 短剧基本信息
    private Long dramaId;
    private String title;
    private String coverUrl;
    private String description;
    private Integer totalEpisodes;
    private BigDecimal singleDramaPrice;
    private Integer status;
    private Integer sort;

    // 关联剧集列表
    private List<DramaEpisodeDTO> episodes;
}

