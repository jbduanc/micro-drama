package com.series.content.api.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class UploadUrlRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private String fileName;
    private String contentType;
    private Long fileSize;
}

