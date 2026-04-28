import { http } from "@/api/http"
import type { Result, UserInfoDTO } from "@/api/auth/types"

export const authService = {
  async getAuthorizeUrl(redirectUri: string): Promise<string> {
    const res = await http.get<Result<string>>("/oauth2/authorize-url", {
      params: { redirectUri },
    })
    return res.data.data
  },

  async loginWithGoogleCode(code: string, redirectUri: string): Promise<string> {
    const res = await http.post<Result<string>>("/oauth2/login/google", { code, redirectUri })
    return res.data.data
  },

  async getUserInfo(): Promise<UserInfoDTO> {
    const res = await http.get<Result<UserInfoDTO>>("/oauth2/user/info")
    return res.data.data
  },

  async logout(): Promise<void> {
    await http.post("/oauth2/logout")
  },
}

