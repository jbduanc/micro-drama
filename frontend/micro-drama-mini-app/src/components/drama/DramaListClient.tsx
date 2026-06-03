"use client";

import { useCallback, useEffect, useState } from "react";
import { Loader2 } from "lucide-react";
import { DramaCard } from "@/components/drama/DramaCard";
import { DramaSearchBar } from "@/components/drama/DramaSearchBar";
import { fetchDramaPageList } from "@/lib/api/drama";
import type { MicroDrama } from "@/types";

export function DramaListClient() {
  const [keyword, setKeyword] = useState("");
  const [rows, setRows] = useState<MicroDrama[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async (title?: string) => {
    setLoading(true);
    setError(null);
    try {
      const res = await fetchDramaPageList({ page: 1, size: 50, title });
      setRows(res.rows);
    } catch (e) {
      setError(
        e instanceof Error
          ? e.message
          : "加载失败，请检查 content-api 与 JWT 配置",
      );
      setRows([]);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    const timer = setTimeout(() => {
      void load(keyword);
    }, 300);
    return () => clearTimeout(timer);
  }, [keyword, load]);

  return (
    <div className="px-4 pt-4">
      <header className="mb-4">
        <h1 className="text-2xl font-bold">短剧</h1>
        <p className="mt-1 text-sm text-zinc-400">搜索并发现精彩微短剧</p>
      </header>

      <DramaSearchBar value={keyword} onChange={setKeyword} />

      <div className="mt-4 space-y-3">
        {loading && (
          <div className="flex items-center justify-center py-12 text-zinc-400">
            <Loader2 className="mr-2 h-4 w-4 animate-spin" />
            加载中...
          </div>
        )}

        {!loading && error && (
          <div className="rounded-xl border border-red-500/20 bg-red-500/10 p-4 text-sm text-red-200">
            {error}
            <p className="mt-2 text-xs text-red-300/80">
              请在 .env.local 设置 NEXT_PUBLIC_DEV_JWT_TOKEN 后重启 dev 服务
            </p>
          </div>
        )}

        {!loading && !error && rows.length === 0 && (
          <p className="py-12 text-center text-sm text-zinc-500">暂无短剧</p>
        )}

        {!loading &&
          rows.map((drama) => <DramaCard key={drama.id ?? drama.title} drama={drama} />)}
      </div>
    </div>
  );
}
