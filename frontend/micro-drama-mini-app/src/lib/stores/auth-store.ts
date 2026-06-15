"use client";

import { create } from "zustand";
import { fetchUserInfo, loginWithTelegram } from "@/lib/api/auth";
import {
  addBalance,
  deductBalance,
  getDefaultProfile,
  loadPaymentRecords,
  loadProfile,
  loadUnlockedEpisodeIds,
  loadWatchHistory,
  saveProfile,
  unlockEpisode,
} from "@/lib/api/user";
import { clearAccessToken, getAccessToken } from "@/lib/auth/token";
import { getInitData, initTelegramWebApp } from "@/lib/telegram/webapp";
import type { PaymentOrder, UserProfile, WatchRecord } from "@/types";

type AuthState = {
  profile: UserProfile;
  payments: PaymentOrder[];
  watchHistory: WatchRecord[];
  unlockedEpisodeIds: Set<string>;
  authReady: boolean;
  authError: string | null;
  hydrate: () => void;
  bootstrapAuth: () => Promise<void>;
  setProfile: (profile: UserProfile) => void;
  topUp: (amount: number) => void;
  markEpisodeUnlocked: (episodeId: string) => void;
  addPayment: (order: PaymentOrder) => void;
  addWatch: (record: WatchRecord) => void;
  tryDeductBalance: (amount: number) => boolean;
  isUnlocked: (episodeId?: string, price?: number | null) => boolean;
};

export const useAuthStore = create<AuthState>((set, get) => ({
  profile: getDefaultProfile(),
  payments: [],
  watchHistory: [],
  unlockedEpisodeIds: new Set(),
  authReady: false,
  authError: null,

  hydrate: () => {
    set({
      profile: loadProfile(),
      payments: loadPaymentRecords(),
      watchHistory: loadWatchHistory(),
      unlockedEpisodeIds: new Set(loadUnlockedEpisodeIds()),
    });
  },

  bootstrapAuth: async () => {
    get().hydrate();
    initTelegramWebApp();

    const initData = getInitData();
    if (initData) {
      try {
        const profile = await loginWithTelegram(initData);
        saveProfile(profile);
        set({ profile, authReady: true, authError: null });
        return;
      } catch (e) {
        clearAccessToken();
        set({
          authReady: true,
          authError: e instanceof Error ? e.message : "Telegram 登录失败",
        });
        return;
      }
    }

    if (getAccessToken()) {
      try {
        const profile = await fetchUserInfo();
        saveProfile(profile);
        set({ profile, authReady: true, authError: null });
        return;
      } catch {
        clearAccessToken();
      }
    }

    set({ authReady: true, authError: null });
  },

  setProfile: (profile) => {
    saveProfile(profile);
    set({ profile });
  },

  topUp: (amount) => {
    addBalance(amount);
    set({ profile: loadProfile() });
  },

  markEpisodeUnlocked: (episodeId) => {
    unlockEpisode(episodeId);
    set({ unlockedEpisodeIds: new Set(loadUnlockedEpisodeIds()) });
  },

  addPayment: (order) => {
    const payments = [order, ...get().payments];
    set({ payments });
  },

  addWatch: (record) => {
    const watchHistory = [
      record,
      ...get().watchHistory.filter((r) => r.episodeId !== record.episodeId),
    ].slice(0, 100);
    set({ watchHistory });
  },

  tryDeductBalance: (amount) => {
    const ok = deductBalance(amount);
    if (ok) set({ profile: loadProfile() });
    return ok;
  },

  isUnlocked: (episodeId, price) => {
    if (!episodeId) return false;
    if (!price || price <= 0) return true;
    return get().unlockedEpisodeIds.has(episodeId);
  },
}));
