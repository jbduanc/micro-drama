import type { AxiosInstance, InternalAxiosRequestConfig } from "axios"
import { useAuthStore } from "@/stores/useAuthStore"
import { authService } from "@/api/auth/service"

const REFRESHED_HEADER = "x-access-token-refreshed"

let refreshPromise: Promise<string | null> | null = null

async function refreshAccessToken(): Promise<string | null> {
  if (!refreshPromise) {
    refreshPromise = authService
      .refreshSession()
      .then((tokens) => tokens?.accessToken ?? null)
      .catch(() => null)
      .finally(() => {
        refreshPromise = null
      })
  }
  return refreshPromise
}

function applyRefreshedToken(config: InternalAxiosRequestConfig, token: string): void {
  useAuthStore.getState().setTokens({ accessToken: token })
  config.headers.set("Authorization", `Bearer ${token}`)
}

/** 为 axios 实例挂载 Bearer、Cookie、401 静默 refresh */
export function setupAxiosAuth(client: AxiosInstance): void {
  client.defaults.withCredentials = true

  client.interceptors.request.use((config) => {
    const token = useAuthStore.getState().accessToken
    if (token) {
      config.headers.set("Authorization", `Bearer ${token}`)
    }
    // refresh 走 httpOnly Cookie（withCredentials）；勿传 X-Refresh-Token，
    // localStorage 里的旧值会优先于 Cookie 导致网关静默 refresh 失败
    return config
  })

  client.interceptors.response.use(
    (res) => {
      if (res.headers[REFRESHED_HEADER] === "true") {
        const auth = res.headers.authorization as string | undefined
        const access = auth?.startsWith("Bearer ") ? auth.slice(7) : null
        if (access) {
          useAuthStore.getState().setTokens({ accessToken: access })
        }
      }
      return res
    },
    async (error) => {
      const status = error?.response?.status
      const original = error?.config as InternalAxiosRequestConfig & { _retry?: boolean }
      if (status === 401 && original && !original._retry) {
        original._retry = true
        const newToken = await refreshAccessToken()
        if (newToken) {
          applyRefreshedToken(original, newToken)
          return client.request(original)
        }
        useAuthStore.getState().clearSession()
        if (window.location.pathname !== "/login") {
          window.location.replace("/login")
        }
      }
      return Promise.reject(error)
    },
  )
}
