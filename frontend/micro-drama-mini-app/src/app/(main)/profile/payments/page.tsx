"use client";

import Link from "next/link";
import { ArrowLeft } from "lucide-react";
import { useAuthStore } from "@/lib/stores/auth-store";
import { formatPrice } from "@/lib/utils";

export default function PaymentsPage() {
  const payments = useAuthStore((s) => s.payments);

  return (
    <div className="px-4 pt-4">
      <Link
        href="/profile"
        className="mb-4 inline-flex items-center gap-1 text-sm text-zinc-400 hover:text-zinc-200"
      >
        <ArrowLeft className="h-4 w-4" />
        返回个人中心
      </Link>
      <h1 className="text-xl font-bold">支付记录</h1>

      <ul className="mt-4 space-y-3">
        {payments.length === 0 && (
          <li className="py-12 text-center text-sm text-zinc-500">暂无支付记录</li>
        )}
        {payments.map((item) => (
          <li
            key={item.id}
            className="rounded-xl border border-white/5 bg-white/[0.03] p-4"
          >
            <div className="flex items-start justify-between gap-3">
              <div>
                <p className="font-medium">{item.dramaTitle}</p>
                <p className="text-sm text-zinc-400">{item.episodeTitle}</p>
              </div>
              <span className="text-amber-300">{formatPrice(item.amount)}</span>
            </div>
            <div className="mt-2 flex items-center justify-between text-xs text-zinc-500">
              <span>{item.method?.toUpperCase() ?? "—"}</span>
              <span>{item.status}</span>
            </div>
            {item.createdAt && (
              <p className="mt-1 text-xs text-zinc-600">
                {new Date(item.createdAt).toLocaleString()}
              </p>
            )}
          </li>
        ))}
      </ul>
    </div>
  );
}
