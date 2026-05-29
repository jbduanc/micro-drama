package transcode

import (
	"bufio"
	"bytes"
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

	hasAudio := probeHasAudio(ctx, ffprobePath(cfg.Transcode.FFmpegPath), inputPath)
	log.Info("transcode input probed", zap.String("input", inputPath), zap.Bool("hasAudio", hasAudio))

	var out []VariantResult
	for _, v := range cfg.Transcode.Variants {
		varDir := filepath.Join(outDir, v.Name)
		if err := os.MkdirAll(varDir, 0o755); err != nil {
			return "", nil, err
		}
		playlist := filepath.Join(varDir, "index.m3u8")
		segmentPattern := filepath.Join(varDir, "%03d.ts")

		// libx264 要求宽高为偶数；scale=w:h:decrease 可能得到奇数尺寸，末尾强制偶数。
		// 音轨用 0:a:0? 可选映射，避免纯视频素材因无音频流导致编码失败。
		scaleVF := fmt.Sprintf(
			"scale=w=%d:h=%d:force_original_aspect_ratio=decrease,scale=trunc(iw/2)*2:trunc(ih/2)*2",
			v.Width, v.Height,
		)
		args := []string{
			"-y",
			"-i", inputPath,
			"-map", "0:v:0",
			"-vf", scaleVF,
			"-c:v", "libx264",
			"-profile:v", "main",
			"-preset", "veryfast",
			"-pix_fmt", "yuv420p",
			"-b:v", fmt.Sprintf("%dk", v.BitrateKbps),
			"-maxrate", fmt.Sprintf("%dk", v.BitrateKbps),
			"-bufsize", fmt.Sprintf("%dk", v.BitrateKbps*2),
			"-g", "48",
			"-keyint_min", "48",
			"-sc_threshold", "0",
			"-hls_time", strconv.Itoa(cfg.Transcode.SegmentSeconds),
			"-hls_playlist_type", "vod",
			"-hls_segment_filename", segmentPattern,
			playlist,
		}
		if hasAudio {
			args = insertBefore(args, "-hls_time", "-map", "0:a:0?", "-c:a", "aac", "-b:a", "128k", "-ac", "2", "-ar", "48000")
		}

		if err := runFFmpeg(ctx, log, cfg.Transcode.FFmpegPath, args, v.Name); err != nil {
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

func runFFmpeg(ctx context.Context, log *zap.Logger, ffmpegPath string, args []string, variant string) error {
	cmd := exec.CommandContext(ctx, ffmpegPath, args...)
	var stderrBuf bytes.Buffer
	stderr, err := cmd.StderrPipe()
	if err != nil {
		return err
	}
	cmd.Stdout = os.Stdout

	if err := cmd.Start(); err != nil {
		return err
	}

	sc := bufio.NewScanner(stderr)
	for sc.Scan() {
		line := strings.TrimSpace(sc.Text())
		if line == "" {
			continue
		}
		stderrBuf.WriteString(line)
		stderrBuf.WriteByte('\n')
		log.Info("ffmpeg", zap.String("variant", variant), zap.String("msg", line))
	}
	_ = sc.Err()

	if err := cmd.Wait(); err != nil {
		tail := tailLines(stderrBuf.String(), 12)
		return fmt.Errorf("ffmpeg failed (variant=%s): %w; stderr tail:\n%s", variant, err, tail)
	}
	return nil
}

func ffprobePath(ffmpegPath string) string {
	if strings.Contains(ffmpegPath, "ffmpeg") {
		return strings.Replace(ffmpegPath, "ffmpeg", "ffprobe", 1)
	}
	return "ffprobe"
}

func probeHasAudio(ctx context.Context, ffprobePath, input string) bool {
	cmd := exec.CommandContext(ctx, ffprobePath,
		"-v", "error",
		"-select_streams", "a:0",
		"-show_entries", "stream=index",
		"-of", "csv=p=0",
		input,
	)
	out, err := cmd.Output()
	return err == nil && strings.TrimSpace(string(out)) != ""
}

// insertBefore inserts tokens immediately before the first occurrence of before.
func insertBefore(args []string, before string, insert ...string) []string {
	for i, a := range args {
		if a == before {
			out := make([]string, 0, len(args)+len(insert))
			out = append(out, args[:i]...)
			out = append(out, insert...)
			out = append(out, args[i:]...)
			return out
		}
	}
	return append(append([]string{}, args...), insert...)
}

func tailLines(s string, n int) string {
	lines := strings.Split(strings.TrimSpace(s), "\n")
	if len(lines) <= n {
		return s
	}
	return strings.Join(lines[len(lines)-n:], "\n")
}
