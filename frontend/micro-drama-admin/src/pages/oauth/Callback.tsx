import { useEffect } from "react"
import { useNavigate, useSearchParams } from "react-router-dom"
import { toast } from "sonner"

import { authService } from "@/api/auth/service"
import { useAuthStore } from "@/stores/useAuthStore"

export default function OAuthCallbackPage() {
  const [searchParams] = useSearchParams()
  const navigate = useNavigate()
  const setSession = useAuthStore((s) => s.setSession)

  useEffect(() => {
    const run = async () => {
      const code = searchParams.get("code")
      if (!code) {
        toast.error("缺少授权码 code")
        navigate("/login", { replace: true })
        return
      }

      try {
        const token = await authService.loginWithGoogleCode(code)
        // set token early so subsequent calls include Authorization header
        useAuthStore.getState().setToken(token)
        const user = await authService.getUserInfo()
        setSession({ token, user })
        toast.success("登录成功")
        navigate("/users", { replace: true })
      } catch (e) {
        console.error(e)
        useAuthStore.getState().clearSession()
        toast.error("登录失败，请重试")
        navigate("/login", { replace: true })
      }
    }

    run()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  return (
    <div className="flex h-screen items-center justify-center bg-background">
      <div className="text-sm text-muted-foreground">登录处理中...</div>
    </div>
  )
}

