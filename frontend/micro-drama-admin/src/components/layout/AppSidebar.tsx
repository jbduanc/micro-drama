import {
  Sidebar,
  SidebarContent,
  SidebarGroup,
  SidebarGroupLabel,
  SidebarMenu,
  SidebarMenuItem,
  SidebarMenuButton,
} from '@/components/ui/sidebar'
import { useNavigate } from 'react-router-dom'
import { useTabStore } from '@/stores/tab-store'
import { CreditCard, Film, Shield, Users } from 'lucide-react'

// 菜单配置（TS + Lucide 图标）
const menuItems = [
  { key: 'users', title: '用户管理', path: '/users', icon: Users },
  { key: 'dramas', title: '短剧管理', path: '/dramas', icon: Film },
  { key: 'plans', title: '会员套餐管理', path: '/plans', icon: CreditCard },
  { key: 'admins', title: '管理员管理', path: '/admins', icon: Shield },
]

export function AppSidebar() {
  const navigate = useNavigate()
  const { openTab } = useTabStore()

  // 菜单点击逻辑
  const handleClick = (item: (typeof menuItems)[0]) => {
    openTab({ key: item.key, title: item.title, path: item.path })
    navigate(item.path)
  }

  return (
    <Sidebar>
      <SidebarContent>
        <SidebarGroup>
          <SidebarGroupLabel>主菜单</SidebarGroupLabel>
          <SidebarMenu>
            {menuItems.map((item) => (
              <SidebarMenuItem key={item.key}>
                <SidebarMenuButton onClick={() => handleClick(item)}>
                  <item.icon className="h-4 w-4" />
                  <span>{item.title}</span>
                </SidebarMenuButton>
              </SidebarMenuItem>
            ))}
          </SidebarMenu>
        </SidebarGroup>
      </SidebarContent>
    </Sidebar>
  )
}
