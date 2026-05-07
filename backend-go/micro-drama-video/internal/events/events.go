package events

// Topics align with backend/micro-drama-common Topics.java

const (
	ContentVideoUploadCompleted = "content.video.upload_completed"
	VideoTranscodeCompleted     = "video.transcode.completed"
	VideoTranscodeFailed        = "video.transcode.failed"
)

// VideoUploadCompletedEvent matches Java VideoUploadCompletedEvent (JSON field names).
type VideoUploadCompletedEvent struct {
	VideoID         string `json:"videoId"`
	SourceObjectKey string `json:"sourceObjectKey"`
	SourceEtag      string `json:"sourceEtag,omitempty"`
	UploadedAt      any    `json:"uploadedAt,omitempty"` // millis number or ISO string from Java
}

type TranscodeVariant struct {
	Width        int32  `json:"width,omitempty"`
	Height       int32  `json:"height,omitempty"`
	BitrateKbps  int32  `json:"bitrateKbps,omitempty"`
	PlaylistKey  string `json:"playlistKey,omitempty"`
}

type VideoTranscodeCompletedEvent struct {
	VideoID           string             `json:"videoId"`
	MasterPlaylistKey string             `json:"masterPlaylistKey"`
	Variants          []TranscodeVariant `json:"variants,omitempty"`
}

type VideoTranscodeFailedEvent struct {
	VideoID string `json:"videoId"`
	Reason  string `json:"reason"`
	Detail  string `json:"detail,omitempty"`
}
