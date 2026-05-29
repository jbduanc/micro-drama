import OSS from "ali-oss"

import type { StsUploadData } from "./types"

/** OSS 已存盘但回调 video-api 失败（常见网关 502）；文件在桶里，可由 notify-transcode 继续。 */
export function isOssCallbackFailedError(err: unknown): boolean {
  if (!err || typeof err !== "object") return false
  const name = "name" in err ? String((err as { name?: string }).name) : ""
  const msg = "message" in err ? String((err as { message?: string }).message) : String(err)
  return name === "CallbackFailedError" || /callbackfailed/i.test(msg) || /error status\s*:\s*502/i.test(msg)
}

/**
 * STS + ali-oss 分片直传。
 * 不携带 OSS PutObject Callback：回调经公网网关易 502；上传成功后由 notify-transcode 触发转码。
 */
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

  try {
    await client.multipartUpload(sts.fileKey, file, options)
  } catch (err) {
    if (isOssCallbackFailedError(err)) {
      console.warn("[oss] upload callback failed (e.g. 502), object may already exist in bucket", err)
      return
    }
    throw err
  }
}
