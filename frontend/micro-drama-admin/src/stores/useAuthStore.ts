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
  token: string | null
  isAuthenticated: boolean
  setSession: (payload: { user: AuthUser; token: string }) => void
  setToken: (token: string | null) => void
  clearSession: () => void
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      user: null,
      token: null,
      isAuthenticated: false,
      setSession: ({ user, token }) => set({ user, token, isAuthenticated: true }),
      setToken: (token) => set({ token, isAuthenticated: Boolean(token) }),
      clearSession: () => set({ user: null, token: null, isAuthenticated: false }),
    }),
    {
      name: "auth-storage",
      partialize: (state) => ({
        token: state.token,
        user: state.user,
        isAuthenticated: state.isAuthenticated,
      }),
    },
  ),
);
