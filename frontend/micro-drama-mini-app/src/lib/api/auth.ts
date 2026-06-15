import { API_BASE } from "@/lib/api/client";
import { authenticatedFetch } from "@/lib/api/authenticatedFetch";
import { setTokens } from "@/lib/auth/token";
import type { ApiResult, UserProfile } from "@/types";

type LoginTokenPayload = {
  accessToken: string;
  refreshToken?: string;
  user: UserProfile & { avatarUrl?: string; authProvider?: string };
};

async function postJson<T>(path: string, body?: unknown): Promise<T> {
  const res = await fetch(`${API_BASE.user}${path}`, {
    method: "POST",
    credentials: "include",
    headers: { "Content-Type": "application/json" },
    body: body != null ? JSON.stringify(body) : undefined,
  });
  const json = (await res.json()) as ApiResult<T>;
  if (json.code != null && json.code !== 0 && json.code !== 200) {
    throw new Error(json.msg || json.message || "登录失败");
  }
  return json.data;
}

export async function loginWithTelegram(initData: string): Promise<UserProfile> {
  const data = await postJson<LoginTokenPayload>("/auth/telegram", { initData });
  setTokens(data.accessToken, data.refreshToken);
  return mapUser(data.user);
}

export async function fetchUserInfo(): Promise<UserProfile> {
  const res = await authenticatedFetch(API_BASE.user, "/auth/user/info");
  const json = (await res.json()) as ApiResult<
    UserProfile & { avatarUrl?: string }
  >;
  if (!res.ok || (json.code != null && json.code !== 0 && json.code !== 200) || !json.data) {
    throw new Error(json.msg || json.message || "获取用户信息失败");
  }
  return mapUser(json.data);
}

function mapUser(u: LoginTokenPayload["user"]): UserProfile {
  return {
    id: u.id,
    nickname: u.nickname,
    avatarUrl: u.avatarUrl,
    balance: Number(u.balance ?? 0),
    telegramId: u.telegramId,
  };
}
