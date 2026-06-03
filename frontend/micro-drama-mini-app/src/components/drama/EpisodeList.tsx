"use client";

import { Lock, Play } from "lucide-react";
import type { DramaEpisode } from "@/types";
import { cn, formatDuration, formatPrice } from "@/lib/utils";
import { useAuthStore } from "@/lib/stores/auth-store";

type Props = {
  episodes: DramaEpisode[];
  dramaTitle?: string;
  onPlay: (episode: DramaEpisode) => void;
  onUnlock: (episode: DramaEpisode) => void;
};

export function EpisodeList({ episodes, onPlay, onUnlock }: Props) {
  const isUnlocked = useAuthStore((s) => s.isUnlocked);
  const sorted = [...episodes].sort((a, b) => a.episodeNum - b.episodeNum);

  return (
    <ul className="space-y-2">
      {sorted.map((episode) => {
        const unlocked = isUnlocked(episode.id, episode.price);
        return (
          <li key={episode.id ?? episode.episodeNum}>
            <button
              type="button"
              onClick={() => (unlocked ? onPlay(episode) : onUnlock(episode))}
              className={cn(
                "flex w-full items-center gap-3 rounded-xl border px-3 py-3 text-left transition",
                unlocked
                  ? "border-white/5 bg-white/[0.03] hover:border-amber-400/30"
                  : "border-amber-400/20 bg-amber-400/5 hover:bg-amber-400/10",
              )}
            >
              <span className="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg bg-zinc-800 text-sm font-semibold text-amber-300">
                {episode.episodeNum}
              </span>
              <div className="min-w-0 flex-1">
                <p className="truncate font-medium">{episode.title}</p>
                <p className="text-xs text-zinc-500">
                  {formatDuration(episode.duration)} · {formatPrice(episode.price)}
                </p>
              </div>
              {unlocked ? (
                <Play className="h-4 w-4 shrink-0 text-amber-400" />
              ) : (
                <Lock className="h-4 w-4 shrink-0 text-amber-400" />
              )}
            </button>
          </li>
        );
      })}
    </ul>
  );
}
