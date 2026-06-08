import { http } from "@/api/http"
import type { Result, UserInfoDTO } from "@/api/auth/types"
import { useAuthStore } from "@/stores/useAuthStore"

export type TokenPair = {
  accessToken: string
  refreshToken: string
}

export const authService = {
  async getAuthorizeUrl(redirectUri: string): Promise<string> {
    const res = await http.get<Result<string>>("/oauth2/authorize-url", {
      params: { redirectUri },
    })
    const url = res.data?.data
    if (!url) {
      throw new Error("Empty authorize url")
    }
    return url
  },

  async loginWithGoogleCode(code: string, redirectUri: string): Promise<TokenPair> {
    const res = await http.post<Result<TokenPair>>("/oauth2/login/google", { code, redirectUri })
    const data = res.data?.data
    if (!data?.accessToken || !data?.refreshToken) {
      throw new Error("Empty token pair")
    }
    return data
  },

  async refreshSession(): Promise<TokenPair | null> {
    const refreshToken = useAuthStore.getState().refreshToken
    const res = await http.post<Result<TokenPair>>(
      "/oauth2/refresh",
      refreshToken ? { refreshToken } : {},
    )
    const data = res.data?.data
    if (!data?.accessToken || !data?.refreshToken) {
      return null
    }
    useAuthStore.getState().setTokens({
      accessToken: data.accessToken,
      refreshToken: data.refreshToken,
    })
    return data
  },

  async getUserInfo(): Promise<UserInfoDTO> {
    const res = await http.get<Result<UserInfoDTO>>("/oauth2/user/info")
    return res.data.data
  },

  async logout(): Promise<void> {
    await http.post("/oauth2/logout")
  },
}
