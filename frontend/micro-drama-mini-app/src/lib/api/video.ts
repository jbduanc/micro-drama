import { API_BASE, apiFetch } from "@/lib/api/client";
import { authHeaders } from "@/lib/auth/token";
import type { ApiResult, PlayAuthResponse } from "@/types";

export async function fetchPlayUrl(params: {
  videoId: string;
  orderId?: string;
  userId?: string;
}): Promise<PlayAuthResponse | null> {
  const query = new URLSearchParams({ videoId: params.videoId });
  if (params.orderId) query.set("orderId", params.orderId);

  const headers: Record<string, string> = { ...authHeaders() };
  if (params.userId) headers["X-User-Id"] = params.userId;

  const res = await apiFetch<ApiResult<PlayAuthResponse>>(
    API_BASE.video,
    `/v1/video/play?${query.toString()}`,
    { headers, cache: "no-store" },
  );

  return res.data ?? null;
}
