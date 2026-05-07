package com.series.content.api;

import com.series.common.entity.Result;
import com.series.content.api.dto.UploadCompleteRequest;
import com.series.content.api.dto.UploadUrlRequest;
import com.series.content.api.dto.UploadUrlResponse;
import com.series.content.api.dto.VideoStatusResponse;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/content/video")
public class ContentVideoController {

    @PostMapping("/upload-url")
    public Result<UploadUrlResponse> createUploadUrl(@RequestBody UploadUrlRequest req) {
        // 占位：后续接 Vultr Object Storage（S3兼容）预签名URL
        UploadUrlResponse resp = new UploadUrlResponse();
        resp.setVideoId(UUID.randomUUID().toString());
        resp.setObjectKey("uploads/" + resp.getVideoId() + "/" + (req.getFileName() == null ? "video.bin" : req.getFileName()));
        resp.setUploadUrl("TODO:s3-presigned-put-url");
        resp.setExpiresInSeconds(900L);
        return Result.ok(resp);
    }

    @PostMapping("/complete")
    public Result<Void> complete(@RequestBody UploadCompleteRequest req) {
        // 占位：后续落库 video_asset 状态=UPLOADED，并发送 Kafka 事件 content.video.upload_completed
        return Result.ok();
    }

    @GetMapping("/status")
    public Result<VideoStatusResponse> status(@RequestParam("videoId") String videoId) {
        // 占位：后续读取 video_asset 状态（UPLOADING/UPLOADED/TRANSCODING/READY/FAILED）
        VideoStatusResponse resp = new VideoStatusResponse();
        resp.setVideoId(videoId);
        resp.setStatus("TODO");
        resp.setMasterPlaylistKey(null);
        return Result.ok(resp);
    }
}

