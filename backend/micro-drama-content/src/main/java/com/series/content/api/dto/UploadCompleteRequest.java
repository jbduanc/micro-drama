package com.series.content.api.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class UploadCompleteRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private String videoId;
    private String objectKey;
    private String etag;
}

