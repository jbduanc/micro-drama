import type { AxiosInstance } from "axios"
import { useAuthStore } from "@/stores/useAuthStore"

/** 为 axios 实例挂载 Bearer 与 401 跳转（避免 headers 赋值的 TS 类型问题） */
export function setupAxiosAuth(client: AxiosInstance): void {
  client.interceptors.request.use((config) => {
    const token = useAuthStore.getState().token
    if (token) {
      config.headers.set("Authorization", `Bearer ${token}`)
    }
    return config
  })

  client.interceptors.response.use(
    (res) => res,
    (error) => {
      if (error?.response?.status === 401) {
        useAuthStore.getState().clearSession()
        if (window.location.pathname !== "/login") {
          window.location.replace("/login")
        }
      }
      return Promise.reject(error)
    },
  )
}
