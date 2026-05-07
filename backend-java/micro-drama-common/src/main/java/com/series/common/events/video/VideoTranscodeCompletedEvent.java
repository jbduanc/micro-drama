package com.series.common.events.video;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * video -> content: 转码完成（HLS产物可用）
 */
@Data
public class VideoTranscodeCompletedEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private String videoId;

    /**
     * HLS master playlist 的对象存储 key
     */
    private String masterPlaylistKey;

    /**
     * 变体列表（多码率）
     */
    private List<Variant> variants;

    private Date completedAt;

    @Data
    public static class Variant implements Serializable {
        private static final long serialVersionUID = 1L;
        private Integer width;
        private Integer height;
        private Integer bitrateKbps;
        private String playlistKey;
    }
}

