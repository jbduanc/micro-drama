import OSS from "ali-oss"

import type { StsUploadData } from "./types"

/** 使用 STS + ali-oss SDK 直传至 OSS Bucket 自带域名；携带上传回调触发转码（无需 MNS 事件通知） */
export async function uploadFileWithSts(
  file: File,
  sts: StsUploadData,
  onProgress?: (percent: number) => void,
): Promise<void> {
  const client = new OSS({
    region: sts.region,
    accessKeyId: sts.accessKeyId,
    accessKeySecret: sts.accessKeySecret,
    stsToken: sts.securityToken,
    bucket: sts.bucket,
    endpoint: sts.endpoint,
    secure: true,
  })

  const options: OSS.MultipartUploadOptions = {
    parallel: 4,
    partSize: 1024 * 1024,
    mime: file.type || "video/mp4",
    headers: {
      "x-oss-meta-videoid": sts.videoId,
    },
    progress: (p: number) => {
      if (onProgress) {
        const percent = Math.round(p * 100)
        onProgress(Math.min(100, Math.max(0, percent)))
      }
    },
  }

  // 新加坡等无 MNS 区域：在上传请求中携带 callback，OSS 服务器直调 video-api
  if (sts.callbackUrl?.trim()) {
    options.callback = {
      url: sts.callbackUrl.trim(),
      body:
        "bucket=${bucket}&object=${object}&etag=${etag}&size=${size}&mimeType=${mimeType}&videoId=${x:videoId}",
      contentType: "application/x-www-form-urlencoded",
      customValue: {
        videoId: sts.videoId,
      },
    }
  }

  await client.multipartUpload(sts.fileKey, file, options)
}
