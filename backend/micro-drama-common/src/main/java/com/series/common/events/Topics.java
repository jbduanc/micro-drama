package com.series.common.events;

/**
 * Kafka topic names shared across services.
 *
 * 注意：topic 名字一旦上线尽量别改；如果要改，用新 topic + 兼容消费。
 */
public final class Topics {
    private Topics() {
    }

    /**
     * content -> video：上传完成，触发转码
     */
    public static final String CONTENT_VIDEO_UPLOAD_COMPLETED = "content.video.upload_completed";

    /**
     * video -> content：转码完成
     */
    public static final String VIDEO_TRANSCODE_COMPLETED = "video.transcode.completed";

    /**
     * video -> content：转码失败
     */
    public static final String VIDEO_TRANSCODE_FAILED = "video.transcode.failed";
}

