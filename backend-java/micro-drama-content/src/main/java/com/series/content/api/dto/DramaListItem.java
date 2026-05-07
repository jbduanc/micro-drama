package com.series.content.api.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class DramaListItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private String dramaId;
    private String title;
    private String coverUrl;
}

