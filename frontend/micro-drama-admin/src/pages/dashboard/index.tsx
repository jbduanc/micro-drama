import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Film, Globe, Users } from 'lucide-react'

const stats = [
  {
    title: '短剧总数',
    value: '0',
    description: '已上架与草稿',
    icon: Film,
  },
  {
    title: '用户总数',
    value: '0',
    description: '注册用户',
    icon: Users,
  },
  {
    title: '今日访问',
    value: '0',
    description: '页面浏览量',
    icon: Globe,
  },
]

export default function Dashboard() {
  return (
    <div className="space-y-8">
      <div>
        <h1 className="text-2xl font-semibold tracking-tight text-foreground">仪表盘</h1>
        <p className="mt-1 text-sm text-muted-foreground">短剧后台数据总览</p>
      </div>

      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
        {stats.map((item) => (
          <Card key={item.title}>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium text-muted-foreground">{item.title}</CardTitle>
              <item.icon className="h-4 w-4 text-muted-foreground" aria-hidden />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold tracking-tight text-foreground">{item.value}</div>
              <CardDescription className="mt-1">{item.description}</CardDescription>
            </CardContent>
          </Card>
        ))}
      </div>
    </div>
  )
}
