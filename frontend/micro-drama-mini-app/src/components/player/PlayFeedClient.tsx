"use client";

import { useEffect, useMemo, useState } from "react";
import { useSearchParams } from "next/navigation";
import { Loader2 } from "lucide-react";
import { SwipePlayer } from "@/components/player/SwipePlayer";
import { fetchDramaDetail, fetchDramaPageList } from "@/lib/api/drama";
import type { DramaEpisode, MicroDrama } from "@/types";

export function PlayFeedClient() {
  const searchParams = useSearchParams();
  const dramaId = searchParams.get("dramaId");
  const [loading, setLoading] = useState(true);
  const [dramas, setDramas] = useState<MicroDrama[]>([]);

  useEffect(() => {
    async function load() {
      setLoading(true);
      try {
        if (dramaId) {
          const detail = await fetchDramaDetail(dramaId);
          setDramas(detail ? [detail] : []);
        } else {
          const { rows } = await fetchDramaPageList({ page: 1, size: 20 });
          const details = await Promise.all(
            rows
              .filter((d) => d.id)
              .slice(0, 10)
              .map((d) => fetchDramaDetail(d.id!)),
          );
          setDramas(details.filter(Boolean) as MicroDrama[]);
        }
      } catch {
        setDramas([]);
      } finally {
        setLoading(false);
      }
    }
    void load();
  }, [dramaId]);

  const feedItems = useMemo(() => {
    const items: { drama: MicroDrama; episode: DramaEpisode }[] = [];
    for (const drama of dramas) {
      for (const episode of drama.episodes ?? []) {
        items.push({ drama, episode });
      }
    }
    return items.sort(
      (a, b) =>
        (a.drama.sort ?? 0) - (b.drama.sort ?? 0) ||
        a.episode.episodeNum - b.episode.episodeNum,
    );
  }, [dramas]);

  if (loading) {
    return (
      <div className="flex h-[calc(100dvh-5rem)] items-center justify-center text-zinc-400">
        <Loader2 className="mr-2 h-4 w-4 animate-spin" />
        加载播放列表...
      </div>
    );
  }

  return <SwipePlayer items={feedItems} />;
}
