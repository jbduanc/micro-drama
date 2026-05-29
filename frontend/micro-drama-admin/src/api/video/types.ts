import type { Result } from "@/api/drama/types"

export type { Result }

export type UploadUrlData = {
  videoId: string
  uploadUrl: string
  fileKey: string
  expiresIn: number
}

/** STS 直传 OSS（ali-oss SDK） */
export type StsUploadData = {
  videoId: string
  fileKey: string
  accessKeyId: string
  accessKeySecret: string
  securityToken: string
  expiration: string
  expiresIn: number
  region: string
  bucket: string
  endpoint: string
  /** OSS 上传回调公网地址（STS 接口返回，新加坡等无 MNS 区域使用） */
  callbackUrl?: string
}

export type NotifyTranscodeData = {
  videoId: string
  sourceObjectKey: string
  sourceEtag?: string
  transcodeTaskId?: string
}

export type PlayAuthData = {
  videoId: string
  playUrl: string
  token: string
  expiresIn: number
  status: string
  hlsPath: string
}

export type DeleteVideoItem = {
  videoId: string
  fileKey?: string
}

export type DeleteVideosData = {
  deleted: string[]
  failed?: string[]
}
