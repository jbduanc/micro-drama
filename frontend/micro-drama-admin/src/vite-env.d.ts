/// <reference types="vite/client" />

interface ImportMetaEnv {
  /** Vite 开发代理：micro-drama-admin 后端地址 */
  readonly VITE_ADMIN_API_TARGET?: string
  /** 生产/预览 axios baseURL；未设置时 dev 用 `/admin-api`，build 用 https://api.dramadjbo.com/admin-api */
  readonly VITE_ADMIN_API_BASE?: string
  /** Vite 开发代理：micro-drama-content 后端地址（短剧 HTTP） */
  readonly VITE_CONTENT_API_TARGET?: string
  /** 生产/预览 axios baseURL；未设置时 dev 用 `/content-api`，build 用 https://api.dramadjbo.com/content-api */
  readonly VITE_CONTENT_API_BASE?: string
  readonly VITE_VIDEO_API_BASE?: string
  readonly VITE_VIDEO_API_TARGET?: string
}

// 让 TypeScript 识别 CSS 文件
declare module '*.css' {
  const content: string
  export default content
}
