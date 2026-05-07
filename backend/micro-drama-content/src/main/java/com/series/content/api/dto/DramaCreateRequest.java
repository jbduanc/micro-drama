package com.series.content.api.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class DramaCreateRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private String title;
    private String coverUrl;
    private String description;
}

