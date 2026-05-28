import axios from "axios"
import { useAuthStore } from "@/stores/useAuthStore"

/**
 * 视频域 HTTP，直连 micro-drama-video（开发环境经 Vite 代理 /video-api）。
 */
const baseURL =
  import.meta.env.VITE_VIDEO_API_BASE != null &&
  String(import.meta.env.VITE_VIDEO_API_BASE).trim() !== ""
    ? String(import.meta.env.VITE_VIDEO_API_BASE).trim()
    : "/video-api"

export const videoHttp = axios.create({
  baseURL,
  timeout: 120_000,
})

videoHttp.interceptors.request.use((config) => {
  const token = useAuthStore.getState().token
  if (token) {
    config.headers = config.headers ?? {}
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

videoHttp.interceptors.response.use(
  (res) => res,
  (error) => {
    const status = error?.response?.status
    if (status === 401) {
      useAuthStore.getState().clearSession()
      if (window.location.pathname !== "/login") {
        window.location.replace("/login")
      }
    }
    return Promise.reject(error)
  },
)
