/// <reference types="vite/client" />

interface ImportMetaEnv {
  /** Vite 开发代理：micro-drama-admin 后端地址 */
  readonly VITE_ADMIN_API_TARGET?: string
  /** Vite 开发代理：micro-drama-content 后端地址（短剧 HTTP） */
  readonly VITE_CONTENT_API_TARGET?: string
  /**
   * 生产构建时内容服务根路径或绝对 URL。
   * 未设置时默认 `/content-api`（由网关或同域反代转发到 content）。
   */
  readonly VITE_CONTENT_API_BASE?: string
}

// 让 TypeScript 识别 CSS 文件
declare module '*.css' {
  const content: string
  export default content
}
