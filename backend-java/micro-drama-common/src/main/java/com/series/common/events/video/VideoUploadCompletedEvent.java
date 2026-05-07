package com.series.common.events.video;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * content -> video: 上传已完成，开始转码
 */
@Data
public class VideoUploadCompletedEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 视频资源ID（由 content 服务生成）
     */
    private String videoId;

    /**
     * 原始文件在对象存储的 Key（例如 s3://bucket/key 里的 key）
     */
    private String sourceObjectKey;

    /**
     * 原始文件的 ETag / checksum（可选，用于校验）
     */
    private String sourceEtag;

    /**
     * 上传完成时间
     */
    private Date uploadedAt;
}

