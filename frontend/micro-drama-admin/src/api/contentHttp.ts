import axios from "axios"
import { useAuthStore } from "@/stores/useAuthStore"

/**
 * 短剧等内容域 HTTP，直连 micro-drama-content（开发环境经 Vite 代理 /content-api）。
 */
const baseURL =
  import.meta.env.VITE_CONTENT_API_BASE != null &&
  String(import.meta.env.VITE_CONTENT_API_BASE).trim() !== ""
    ? String(import.meta.env.VITE_CONTENT_API_BASE).trim()
    : "/content-api"

export const contentHttp = axios.create({
  baseURL,
  timeout: 30_000,
})

contentHttp.interceptors.request.use((config) => {
  const token = useAuthStore.getState().token
  if (token) {
    config.headers = config.headers ?? {}
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

contentHttp.interceptors.response.use(
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
