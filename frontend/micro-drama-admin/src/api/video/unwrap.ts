import type { PlayAuthData, Result } from "./types"

/** 兼容 Go（code=0）与 Java（code=200）统一响应 */
export function unwrapApiResult<T>(body: Result<T> | T | null | undefined): T {
  if (body == null) {
    throw new Error("empty response")
  }
  if (typeof body !== "object") {
    throw new Error("invalid response")
  }
  if ("data" in body && "code" in body) {
    const r = body as Result<T>
    const code = r.code
    if (code != null && code !== 0 && code !== 200) {
      throw new Error(r.msg || r.message || `request failed (${code})`)
    }
    if (r.data == null) {
      throw new Error(r.msg || r.message || "empty data")
    }
    return r.data
  }
  return body as T
}

export function normalizePlayAuthData(raw: unknown): PlayAuthData {
  if (raw && typeof raw === "object" && "data" in raw) {
    const nested = (raw as Result<unknown>).data
    if (nested && typeof nested === "object" && !("playUrl" in raw) && !("play_url" in raw)) {
      return normalizePlayAuthData(nested)
    }
  }
  if (!raw || typeof raw !== "object") {
    throw new Error("invalid play response")
  }
  const o = raw as Record<string, unknown>
  return {
    videoId: String(o.videoId ?? o.video_id ?? ""),
    playUrl: String(o.playUrl ?? o.play_url ?? ""),
    token: String(o.token ?? ""),
    expiresIn: Number(o.expiresIn ?? o.expires_in ?? 0),
    status: String(o.status ?? ""),
    hlsPath: String(o.hlsPath ?? o.hls_path ?? ""),
  }
}
