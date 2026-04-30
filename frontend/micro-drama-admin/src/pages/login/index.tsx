import { toast } from 'sonner'

import { authService } from '@/api/auth/service'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'

export default function LoginPage() {
  async function handleGoogleLogin() {
    try {
      const redirectUri = new URL('/oauth/callback', window.location.origin).toString()
      const url = await authService.getAuthorizeUrl(redirectUri)
      if (typeof url !== 'string' || !/^https?:\/\//i.test(url)) {
        console.error('Invalid authorize url:', url)
        toast.error('授权地址不合法，请联系管理员')
        return
      }
      window.location.href = url
    } catch (e) {
      console.error(e)
      toast.error('获取 Google 授权地址失败')
    }
  }

  return (
    <div className="flex min-h-svh items-center justify-center bg-background p-6">
      <Card className="w-full max-w-md">
        <CardHeader>
          <CardTitle>登录</CardTitle>
          <CardDescription>使用 Google 账号登录后台管理系统</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <Button className="w-full" onClick={handleGoogleLogin}>
            使用 Google 登录
          </Button>
          <div className="text-xs text-muted-foreground">
            登录即代表你同意系统的安全策略与访问控制。
          </div>
        </CardContent>
      </Card>
    </div>
  )
}
