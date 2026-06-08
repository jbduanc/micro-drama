import { create } from "zustand"
import { persist } from "zustand/middleware"

export type AuthUser = {
  id: number
  nickname: string
  googleEmail: string
  avatar?: string
  status: number
}

interface AuthState {
  user: AuthUser | null
  accessToken: string | null
  refreshToken: string | null
  isAuthenticated: boolean
  setSession: (payload: { user: AuthUser; accessToken: string; refreshToken: string }) => void
  setTokens: (payload: { accessToken: string; refreshToken?: string | null }) => void
  clearSession: () => void
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      user: null,
      accessToken: null,
      refreshToken: null,
      isAuthenticated: false,
      setSession: ({ user, accessToken, refreshToken }) =>
        set({ user, accessToken, refreshToken, isAuthenticated: true }),
      setTokens: ({ accessToken, refreshToken }) =>
        set((state) => ({
          accessToken,
          refreshToken: refreshToken ?? state.refreshToken,
          isAuthenticated: Boolean(accessToken),
        })),
      clearSession: () =>
        set({ user: null, accessToken: null, refreshToken: null, isAuthenticated: false }),
    }),
    {
      name: "auth-storage",
      partialize: (state) => ({
        accessToken: state.accessToken,
        refreshToken: state.refreshToken,
        user: state.user,
        isAuthenticated: state.isAuthenticated,
      }),
    },
  ),
)

/** @deprecated 兼容旧代码 */
export function getLegacyToken(state: AuthState): string | null {
  return state.accessToken
}
