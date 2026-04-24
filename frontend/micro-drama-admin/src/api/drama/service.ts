import { http } from "@/api/http"
import type { MicroDramaDTO, Result, TablePageInfo } from "./types"

export const dramaService = {
  /**
   * 分页查询短剧列表
   * POST /microDramas/pageList
   */
  async pageList(payload: MicroDramaDTO): Promise<TablePageInfo<MicroDramaDTO>> {
    const res = await http.post<TablePageInfo<MicroDramaDTO>>(
      "/microDramas/pageList",
      payload,
    )
    return res.data
  },

  /**
   * 根据ID查询短剧详情（含剧集列表）
   * GET /microDramas/detail/{dramaId}
   */
  async detail(dramaId: number | string): Promise<Result<MicroDramaDTO>> {
    const res = await http.get<Result<MicroDramaDTO>>(
      `/microDramas/detail/${dramaId}`,
    )
    return res.data
  },

  /**
   * 新增/编辑短剧（含剧集信息）
   * POST /microDramas/saveOrUpdate
   */
  async saveOrUpdate(dto: MicroDramaDTO): Promise<Result<boolean>> {
    const res = await http.post<Result<boolean>>("/microDramas/saveOrUpdate", dto)
    return res.data
  },

  /**
   * 根据ID删除短剧（级联删除所有剧集）
   * POST /microDramas/delete/{dramaId}
   */
  async remove(dramaId: number | string): Promise<Result<boolean>> {
    const res = await http.post<Result<boolean>>(`/microDramas/delete/${dramaId}`)
    return res.data
  },
}
