import { Suspense } from "react";
import { Loader2 } from "lucide-react";
import { PlayFeedClient } from "@/components/player/PlayFeedClient";

export default function PlayPage() {
  return (
    <Suspense
      fallback={
        <div className="flex h-[calc(100dvh-5rem)] items-center justify-center text-zinc-400">
          <Loader2 className="mr-2 h-4 w-4 animate-spin" />
          加载中...
        </div>
      }
    >
      <PlayFeedClient />
    </Suspense>
  );
}
