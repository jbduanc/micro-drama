const ACCESS_KEY = "micro_drama_access_token";
const REFRESH_KEY = "micro_drama_refresh_token";

export function getAccessToken(): string | null {
  if (typeof window === "undefined") {
    return process.env.NEXT_PUBLIC_DEV_JWT_TOKEN || null;
  }
  return (
    localStorage.getItem(ACCESS_KEY) ||
    process.env.NEXT_PUBLIC_DEV_JWT_TOKEN ||
    null
  );
}

export function getRefreshToken(): string | null {
  if (typeof window === "undefined") return null;
  return localStorage.getItem(REFRESH_KEY);
}

export function setTokens(accessToken: string, refreshToken?: string | null): void {
  if (typeof window === "undefined") return;
  localStorage.setItem(ACCESS_KEY, accessToken);
  if (refreshToken) {
    localStorage.setItem(REFRESH_KEY, refreshToken);
  }
}

/** @deprecated 使用 setTokens */
export function setAccessToken(token: string): void {
  setTokens(token, getRefreshToken());
}

export function clearAccessToken(): void {
  if (typeof window === "undefined") return;
  localStorage.removeItem(ACCESS_KEY);
  localStorage.removeItem(REFRESH_KEY);
}

export function authHeaders(): Record<string, string> {
  const headers: Record<string, string> = {};
  const token = getAccessToken();
  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }
  const refresh = getRefreshToken();
  if (refresh) {
    headers["X-Refresh-Token"] = refresh;
  }
  return headers;
}
