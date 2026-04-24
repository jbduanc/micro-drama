// src/api/drama/hooks.ts
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query"
import { dramaService } from "./service"
import type { MicroDramaDTO } from "./types"

export const dramaKeys = {
  all: ["microDramas"] as const,
  lists: () => [...dramaKeys.all, "list"] as const,
  list: (params: MicroDramaDTO) => [...dramaKeys.lists(), params] as const,
  details: () => [...dramaKeys.all, "detail"] as const,
  detail: (id: number | string) => [...dramaKeys.details(), id] as const,
}

export function useDramaPageList(params: MicroDramaDTO) {
  return useQuery({
    queryKey: dramaKeys.list(params),
    queryFn: () => dramaService.pageList(params),
    placeholderData: (previousData) => previousData, // 分页时保留旧数据
  })
}

export function useDramaDetail(id: number | string) {
  return useQuery({
    queryKey: dramaKeys.detail(id),
    queryFn: () => dramaService.detail(id),
    enabled: !!id,
  })
}

export function useSaveOrUpdateDrama() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: dramaService.saveOrUpdate,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: dramaKeys.lists() });
    },
  });
}

export function useDeleteDrama() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: dramaService.remove,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: dramaKeys.lists() });
    },
  });
}
