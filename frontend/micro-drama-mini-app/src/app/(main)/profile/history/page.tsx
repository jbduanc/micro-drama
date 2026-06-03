"use client";

import Link from "next/link";
import Image from "next/image";
import { ArrowLeft, Play } from "lucide-react";
import { useAuthStore } from "@/lib/stores/auth-store";

export default function HistoryPage() {
  const watchHistory = useAuthStore((s) => s.watchHistory);

  return (
    <div className="px-4 pt-4">
      <Link
        href="/profile"
        className="mb-4 inline-flex items-center gap-1 text-sm text-zinc-400 hover:text-zinc-200"
      >
        <ArrowLeft className="h-4 w-4" />
        返回个人中心
      </Link>
      <h1 className="text-xl font-bold">观看记录</h1>

      <ul className="mt-4 space-y-3">
        {watchHistory.length === 0 && (
          <li className="py-12 text-center text-sm text-zinc-500">暂无观看记录</li>
        )}
        {watchHistory.map((item) => (
          <li key={item.id}>
            <Link
              href={`/dramas/${item.dramaId}`}
              className="flex items-center gap-3 rounded-xl border border-white/5 bg-white/[0.03] p-3"
            >
              <div className="relative h-14 w-10 shrink-0 overflow-hidden rounded-lg bg-zinc-800">
                {item.coverUrl ? (
                  <Image
                    src={item.coverUrl}
                    alt={item.dramaTitle}
                    fill
                    className="object-cover"
                    sizes="40px"
                  />
                ) : (
                  <div className="flex h-full items-center justify-center">
                    <Play className="h-4 w-4 text-zinc-500" />
                  </div>
                )}
              </div>
              <div className="min-w-0 flex-1">
                <p className="truncate font-medium">{item.dramaTitle}</p>
                <p className="truncate text-sm text-zinc-400">
                  第 {item.episodeNum} 集 · {item.episodeTitle}
                </p>
                <p className="text-xs text-zinc-600">
                  {new Date(item.watchedAt).toLocaleString()}
                </p>
              </div>
            </Link>
          </li>
        ))}
      </ul>
    </div>
  );
}
