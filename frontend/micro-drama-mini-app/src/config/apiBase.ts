/** 生产环境 API 网关域名（Traefik 统一入口） */
export const PROD_API_ORIGIN = "https://api.dramadjbo.com";

const LOCAL_HOSTS = new Set(["localhost", "127.0.0.1"]);

function isLocalHostname(hostname: string): boolean {
  return LOCAL_HOSTS.has(hostname);
}

/** 将误配的 www 同源 API 纠正为 api 网关 */
function normalizeConfiguredBase(
  value: string,
  pathPrefix: "/content-api" | "/video-api" | "/payment-api" | "/user-api",
): string {
  const trimmed = value.trim();
  try {
    const url = new URL(trimmed, "https://placeholder.local");
    if (
      url.hostname === "www.dramadjbo.com" ||
      url.hostname === "dramadjbo.com"
    ) {
      return `${PROD_API_ORIGIN}${pathPrefix}`;
    }
  } catch {
    /* 相对路径等由后续逻辑处理 */
  }
  return trimmed;
}

function isLocalDevRuntime(): boolean {
  if (typeof window !== "undefined") {
    return isLocalHostname(window.location.hostname);
  }
  return process.env.NODE_ENV === "development";
}

/**
 * 解析浏览器侧 fetch baseURL：
 * - NEXT_PUBLIC_*_API_BASE：显式配置（自动纠正 www → api）
 * - 仅 localhost / 127.0.0.1 使用相对路径走 Next rewrites
 * - 其余（含 www.dramadjbo.com、生产构建）一律 api.dramadjbo.com
 */
export function resolveApiBase(
  envValue: string | undefined,
  pathPrefix: "/content-api" | "/video-api" | "/payment-api" | "/user-api",
): string {
  if (envValue != null && String(envValue).trim() !== "") {
    return normalizeConfiguredBase(String(envValue), pathPrefix);
  }
  if (isLocalDevRuntime()) {
    return pathPrefix;
  }
  return `${PROD_API_ORIGIN}${pathPrefix}`;
}
