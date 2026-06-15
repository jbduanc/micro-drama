"use client";

import { useEffect } from "react";
import { isTelegramEnv } from "@/lib/telegram/webapp";
import { useAuthStore } from "@/lib/stores/auth-store";

export function AppProviders({ children }: { children: React.ReactNode }) {
  const bootstrapAuth = useAuthStore((s) => s.bootstrapAuth);
  const authReady = useAuthStore((s) => s.authReady);
  const authError = useAuthStore((s) => s.authError);

  useEffect(() => {
    void bootstrapAuth();
  }, [bootstrapAuth]);

  useEffect(() => {
    if (isTelegramEnv()) {
      document.documentElement.classList.add("telegram-mini-app");
    }
  }, []);

  if (!authReady) {
    return (
      <div className="flex min-h-full items-center justify-center text-sm text-zinc-400">
        登录中...
      </div>
    );
  }

  if (authError) {
    return (
      <div className="flex min-h-full flex-col items-center justify-center gap-2 px-6 text-center">
        <p className="text-sm text-red-300">{authError}</p>
        <p className="text-xs text-zinc-500">
          请从 Telegram Bot 打开小程序，并确认服务端已配置 TELEGRAM_BOT_TOKEN
        </p>
      </div>
    );
  }

  return children;
}
