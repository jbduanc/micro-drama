"use client";

import Image from "next/image";
import Link from "next/link";
import { ChevronRight, History, Receipt, Wallet } from "lucide-react";
import { useAuthStore } from "@/lib/stores/auth-store";

export function ProfileHeader() {
  const profile = useAuthStore((s) => s.profile);
  const topUp = useAuthStore((s) => s.topUp);

  return (
    <section className="rounded-2xl border border-white/5 bg-gradient-to-br from-amber-400/10 to-transparent p-5">
      <div className="flex items-center gap-4">
        <div className="relative h-16 w-16 overflow-hidden rounded-full bg-zinc-800">
          {profile.avatarUrl ? (
            <Image
              src={profile.avatarUrl}
              alt={profile.nickname}
              fill
              className="object-cover"
            />
          ) : (
            <div className="flex h-full items-center justify-center text-xl font-semibold text-amber-300">
              {profile.nickname.slice(0, 1).toUpperCase()}
            </div>
          )}
        </div>
        <div>
          <h2 className="text-xl font-semibold">{profile.nickname}</h2>
          <p className="text-sm text-zinc-400">ID: {profile.id}</p>
        </div>
      </div>

      <div className="mt-5 flex items-center justify-between rounded-xl bg-black/20 px-4 py-3">
        <div className="flex items-center gap-2">
          <Wallet className="h-4 w-4 text-amber-400" />
          <span className="text-sm text-zinc-400">账户余额</span>
        </div>
        <div className="flex items-center gap-3">
          <span className="text-lg font-bold text-amber-300">
            ${profile.balance.toFixed(2)}
          </span>
          <button
            type="button"
            onClick={() => topUp(10)}
            className="rounded-lg bg-amber-400 px-3 py-1 text-xs font-semibold text-black"
          >
            +10 测试充值
          </button>
        </div>
      </div>

      <div className="mt-4 divide-y divide-white/5 rounded-xl border border-white/5 bg-white/[0.02]">
        <Link
          href="/profile/payments"
          className="flex items-center justify-between px-4 py-3 text-sm hover:bg-white/[0.03]"
        >
          <span className="flex items-center gap-2">
            <Receipt className="h-4 w-4 text-zinc-400" />
            支付记录
          </span>
          <ChevronRight className="h-4 w-4 text-zinc-500" />
        </Link>
        <Link
          href="/profile/history"
          className="flex items-center justify-between px-4 py-3 text-sm hover:bg-white/[0.03]"
        >
          <span className="flex items-center gap-2">
            <History className="h-4 w-4 text-zinc-400" />
            观看记录
          </span>
          <ChevronRight className="h-4 w-4 text-zinc-500" />
        </Link>
      </div>
    </section>
  );
}
