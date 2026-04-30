export type MicroDramaStatus = 0 | 1

export type DramaEpisode = {
  episodeId?: number | string
  dramaId?: number | string
  episodeNum: number
  title: string
  videoUrl?: string
  durationSeconds?: number
  price?: number
  status?: MicroDramaStatus
  sort?: number
}

/**
 * 与后端 `MicroDramaDTO` 对齐（用于列表查询、详情、保存）
 */
export type MicroDramaDTO = {
  // 分页参数
  page?: number
  size?: number

  // 短剧基本信息
  dramaId?: number | string
  title?: string
  coverUrl?: string
  description?: string
  totalEpisodes?: number
  singleDramaPrice?: number
  status?: MicroDramaStatus
  sort?: number

  // 关联剧集列表（新增/编辑时提交；详情返回）
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
