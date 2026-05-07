package com.series.content.api.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class UploadUrlResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private String videoId;
    private String uploadUrl;
    private String objectKey;
    private Long expiresInSeconds;
}

