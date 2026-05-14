export type MicroDramaStatus = 0 | 1

/** 对应 content_db.episode */
export type DramaEpisode = {
  /** 已有剧集 UUID，新建不传 */
  id?: string
  episodeNum: number
  title: string
  /** 关联 video_db.video_asset.id */
  videoAssetId?: string
  duration?: number
  price?: number
}

/**
 * 与后端 `MicroDramaDTO` 对齐（content_db.drama）
 */
export type MicroDramaDTO = {
  page?: number
  size?: number

  /** drama 主键 UUID */
  id?: string
  title?: string
  coverUrl?: string
  description?: string
  totalEpisodes?: number
  price?: number
  status?: MicroDramaStatus
  sort?: number

  episodes?: DramaEpisode[]
}

export type TablePageInfo<T> = {
  data?: T[]
  list?: T[]
  total: number
}

export type Result<T> = {
  code?: number
  msg?: string
  message?: string
  data: T
}
