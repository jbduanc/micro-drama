import type { PaymentOrder, UserProfile, WatchRecord } from "@/types";

const PROFILE_KEY = "micro_drama_profile";
const PAYMENTS_KEY = "micro_drama_payments";
const HISTORY_KEY = "micro_drama_watch_history";
const UNLOCKS_KEY = "micro_drama_unlocks";

function readJson<T>(key: string, fallback: T): T {
  if (typeof window === "undefined") return fallback;
  try {
    const raw = localStorage.getItem(key);
    return raw ? (JSON.parse(raw) as T) : fallback;
  } catch {
    return fallback;
  }
}

function writeJson<T>(key: string, value: T): void {
  if (typeof window === "undefined") return;
  localStorage.setItem(key, JSON.stringify(value));
}

export function getDefaultProfile(): UserProfile {
  return {
    id: "guest",
    nickname: "游客",
    balance: 0,
  };
}

export function loadProfile(): UserProfile {
  return readJson<UserProfile>(PROFILE_KEY, getDefaultProfile());
}

export function saveProfile(profile: UserProfile): void {
  writeJson(PROFILE_KEY, profile);
}

export function loadPaymentRecords(): PaymentOrder[] {
  return readJson<PaymentOrder[]>(PAYMENTS_KEY, []);
}

export function appendPaymentRecord(order: PaymentOrder): void {
  const list = loadPaymentRecords();
  writeJson(PAYMENTS_KEY, [order, ...list]);
}

export function loadWatchHistory(): WatchRecord[] {
  return readJson<WatchRecord[]>(HISTORY_KEY, []);
}

export function appendWatchRecord(record: WatchRecord): void {
  const list = loadWatchHistory().filter((r) => r.episodeId !== record.episodeId);
  writeJson(HISTORY_KEY, [record, ...list].slice(0, 100));
}

export function loadUnlockedEpisodeIds(): string[] {
  return readJson<string[]>(UNLOCKS_KEY, []);
}

export function unlockEpisode(episodeId: string): void {
  const ids = new Set(loadUnlockedEpisodeIds());
  ids.add(episodeId);
  writeJson(UNLOCKS_KEY, Array.from(ids));
}

export function isEpisodeUnlocked(
  episodeId: string | undefined,
  price?: number | null,
): boolean {
  if (!episodeId) return false;
  if (!price || price <= 0) return true;
  return loadUnlockedEpisodeIds().includes(episodeId);
}

export function deductBalance(amount: number): boolean {
  const profile = loadProfile();
  if (profile.balance < amount) return false;
  saveProfile({ ...profile, balance: profile.balance - amount });
  return true;
}

export function addBalance(amount: number): void {
  const profile = loadProfile();
  saveProfile({ ...profile, balance: profile.balance + amount });
}
