export type Result<T> = {
  code: number
  msg: string
  data: T
}

export type UserInfoDTO = {
  id: number
  nickname: string
  googleEmail: string
  avatar?: string
  status: number
}

