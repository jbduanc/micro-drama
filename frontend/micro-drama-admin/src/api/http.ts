import axios from "axios"
import { useAuthStore } from "@/stores/useAuthStore"

export const http = axios.create({
  baseURL: "/admin-api",
  timeout: 30_000,
})

http.interceptors.request.use((config) => {
  const token = useAuthStore.getState().token
  if (token) {
    config.headers = config.headers ?? {}
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  (res) => res,
  (error) => {
    const status = error?.response?.status
    if (status === 401) {
      // Token invalid/expired → clear session and redirect to login.
      useAuthStore.getState().clearSession()
      if (window.location.pathname !== "/login") {
        window.location.replace("/login")
      }
    }
    return Promise.reject(error)
  },
)

