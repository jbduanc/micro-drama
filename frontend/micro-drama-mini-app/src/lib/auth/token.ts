const TOKEN_KEY = "micro_drama_access_token";

export function getAccessToken(): string | null {
  if (typeof window === "undefined") {
    return process.env.NEXT_PUBLIC_DEV_JWT_TOKEN || null;
  }
  return (
    localStorage.getItem(TOKEN_KEY) ||
    process.env.NEXT_PUBLIC_DEV_JWT_TOKEN ||
    null
  );
}

export function setAccessToken(token: string): void {
  if (typeof window === "undefined") return;
  localStorage.setItem(TOKEN_KEY, token);
}

export function clearAccessToken(): void {
  if (typeof window === "undefined") return;
  localStorage.removeItem(TOKEN_KEY);
}

export function authHeaders(): Record<string, string> {
  const token = getAccessToken();
  if (!token) return {};
  return { Authorization: `Bearer ${token}` };
}
