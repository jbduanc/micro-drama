"use client";

import { useCallback, useEffect, useRef, useState } from "react";
import { Loader2 } from "lucide-react";
import { DramaCard } from "@/components/drama/DramaCard";
import { DramaSearchBar } from "@/components/drama/DramaSearchBar";
import { fetchDramaPageList } from "@/lib/api/drama";
import type { MicroDrama } from "@/types";

const PAGE_SIZE = 18;

export function DramaListClient() {
  const [keyword, setKeyword] = useState("");
  const [rows, setRows] = useState<MicroDrama[]>([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(1);
  const [loading, setLoading] = useState(true);
  const [loadingMore, setLoadingMore] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const keywordRef = useRef(keyword);
  const loadIdRef = useRef(0);
  const fetchingMoreRef = useRef(false);
  const sentinelRef = useRef<HTMLDivElement>(null);

  const hasMore = rows.length < total;

  const loadPage = useCallback(
    async (pageNum: number, title: string, append: boolean) => {
      const loadId = ++loadIdRef.current;
      if (append) {
        fetchingMoreRef.current = true;
        setLoadingMore(true);
      } else {
        setLoading(true);
        setError(null);
      }

      try {
        const res = await fetchDramaPageList({
          page: pageNum,
          size: PAGE_SIZE,
          title,
        });
        if (loadId !== loadIdRef.current) return;

        setTotal(res.total);
        setRows((prev) => (append ? [...prev, ...res.rows] : res.rows));
      } catch (e) {
        if (loadId !== loadIdRef.current) return;
        const msg =
          e instanceof Error ? e.message : "加载失败，请稍后重试";
        setError(
          msg.includes("Internal Server Error") || msg.includes("500")
            ? "内容服务异常，请确认已部署最新版本且 API 指向 api.dramadjbo.com"
            : msg || "加载失败，请检查 JWT 或网络",
        );
        if (!append) setRows([]);
      } finally {
        if (loadId !== loadIdRef.current) return;
        setLoading(false);
        setLoadingMore(false);
        fetchingMoreRef.current = false;
      }
    },
    [],
  );

  useEffect(() => {
    const timer = setTimeout(() => {
      keywordRef.current = keyword;
      setPage(1);
      void loadPage(1, keyword, false);
    }, 300);
    return () => clearTimeout(timer);
  }, [keyword, loadPage]);

  useEffect(() => {
    if (page <= 1) return;
    void loadPage(page, keywordRef.current, true);
  }, [page, loadPage]);

  useEffect(() => {
    const el = sentinelRef.current;
    if (!el || loading || loadingMore || !hasMore) return;

    const observer = new IntersectionObserver(
      (entries) => {
        if (entries[0]?.isIntersecting && !fetchingMoreRef.current) {
          setPage((p) => p + 1);
        }
      },
      { rootMargin: "120px" },
    );

    observer.observe(el);
    return () => observer.disconnect();
  }, [hasMore, loading, loadingMore, rows.length]);

  return (
    <div className="px-4 pt-4">
      <header className="mb-4">
        <h1 className="text-2xl font-bold">短剧</h1>
        <p className="mt-1 text-sm text-zinc-400">搜索并发现精彩微短剧</p>
      </header>

      <DramaSearchBar value={keyword} onChange={setKeyword} />

      <div className="mt-4">
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
              请从 Telegram 小程序登录，或检查 content-api 服务是否可用
            </p>
          </div>
        )}

        {!loading && !error && rows.length === 0 && (
          <p className="py-12 text-center text-sm text-zinc-500">暂无短剧</p>
        )}

        {!loading && !error && rows.length > 0 && (
          <div className="grid grid-cols-3 gap-x-2 gap-y-4">
            {rows.map((drama) => (
              <DramaCard key={drama.id ?? drama.title} drama={drama} />
            ))}
          </div>
        )}

        {!loading && loadingMore && (
          <div className="flex items-center justify-center py-6 text-zinc-400">
            <Loader2 className="mr-2 h-4 w-4 animate-spin" />
            加载更多...
          </div>
        )}

        {!loading && !loadingMore && hasMore && (
          <div ref={sentinelRef} className="h-4" aria-hidden />
        )}

        {!loading && !loadingMore && rows.length > 0 && !hasMore && (
          <p className="py-6 text-center text-xs text-zinc-600">已加载全部</p>
        )}
      </div>
    </div>
  );
}
