import { API_BASE } from "@/lib/api/client";
import { setAccessToken } from "@/lib/auth/token";
import type { ApiResult, UserProfile } from "@/types";

type LoginTokenPayload = {
  accessToken: string;
  user: UserProfile & { avatarUrl?: string; authProvider?: string };
};

async function postJson<T>(path: string, body?: unknown): Promise<T> {
  const res = await fetch(`${API_BASE.user}${path}`, {
    method: "POST",
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
  setAccessToken(data.accessToken);
  return mapUser(data.user);
}

export async function loginWithDevUser(payload?: {
  telegramId?: string;
  nickname?: string;
}): Promise<UserProfile> {
  const data = await postJson<LoginTokenPayload>("/auth/dev/init", payload);
  setAccessToken(data.accessToken);
  return mapUser(data.user);
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
