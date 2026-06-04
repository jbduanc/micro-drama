import { resolveApiBase } from "@/config/apiBase";

export const API_BASE = {
  user: resolveApiBase(process.env.NEXT_PUBLIC_USER_API_BASE, "/user-api"),
  content: resolveApiBase(
    process.env.NEXT_PUBLIC_CONTENT_API_BASE,
    "/content-api",
  ),
  video: resolveApiBase(process.env.NEXT_PUBLIC_VIDEO_API_BASE, "/video-api"),
  payment: resolveApiBase(
    process.env.NEXT_PUBLIC_PAYMENT_API_BASE,
    "/payment-api",
  ),
};

type RequestOptions = {
  method?: string;
  body?: unknown;
  headers?: Record<string, string>;
  cache?: RequestCache;
  next?: NextFetchRequestConfig;
};

export class ApiError extends Error {
  status: number;

  constructor(message: string, status: number) {
    super(message);
    this.status = status;
  }
}

export async function apiFetch<T>(
  base: string,
  path: string,
  options: RequestOptions = {},
): Promise<T> {
  const { method = "GET", body, headers = {}, cache, next } = options;

  const res = await fetch(`${base}${path}`, {
    method,
    headers: {
      "Content-Type": "application/json",
      ...headers,
    },
    body: body != null ? JSON.stringify(body) : undefined,
    cache,
    next,
  });

  if (!res.ok) {
    const text = await res.text().catch(() => "");
    throw new ApiError(text || res.statusText, res.status);
  }

  return res.json() as Promise<T>;
}
