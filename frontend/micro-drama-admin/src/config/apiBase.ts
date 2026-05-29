/** 生产环境 API 网关域名（Kong / 统一入口） */
export const PROD_API_ORIGIN = "https://api.dramadjbo.com"

/**
 * 解析浏览器侧 axios baseURL（与 vite.config 代理是两套配置）：
 * - VITE_*_API_BASE：浏览器请求的 URL（.env.production）
 * - VITE_*_API_TARGET：仅 pnpm dev 时 Vite 把相对路径转发到的后端（.env.development）
 */
export function resolveApiBase(
  envValue: string | undefined,
  pathPrefix: "/admin-api" | "/content-api" | "/video-api",
): string {
  if (envValue != null && String(envValue).trim() !== "") {
    return String(envValue).trim()
  }
  if (import.meta.env.DEV) {
    return pathPrefix
  }
  return `${PROD_API_ORIGIN}${pathPrefix}`
}
