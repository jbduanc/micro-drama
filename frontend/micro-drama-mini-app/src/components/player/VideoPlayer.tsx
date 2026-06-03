"use client";

import { useEffect, useRef } from "react";

type Props = {
  src?: string | null;
  poster?: string;
  autoPlay?: boolean;
  onEnded?: () => void;
};

export function VideoPlayer({ src, poster, autoPlay, onEnded }: Props) {
  const ref = useRef<HTMLVideoElement>(null);

  useEffect(() => {
    const el = ref.current;
    if (!el || !src) return;
    el.load();
    if (autoPlay) {
      void el.play().catch(() => undefined);
    }
  }, [src, autoPlay]);

  if (!src) {
    return (
      <div className="flex aspect-[9/16] w-full items-center justify-center bg-black text-sm text-zinc-400">
        暂无可播放地址
      </div>
    );
  }

  return (
    <video
      ref={ref}
      className="aspect-[9/16] w-full bg-black object-contain"
      src={src}
      poster={poster}
      controls
      playsInline
      onEnded={onEnded}
    />
  );
}
