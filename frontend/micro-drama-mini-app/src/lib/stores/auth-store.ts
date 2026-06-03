"use client";

import { create } from "zustand";
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
import type { PaymentOrder, UserProfile, WatchRecord } from "@/types";

type AuthState = {
  profile: UserProfile;
  payments: PaymentOrder[];
  watchHistory: WatchRecord[];
  unlockedEpisodeIds: Set<string>;
  hydrate: () => void;
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

  hydrate: () => {
    set({
      profile: loadProfile(),
      payments: loadPaymentRecords(),
      watchHistory: loadWatchHistory(),
      unlockedEpisodeIds: new Set(loadUnlockedEpisodeIds()),
    });
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
