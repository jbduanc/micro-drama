import { http } from "@/api/http"
import type { Result, UserInfoDTO } from "@/api/auth/types"

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

  async loginWithGoogleCode(code: string, redirectUri: string): Promise<string> {
    const res = await http.post<Result<string>>("/oauth2/login/google", { code, redirectUri })
    const token = res.data?.data
    if (!token) {
      throw new Error("Empty token")
    }
    return token
  },

  async getUserInfo(): Promise<UserInfoDTO> {
    const res = await http.get<Result<UserInfoDTO>>("/oauth2/user/info")
    return res.data.data
  },

  async logout(): Promise<void> {
    await http.post("/oauth2/logout")
  },
}

