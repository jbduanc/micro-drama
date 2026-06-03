"use client";

import { useEffect, useRef, useState } from "react";
import { ChevronUp, Lock } from "lucide-react";
import type { DramaEpisode, MicroDrama } from "@/types";
import { fetchPlayUrl } from "@/lib/api/video";
import { appendWatchRecord } from "@/lib/api/user";
import { useAuthStore } from "@/lib/stores/auth-store";
import { UnlockSheet } from "@/components/payment/UnlockSheet";
import { VideoPlayer } from "@/components/player/VideoPlayer";

type FeedItem = {
  drama: MicroDrama;
  episode: DramaEpisode;
};

type Props = {
  items: FeedItem[];
};

export function SwipePlayer({ items }: Props) {
  const [index, setIndex] = useState(0);
  const [playUrl, setPlayUrl] = useState<string | null>(null);
  const [unlockTarget, setUnlockTarget] = useState<FeedItem | null>(null);
  const containerRef = useRef<HTMLDivElement>(null);
  const touchStartY = useRef(0);

  const profile = useAuthStore((s) => s.profile);
  const isUnlocked = useAuthStore((s) => s.isUnlocked);
  const unlockedEpisodeIds = useAuthStore((s) => s.unlockedEpisodeIds);
  const addWatch = useAuthStore((s) => s.addWatch);

  const current = items[index];

  useEffect(() => {
    if (!current) return;

    let cancelled = false;

    async function resolvePlayUrl() {
      if (!isUnlocked(current.episode.id, current.episode.price)) {
        if (!cancelled) setPlayUrl(null);
        return;
      }
      if (!current.episode.videoAssetId) {
        if (!cancelled) setPlayUrl(null);
        return;
      }
      try {
        const auth = await fetchPlayUrl({
          videoId: current.episode.videoAssetId,
          userId: profile.id,
        });
        if (!cancelled) setPlayUrl(auth?.playUrl ?? null);
      } catch {
        if (!cancelled) setPlayUrl(null);
      }
    }

    void resolvePlayUrl();
    return () => {
      cancelled = true;
    };
  }, [current, isUnlocked, profile.id, unlockedEpisodeIds]);

  const goNext = () => {
    if (index < items.length - 1) setIndex((i) => i + 1);
  };

  const goPrev = () => {
    if (index > 0) setIndex((i) => i - 1);
  };

  const onTouchStart = (e: React.TouchEvent) => {
    touchStartY.current = e.touches[0]?.clientY ?? 0;
  };

  const onTouchEnd = (e: React.TouchEvent) => {
    const delta = (e.changedTouches[0]?.clientY ?? 0) - touchStartY.current;
    if (delta < -50) goNext();
    if (delta > 50) goPrev();
  };

  const recordWatch = (item: FeedItem) => {
    if (!item.drama.id || !item.episode.id) return;
    const record = {
      id: crypto.randomUUID(),
      dramaId: item.drama.id,
      dramaTitle: item.drama.title ?? "短剧",
      episodeId: item.episode.id,
      episodeTitle: item.episode.title,
      episodeNum: item.episode.episodeNum,
      coverUrl: item.drama.coverUrl,
      watchedAt: new Date().toISOString(),
    };
    appendWatchRecord(record);
    addWatch(record);
  };

  if (!current) {
    return (
      <div className="flex h-[calc(100dvh-5rem)] items-center justify-center text-zinc-400">
        暂无可播放内容，请先在剧集中解锁剧集
      </div>
    );
  }

  const locked = !isUnlocked(current.episode.id, current.episode.price);

  return (
    <>
      <div
        ref={containerRef}
        className="relative h-[calc(100dvh-5rem)] overflow-hidden bg-black"
        onTouchStart={onTouchStart}
        onTouchEnd={onTouchEnd}
      >
        <VideoPlayer
          src={playUrl}
          poster={current.drama.coverUrl}
          autoPlay={!locked}
          onEnded={() => {
            recordWatch(current);
            goNext();
          }}
        />

        <div className="pointer-events-none absolute inset-x-0 bottom-0 bg-gradient-to-t from-black/80 to-transparent p-4 pb-8">
          <p className="text-sm text-amber-300">{current.drama.title}</p>
          <p className="text-lg font-semibold">
            第 {current.episode.episodeNum} 集 · {current.episode.title}
          </p>
          <p className="mt-1 text-xs text-zinc-400">
            {index + 1}/{items.length} · 上滑下一集
          </p>
        </div>

        {locked && (
          <div className="absolute inset-0 flex flex-col items-center justify-center gap-3 bg-black/70 px-6 text-center">
            <Lock className="h-10 w-10 text-amber-400" />
            <p className="text-lg font-medium">本集尚未解锁</p>
            <button
              type="button"
              onClick={() => setUnlockTarget(current)}
              className="rounded-full bg-amber-400 px-5 py-2 text-sm font-semibold text-black"
            >
              立即解锁
            </button>
          </div>
        )}

        {index < items.length - 1 && !locked && (
          <div className="absolute right-4 top-1/2 -translate-y-1/2 text-zinc-500">
            <ChevronUp className="h-6 w-6 rotate-180 animate-bounce" />
          </div>
        )}
      </div>

      {unlockTarget && (
        <UnlockSheet
          open
          dramaTitle={unlockTarget.drama.title ?? "短剧"}
          episode={unlockTarget.episode}
          onClose={() => setUnlockTarget(null)}
          onUnlocked={() => {
            setUnlockTarget(null);
            setPlayUrl(null);
          }}
        />
      )}
    </>
  );
}
