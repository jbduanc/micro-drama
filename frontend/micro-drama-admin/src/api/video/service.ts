import { videoHttp } from "@/api/videoHttp"
import { uploadFileWithSts } from "@/api/video/ossStsUpload"
import type {
  DeleteVideoItem,
  DeleteVideosData,
  NotifyTranscodeData,
  PlayAuthData,
  Result,
  StsUploadData,
  UploadUrlData,
} from "./types"

function unwrap<T>(res: Result<T>): T {
  if (res.code != null && res.code !== 0) {
    throw new Error(res.msg || res.message || "request failed")
  }
  return res.data
}

export const videoService = {
  /** 获取阿里云 STS 临时凭证（上传前调用） */
  async fetchSts(payload: {
    dramaId: string
    episodeId: string
    contentType?: string
  }): Promise<StsUploadData> {
    const res = await videoHttp.post<Result<StsUploadData>>("/v1/video/sts", payload)
    return unwrap(res.data)
  },

  /** @deprecated 预签名 PUT，请使用 fetchSts + uploadFileWithSts（ali-oss） */
  async createUploadUrl(payload: {
    dramaId: string
    episodeId: string
    contentType?: string
  }): Promise<UploadUrlData> {
    const res = await videoHttp.post<Result<UploadUrlData>>("/v1/video/upload-url", payload)
    return unwrap(res.data)
  },

  /** 手动通知转码（OSS 事件不可达时的兼容接口） */
  async notifyTranscode(payload: {
    videoId: string
    fileKey: string
    dramaId: string
    episodeId: string
    etag?: string
    sizeBytes?: number
  }): Promise<NotifyTranscodeData> {
    const res = await videoHttp.post<Result<NotifyTranscodeData>>(
      "/v1/video/notify-transcode",
      payload,
    )
    return unwrap(res.data)
  },

  async play(videoId: string, orderId?: string): Promise<PlayAuthData> {
    const res = await videoHttp.get<Result<PlayAuthData>>("/v1/video/play", {
      params: { videoId, orderId: orderId || undefined },
    })
    return unwrap(res.data)
  },

  async deleteVideos(items: DeleteVideoItem[]): Promise<DeleteVideosData> {
    const res = await videoHttp.post<Result<DeleteVideosData>>("/v1/video/delete", { items })
    return unwrap(res.data)
  },
}

export { uploadFileWithSts }

/** 直传 OSS（PUT 预签名 URL），支持上传进度 */
export function uploadFileToOSS(
  file: File,
  uploadUrl: string,
  contentType: string,
  onProgress?: (percent: number) => void,
): Promise<void> {
  return new Promise((resolve, reject) => {
    const xhr = new XMLHttpRequest()
    xhr.open("PUT", uploadUrl)
    xhr.setRequestHeader("Content-Type", contentType || "video/mp4")
    xhr.upload.onprogress = (e) => {
      if (e.lengthComputable && onProgress) {
        onProgress(Math.round((e.loaded / e.total) * 100))
      }
    }
    xhr.onload = () => {
      if (xhr.status >= 200 && xhr.status < 300) {
        resolve()
        return
      }
      reject(new Error(`OSS 上传失败: HTTP ${xhr.status}`))
    }
    xhr.onerror = () => reject(new Error("OSS 上传网络错误"))
    xhr.send(file)
  })
}
