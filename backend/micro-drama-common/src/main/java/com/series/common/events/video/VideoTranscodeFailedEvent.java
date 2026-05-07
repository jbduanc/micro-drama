package com.series.common.events.video;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * video -> content: 转码失败
 */
@Data
public class VideoTranscodeFailedEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private String videoId;
    private String reason;
    private String detail;
    private Date failedAt;
}

