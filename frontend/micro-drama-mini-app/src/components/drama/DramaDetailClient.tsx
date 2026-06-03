"use client";

import { useRouter } from "next/navigation";
import { useState } from "react";
import Image from "next/image";
import type { DramaEpisode, MicroDrama } from "@/types";
import { EpisodeList } from "@/components/drama/EpisodeList";
import { UnlockSheet } from "@/components/payment/UnlockSheet";
import { VideoPlayer } from "@/components/player/VideoPlayer";
import { fetchPlayUrl } from "@/lib/api/video";
import { appendWatchRecord } from "@/lib/api/user";
import { useAuthStore } from "@/lib/stores/auth-store";

export function DramaDetailClient({ drama }: { drama: MicroDrama }) {
  const router = useRouter();
  const [playing, setPlaying] = useState<DramaEpisode | null>(null);
  const [playUrl, setPlayUrl] = useState<string | null>(null);
  const [unlockTarget, setUnlockTarget] = useState<DramaEpisode | null>(null);

  const profile = useAuthStore((s) => s.profile);
  const addWatch = useAuthStore((s) => s.addWatch);

  const handlePlay = async (episode: DramaEpisode) => {
    if (!episode.videoAssetId) {
      alert("该集尚未上传视频");
      return;
    }
    setPlaying(episode);
    try {
      const auth = await fetchPlayUrl({
        videoId: episode.videoAssetId,
        userId: profile.id,
      });
      setPlayUrl(auth?.playUrl ?? null);
      if (drama.id && episode.id) {
        const record = {
          id: crypto.randomUUID(),
          dramaId: drama.id,
          dramaTitle: drama.title ?? "短剧",
          episodeId: episode.id,
          episodeTitle: episode.title,
          episodeNum: episode.episodeNum,
          coverUrl: drama.coverUrl,
          watchedAt: new Date().toISOString(),
        };
        appendWatchRecord(record);
        addWatch(record);
      }
    } catch {
      setPlayUrl(null);
      alert("获取播放地址失败，请确认 video 服务已启动且已购买");
    }
  };

  return (
    <div className="px-4 pb-6 pt-4">
      <div className="relative mb-4 aspect-[16/10] overflow-hidden rounded-2xl bg-zinc-800">
        {drama.coverUrl ? (
          <Image
            src={drama.coverUrl}
            alt={drama.title ?? "短剧封面"}
            fill
            className="object-cover"
            priority
            sizes="(max-width: 512px) 100vw, 512px"
          />
        ) : null}
      </div>

      <h1 className="text-2xl font-bold">{drama.title}</h1>
      <p className="mt-2 text-sm leading-6 text-zinc-400">
        {drama.description || "暂无简介"}
      </p>

      {playing && (
        <div className="mt-5 overflow-hidden rounded-2xl border border-white/10">
          <div className="border-b border-white/10 px-3 py-2 text-sm text-zinc-400">
            正在播放：第 {playing.episodeNum} 集 · {playing.title}
          </div>
          <VideoPlayer src={playUrl} poster={drama.coverUrl} autoPlay />
        </div>
      )}

      <div className="mt-6">
        <div className="mb-3 flex items-center justify-between">
          <h2 className="text-lg font-semibold">剧集列表</h2>
          <button
            type="button"
            onClick={() => router.push(`/play?dramaId=${drama.id}`)}
            className="text-sm text-amber-400"
          >
            滑动播放
          </button>
        </div>
        <EpisodeList
          episodes={drama.episodes ?? []}
          dramaTitle={drama.title}
          onPlay={(ep) => void handlePlay(ep)}
          onUnlock={setUnlockTarget}
        />
      </div>

      {unlockTarget && (
        <UnlockSheet
          open
          dramaTitle={drama.title ?? "短剧"}
          episode={unlockTarget}
          onClose={() => setUnlockTarget(null)}
          onUnlocked={() => {
            setUnlockTarget(null);
            void handlePlay(unlockTarget);
          }}
        />
      )}
    </div>
  );
}
