package worker

import (
	"context"
	"fmt"
	"os"
	"os/exec"
	"path/filepath"

	"go.uber.org/zap"

	"micro-drama-video/internal/config"
	"micro-drama-video/internal/events"
)

// ProcessUploadCompleted is the main pipeline hook: download (optional) → FFmpeg → upload HLS (TODO).
func ProcessUploadCompleted(ctx context.Context, log *zap.Logger, cfg *config.Config, ev *events.VideoUploadCompletedEvent) (*events.VideoTranscodeCompletedEvent, *events.VideoTranscodeFailedEvent) {
	if ev == nil || ev.VideoID == "" {
		return nil, &events.VideoTranscodeFailedEvent{VideoID: "", Reason: "invalid_event", Detail: "missing videoId"}
	}

	workDir := filepath.Join(cfg.Worker.WorkDir, ev.VideoID)
	if err := os.MkdirAll(workDir, 0o755); err != nil {
		return nil, &events.VideoTranscodeFailedEvent{VideoID: ev.VideoID, Reason: "mkdir", Detail: err.Error()}
	}

	// Placeholder: real impl downloads object from S3 to localPath, runs FFmpeg, uploads m3u8+ts.
	localSrc := filepath.Join(workDir, "source.bin")
	if err := os.WriteFile(localSrc, []byte("placeholder — replace with S3 GetObject\n"), 0o644); err != nil {
		return nil, &events.VideoTranscodeFailedEvent{VideoID: ev.VideoID, Reason: "write_placeholder", Detail: err.Error()}
	}

	outDir := filepath.Join(workDir, "hls")
	if err := os.MkdirAll(outDir, 0o755); err != nil {
		return nil, &events.VideoTranscodeFailedEvent{VideoID: ev.VideoID, Reason: "mkdir_hls", Detail: err.Error()}
	}

	// Example HLS ladder (adjust when wiring real source file):
	// ffmpeg -i input -c:v libx264 -c:a aac -hls_time 6 -hls_list_size 0 -f hls out.m3u8
	master := filepath.Join(outDir, "master.m3u8")
	cmd := exec.CommandContext(ctx, cfg.Worker.FFmpegPath,
		"-y", "-i", localSrc,
		"-c:v", "libx264", "-c:a", "aac",
		"-hls_time", "6", "-hls_list_size", "0",
		"-f", "hls", master,
	)
	if out, err := cmd.CombinedOutput(); err != nil {
		log.Warn("ffmpeg failed (expected until real input)", zap.Error(err), zap.String("output", string(out)))
		// Do not fail the worker permanently in dev: emit failed event
		return nil, &events.VideoTranscodeFailedEvent{
			VideoID: ev.VideoID,
			Reason:  "ffmpeg",
			Detail:  fmt.Sprintf("%v: %s", err, string(out)),
		}
	}

	relKey := fmt.Sprintf("hls/%s/master.m3u8", ev.VideoID)
	return &events.VideoTranscodeCompletedEvent{
		VideoID:           ev.VideoID,
		MasterPlaylistKey: relKey,
	}, nil
}
