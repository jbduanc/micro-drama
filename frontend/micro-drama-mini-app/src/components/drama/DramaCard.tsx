import Image from "next/image";
import Link from "next/link";
import type { MicroDrama } from "@/types";
import { formatPrice } from "@/lib/utils";

export function DramaCard({ drama }: { drama: MicroDrama }) {
  const href = `/dramas/${drama.id}`;

  return (
    <Link
      href={href}
      className="group flex gap-3 rounded-2xl border border-white/5 bg-white/[0.03] p-3 transition hover:border-amber-400/30 hover:bg-white/[0.06]"
    >
      <div className="relative h-24 w-[4.5rem] shrink-0 overflow-hidden rounded-xl bg-zinc-800">
        {drama.coverUrl ? (
          <Image
            src={drama.coverUrl}
            alt={drama.title ?? "短剧封面"}
            fill
            className="object-cover"
            sizes="72px"
          />
        ) : (
          <div className="flex h-full items-center justify-center text-xs text-zinc-500">
            暂无封面
          </div>
        )}
      </div>
      <div className="min-w-0 flex-1">
        <h3 className="truncate text-base font-medium text-zinc-100 group-hover:text-amber-300">
          {drama.title ?? "未命名短剧"}
        </h3>
        <p className="mt-1 line-clamp-2 text-sm text-zinc-400">
          {drama.description || "精彩短剧，点击观看"}
        </p>
        <div className="mt-2 flex items-center gap-3 text-xs text-zinc-500">
          <span>{drama.totalEpisodes ?? 0} 集</span>
          <span>{formatPrice(drama.price)}</span>
        </div>
      </div>
    </Link>
  );
}
