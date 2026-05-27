package transcode

import (
	"bufio"
	"context"
	"fmt"
	"os"
	"os/exec"
	"path/filepath"
	"strconv"
	"strings"

	"go.uber.org/zap"

	"micro-drama-transcoder/internal/config"
)

type VariantResult struct {
	Name        string
	Width       int
	Height      int
	BitrateKbps int
	PlaylistRel string // e.g. "480p/index.m3u8"
}

// TranscodeToHLS produces:
// - <outDir>/<variant>/index.m3u8 + segments
// - <outDir>/index.m3u8 (master)
func TranscodeToHLS(ctx context.Context, log *zap.Logger, cfg *config.Config, inputPath, outDir string) (masterFile string, variants []VariantResult, err error) {
	if err := os.MkdirAll(outDir, 0o755); err != nil {
		return "", nil, err
	}
	if len(cfg.Transcode.Variants) == 0 {
		return "", nil, fmt.Errorf("no transcode variants configured")
	}

	var out []VariantResult
	for _, v := range cfg.Transcode.Variants {
		varDir := filepath.Join(outDir, v.Name)
		if err := os.MkdirAll(varDir, 0o755); err != nil {
			return "", nil, err
		}
		playlist := filepath.Join(varDir, "index.m3u8")
		segmentPattern := filepath.Join(varDir, "%03d.ts")

		// Notes:
		// - force_key_frames improves segment boundary for seeking
		// - we keep audio AAC, video H264 for wide compatibility
		args := []string{
			"-y",
			"-i", inputPath,
			"-vf", fmt.Sprintf("scale=w=%d:h=%d:force_original_aspect_ratio=decrease", v.Width, v.Height),
			"-c:v", "libx264",
			"-profile:v", "main",
			"-preset", "veryfast",
			"-b:v", fmt.Sprintf("%dk", v.BitrateKbps),
			"-maxrate", fmt.Sprintf("%dk", v.BitrateKbps),
			"-bufsize", fmt.Sprintf("%dk", v.BitrateKbps*2),
			"-g", "48",
			"-keyint_min", "48",
			"-sc_threshold", "0",
			"-c:a", "aac",
			"-b:a", "128k",
			"-ac", "2",
			"-ar", "48000",
			"-hls_time", strconv.Itoa(cfg.Transcode.SegmentSeconds),
			"-hls_playlist_type", "vod",
			"-hls_segment_filename", segmentPattern,
			playlist,
		}

		if err := runFFmpeg(ctx, log, cfg.Transcode.FFmpegPath, args); err != nil {
			return "", nil, err
		}

		out = append(out, VariantResult{
			Name:        v.Name,
			Width:       v.Width,
			Height:      v.Height,
			BitrateKbps: v.BitrateKbps,
			PlaylistRel: filepath.ToSlash(filepath.Join(v.Name, "index.m3u8")),
		})
	}

	master := filepath.Join(outDir, "index.m3u8")
	if err := writeMaster(master, out); err != nil {
		return "", nil, err
	}
	return master, out, nil
}

func writeMaster(path string, vars []VariantResult) error {
	f, err := os.Create(path)
	if err != nil {
		return err
	}
	defer f.Close()

	w := bufio.NewWriter(f)
	_, _ = w.WriteString("#EXTM3U\n")
	_, _ = w.WriteString("#EXT-X-VERSION:3\n")

	for _, v := range vars {
		// rough bandwidth estimation (bits per second)
		bw := (v.BitrateKbps * 1000) + 128000
		_, _ = w.WriteString(fmt.Sprintf("#EXT-X-STREAM-INF:BANDWIDTH=%d,RESOLUTION=%dx%d\n", bw, v.Width, v.Height))
		_, _ = w.WriteString(v.PlaylistRel + "\n")
	}
	return w.Flush()
}

func runFFmpeg(ctx context.Context, log *zap.Logger, ffmpegPath string, args []string) error {
	cmd := exec.CommandContext(ctx, ffmpegPath, args...)
	stderr, err := cmd.StderrPipe()
	if err != nil {
		return err
	}
	cmd.Stdout = os.Stdout

	if err := cmd.Start(); err != nil {
		return err
	}

	// stream stderr lines into logs (ffmpeg prints progress on stderr)
	sc := bufio.NewScanner(stderr)
	for sc.Scan() {
		line := strings.TrimSpace(sc.Text())
		if line != "" {
			log.Info("ffmpeg", zap.String("msg", line))
		}
	}
	_ = sc.Err()

	if err := cmd.Wait(); err != nil {
		return fmt.Errorf("ffmpeg failed: %w", err)
	}
	return nil
}
