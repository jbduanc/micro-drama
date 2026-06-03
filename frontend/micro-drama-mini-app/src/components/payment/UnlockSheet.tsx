"use client";

import { useState } from "react";
import { Loader2, Wallet, CreditCard } from "lucide-react";
import type { DramaEpisode } from "@/types";
import {
  createWeb2Payment,
  createWeb3Payment,
} from "@/lib/api/payment";
import { appendPaymentRecord } from "@/lib/api/user";
import { useAuthStore } from "@/lib/stores/auth-store";
import { formatPrice } from "@/lib/utils";

type Props = {
  open: boolean;
  dramaTitle: string;
  episode: DramaEpisode;
  onClose: () => void;
  onUnlocked: () => void;
};

export function UnlockSheet({
  open,
  dramaTitle,
  episode,
  onClose,
  onUnlocked,
}: Props) {
  const [loading, setLoading] = useState<"web2" | "web3" | "balance" | null>(
    null,
  );
  const profile = useAuthStore((s) => s.profile);
  const markEpisodeUnlocked = useAuthStore((s) => s.markEpisodeUnlocked);
  const tryDeductBalance = useAuthStore((s) => s.tryDeductBalance);
  const addPayment = useAuthStore((s) => s.addPayment);

  if (!open) return null;

  const amount = episode.price ?? 0;

  const finishUnlock = (method: "web2" | "web3" | "balance") => {
    if (!episode.id) return;
    markEpisodeUnlocked(episode.id);
    onUnlocked();
    void method;
  };

  const payWithBalance = async () => {
    if (!episode.id || amount <= 0) {
      finishUnlock("balance");
      return;
    }
    setLoading("balance");
    try {
      if (!tryDeductBalance(amount)) {
        alert("余额不足，请先充值或使用其他支付方式");
        return;
      }
      const order = {
        id: crypto.randomUUID(),
        status: "paid",
        amount,
        dramaTitle,
        episodeTitle: episode.title,
        method: "web2" as const,
        createdAt: new Date().toISOString(),
      };
      appendPaymentRecord(order);
      addPayment(order);
      finishUnlock("balance");
    } finally {
      setLoading(null);
    }
  };

  const payWeb2 = async () => {
    setLoading("web2");
    try {
      const order = await createWeb2Payment({
        episodeId: episode.id ?? "",
        dramaTitle,
        episodeTitle: episode.title,
        amount,
      });
      appendPaymentRecord({ ...order, status: "paid" });
      addPayment({ ...order, status: "paid" });
      finishUnlock("web2");
    } finally {
      setLoading(null);
    }
  };

  const payWeb3 = async () => {
    setLoading("web3");
    try {
      const order = await createWeb3Payment({
        episodeId: episode.id ?? "",
        dramaTitle,
        episodeTitle: episode.title,
        amount,
      });
      appendPaymentRecord(order);
      addPayment(order);
      finishUnlock("web3");
    } catch {
      alert("Web3 支付下单失败，请检查 payment 服务是否启动");
    } finally {
      setLoading(null);
    }
  };

  return (
    <div className="fixed inset-0 z-[100] flex items-end bg-black/60">
      <div className="w-full rounded-t-3xl bg-[#17171c] p-5 pb-[calc(1.25rem+env(safe-area-inset-bottom))]">
        <div className="mx-auto mb-4 h-1 w-10 rounded-full bg-zinc-600" />
        <h3 className="text-lg font-semibold">解锁剧集</h3>
        <p className="mt-1 text-sm text-zinc-400">
          {dramaTitle} · 第 {episode.episodeNum} 集 {episode.title}
        </p>
        <p className="mt-3 text-2xl font-bold text-amber-400">
          {formatPrice(amount)}
        </p>
        <p className="mt-1 text-xs text-zinc-500">
          账户余额：${profile.balance.toFixed(2)}
        </p>

        <div className="mt-5 space-y-3">
          <button
            type="button"
            disabled={loading != null}
            onClick={() => void payWithBalance()}
            className="flex w-full items-center justify-center gap-2 rounded-xl bg-amber-400 py-3 text-sm font-semibold text-black disabled:opacity-60"
          >
            {loading === "balance" ? (
              <Loader2 className="h-4 w-4 animate-spin" />
            ) : (
              <Wallet className="h-4 w-4" />
            )}
            余额支付
          </button>

          <button
            type="button"
            disabled={loading != null}
            onClick={() => void payWeb2()}
            className="flex w-full items-center justify-center gap-2 rounded-xl border border-white/10 py-3 text-sm font-medium disabled:opacity-60"
          >
            {loading === "web2" ? (
              <Loader2 className="h-4 w-4 animate-spin" />
            ) : (
              <CreditCard className="h-4 w-4" />
            )}
            Web2 支付（Stripe / 法币）
          </button>

          <button
            type="button"
            disabled={loading != null}
            onClick={() => void payWeb3()}
            className="flex w-full items-center justify-center gap-2 rounded-xl border border-amber-400/30 py-3 text-sm font-medium text-amber-300 disabled:opacity-60"
          >
            {loading === "web3" ? (
              <Loader2 className="h-4 w-4 animate-spin" />
            ) : (
              <Wallet className="h-4 w-4" />
            )}
            Web3 支付（链上）
          </button>
        </div>

        <button
          type="button"
          onClick={onClose}
          className="mt-4 w-full py-2 text-sm text-zinc-400"
        >
          取消
        </button>
      </div>
    </div>
  );
}
