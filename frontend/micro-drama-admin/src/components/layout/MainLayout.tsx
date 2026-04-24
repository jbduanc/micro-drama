import { useEffect } from 'react'
import { Outlet, useLocation, useNavigate } from 'react-router-dom'
import { SidebarProvider, SidebarInset } from '@/components/ui/sidebar'
import { AppSidebar } from './AppSidebar'
import { Header } from './Header'
import { useTabStore } from '@/stores/tab-store'
import { X } from 'lucide-react'

const routeMap = {
  '/users': { key: 'users', title: '用户管理' },
  '/dramas': { key: 'dramas', title: '短剧管理' },
  '/plans': { key: 'plans', title: '会员套餐管理' },
  '/admins': { key: 'admins', title: '管理员管理' },
}

export function MainLayout() {
  const navigate = useNavigate()
  const location = useLocation()
  const { tabs, activeKey, openTab, closeTab, switchTab } = useTabStore()

  useEffect(() => {
    const path = location.pathname
    const route = routeMap[path as keyof typeof routeMap]
    if (route) {
      openTab({ ...route, path })
    }
  }, [location.pathname, openTab])

  return (
    <SidebarProvider>
      <div className="flex h-screen min-h-0 w-full">
        {/* 左侧侧边栏 */}
        <AppSidebar />

        {/* 右侧主内容区 */}
        <SidebarInset className="flex h-full min-w-0 flex-col overflow-hidden bg-background">
          <Header />
          {/* Tab 栏 */}
          <div className="flex items-center gap-1 border-b bg-muted/30 px-4 py-2 overflow-x-auto">
            {tabs.map((tab) => (
              <div
                key={tab.key}
                onClick={() => {
                  switchTab(tab.key)
                  navigate(tab.path)
                }}
                className={`
                  flex items-center gap-2 px-3 py-1.5 text-sm rounded-md cursor-pointer transition-colors
                  hover:bg-muted
                  ${activeKey === tab.key ? 'bg-muted font-medium' : 'text-muted-foreground'}
                `}
              >
                {tab.title}
                <button
                  onClick={(e) => {
                    e.stopPropagation()
                    closeTab(tab.key)
                  }}
                  className="h-4 w-4 rounded-full hover:bg-muted-foreground/20 flex items-center justify-center"
                >
                  <X className="h-3 w-3" />
                </button>
              </div>
            ))}
          </div>

          {/* 页面内容区（SidebarInset 已为 main，此处用 div 避免嵌套 main） */}
          <div className="flex-1 overflow-auto p-6">
            <Outlet />
          </div>
        </SidebarInset>
      </div>
    </SidebarProvider>
  )
}
