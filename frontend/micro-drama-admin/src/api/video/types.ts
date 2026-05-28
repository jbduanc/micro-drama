import type { Result } from "@/api/drama/types"

export type { Result }

export type UploadUrlData = {
  videoId: string
  uploadUrl: string
  fileKey: string
  expiresIn: number
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
