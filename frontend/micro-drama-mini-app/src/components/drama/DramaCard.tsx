import Image from "next/image";
import Link from "next/link";
import type { MicroDrama } from "@/types";

export function DramaCard({ drama }: { drama: MicroDrama }) {
  const href = `/dramas/${drama.id}`;

  return (
    <Link
      href={href}
      className="group block overflow-hidden rounded-xl transition hover:opacity-90"
    >
      <div className="relative aspect-[3/4] overflow-hidden rounded-xl bg-zinc-800 ring-1 ring-white/5 transition group-hover:ring-amber-400/30">
        {drama.coverUrl ? (
          <Image
            src={drama.coverUrl}
            alt={drama.title ?? "短剧封面"}
            fill
            className="object-cover"
            sizes="(max-width: 512px) 33vw, 160px"
          />
        ) : (
          <div className="flex h-full items-center justify-center text-xs text-zinc-500">
            暂无封面
          </div>
        )}
      </div>
      <h3 className="mt-2 truncate px-0.5 text-sm font-medium text-zinc-100 group-hover:text-amber-300">
        {drama.title ?? "未命名短剧"}
      </h3>
    </Link>
  );
}
