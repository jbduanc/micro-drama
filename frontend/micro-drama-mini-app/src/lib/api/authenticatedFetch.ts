import { API_BASE } from "@/lib/api/client";
import {
  authHeaders,
  clearAccessToken,
  getRefreshToken,
  setTokens,
} from "@/lib/auth/token";
import type { ApiResult } from "@/types";

type RequestOptions = {
  method?: string;
  body?: unknown;
  headers?: Record<string, string>;
  cache?: RequestCache;
  next?: NextFetchRequestConfig;
};

let refreshPromise: Promise<boolean> | null = null;

async function refreshUserSession(): Promise<boolean> {
  if (!refreshPromise) {
    refreshPromise = (async () => {
      const refreshToken = getRefreshToken();
      if (!refreshToken) return false;
      try {
        const res = await fetch(`${API_BASE.user}/auth/refresh`, {
          method: "POST",
          credentials: "include",
          headers: {
            "Content-Type": "application/json",
            "X-Refresh-Token": refreshToken,
          },
          body: JSON.stringify({ refreshToken }),
        });
        const json = (await res.json()) as ApiResult<{
          accessToken: string;
          refreshToken: string;
        }>;
        if (!res.ok || (json.code != null && json.code !== 0 && json.code !== 200)) {
          return false;
        }
        if (json.data?.accessToken) {
          setTokens(json.data.accessToken, json.data.refreshToken);
          return true;
        }
        return false;
      } catch {
        return false;
      } finally {
        refreshPromise = null;
      }
    })();
  }
  return refreshPromise;
}

export async function authenticatedFetch(
  base: string,
  path: string,
  options: RequestOptions = {},
  retried = false,
): Promise<Response> {
  const { method = "GET", body, headers = {}, cache, next } = options;

  const res = await fetch(`${base}${path}`, {
    method,
    credentials: "include",
    headers: {
      "Content-Type": "application/json",
      ...authHeaders(),
      ...headers,
    },
    body: body != null ? JSON.stringify(body) : undefined,
    cache,
    next,
  });

  const refreshed = res.headers.get("x-access-token-refreshed");
  if (refreshed === "true") {
    const auth = res.headers.get("authorization");
    if (auth?.startsWith("Bearer ")) {
      setTokens(auth.slice(7), getRefreshToken());
    }
  }

  if (res.status === 401 && !retried) {
    const ok = await refreshUserSession();
    if (ok) {
      return authenticatedFetch(base, path, options, true);
    }
    clearAccessToken();
  }

  return res;
}
