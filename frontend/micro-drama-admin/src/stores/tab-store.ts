import { create } from 'zustand'
import { devtools } from 'zustand/middleware'

// TypeScript 类型定义（行业标准）
export interface TabItem {
  key: string
  title: string
  path: string
}

interface TabStore {
  tabs: TabItem[]
  activeKey: string
  openTab: (tab: TabItem) => void
  closeTab: (key: string) => void
  switchTab: (key: string) => void
}

// 🔥 Zustand + DevTools（2026 React 状态管理默认方案）
export const useTabStore = create<TabStore>()(
  devtools((set) => ({
    tabs: [],
    activeKey: '',

    // 打开/激活 Tab
    openTab: (tab) =>
      set((state) => {
        const exist = state.tabs.find((t) => t.key === tab.key)
        if (exist) return { activeKey: tab.key }
        return { tabs: [...state.tabs, tab], activeKey: tab.key }
      }),

    // 关闭 Tab
    closeTab: (key) =>
      set((state) => {
        if (state.tabs.length <= 1) return state
        const newTabs = state.tabs.filter((t) => t.key !== key)
        const newActive = state.activeKey === key ? newTabs.at(-1)!.key : state.activeKey
        return { tabs: newTabs, activeKey: newActive }
      }),

    // 切换 Tab
    switchTab: (key) => set({ activeKey: key }),
  }))
)
