"use client";

import { useEffect } from "react";
import { initTelegramWebApp, isTelegramEnv, getTelegramUser } from "@/lib/telegram/webapp";
import { useAuthStore } from "@/lib/stores/auth-store";

export function AppProviders({ children }: { children: React.ReactNode }) {
  const hydrate = useAuthStore((s) => s.hydrate);
  const setProfile = useAuthStore((s) => s.setProfile);

  useEffect(() => {
    hydrate();
    initTelegramWebApp();

    const tgUser = getTelegramUser();
    if (tgUser) {
      setProfile({
        id: String(tgUser.id),
        nickname: tgUser.username || tgUser.first_name,
        avatarUrl: tgUser.photo_url,
        balance: useAuthStore.getState().profile.balance,
        telegramId: String(tgUser.id),
      });
    }
  }, [hydrate, setProfile]);

  useEffect(() => {
    if (isTelegramEnv()) {
      document.documentElement.classList.add("telegram-mini-app");
    }
  }, []);

  return children;
}
