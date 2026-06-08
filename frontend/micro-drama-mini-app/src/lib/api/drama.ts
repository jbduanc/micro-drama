import { API_BASE, apiFetch } from "@/lib/api/client";
import type {
  ApiResult,
  MicroDrama,
  TablePageInfo,
} from "@/types";

function pageRows<T>(page: TablePageInfo<T>): T[] {
  return page.data ?? page.list ?? [];
}

export async function fetchDramaPageList(params: {
  page?: number;
  size?: number;
  title?: string;
}): Promise<{ rows: MicroDrama[]; total: number }> {
  const page = await apiFetch<TablePageInfo<MicroDrama>>(
    API_BASE.content,
    "/microDramas/pageList",
    {
      method: "POST",
      body: {
        page: params.page ?? 1,
        size: params.size ?? 20,
        title: params.title?.trim() || undefined,
        status: 1,
      },
      cache: "no-store",
    },
  );

  return { rows: pageRows(page), total: page.total ?? 0 };
}

export async function fetchDramaDetail(
  dramaId: string,
): Promise<MicroDrama | null> {
  const res = await apiFetch<ApiResult<MicroDrama>>(
    API_BASE.content,
    `/microDramas/detail/${dramaId}`,
    {
      cache: "no-store",
    },
  );
  return res.data ?? null;
}

export async function fetchDramaIdsForSitemap(): Promise<string[]> {
  try {
    const { rows } = await fetchDramaPageList({ page: 1, size: 500 });
    return rows.map((d) => d.id).filter(Boolean) as string[];
  } catch {
    return [];
  }
}
