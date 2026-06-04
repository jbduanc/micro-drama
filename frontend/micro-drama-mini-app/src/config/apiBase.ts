/** 生产环境 API 网关域名（Kong / 统一入口） */
export const PROD_API_ORIGIN = "https://api.dramadjbo.com";

/**
 * 解析浏览器侧 fetch baseURL：
 * - NEXT_PUBLIC_*_API_BASE：浏览器请求的 URL（.env.production）
 * - dev 未设置时走 Next rewrites 相对路径 /content-api 等
 */
export function resolveApiBase(
  envValue: string | undefined,
  pathPrefix: "/content-api" | "/video-api" | "/payment-api",
): string {
  if (envValue != null && String(envValue).trim() !== "") {
    return String(envValue).trim();
  }
  if (process.env.NODE_ENV === "development") {
    return pathPrefix;
  }
  return `${PROD_API_ORIGIN}${pathPrefix}`;
}
