import { useEffect, useMemo, useState } from "react"
import { useNavigate, useParams } from "react-router-dom"
import { toast } from "sonner"
import { Pencil, Play, Plus, Trash2 } from "lucide-react"

import { http } from "@/api/http"
import type { DramaEpisode, MicroDramaDTO, Result } from "@/api/drama/types"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Dialog, DialogContent, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"

type EpisodeFormState = {
  episodeNum: string
  episodeTitle: string
  duration: string
  singleEpisodePrice: string
  videoUrl: string
}

function toNumberOrUndefined(value: string) {
  const n = Number(value)
  return Number.isFinite(n) ? n : undefined
}

export default function DramaEditPage() {
  const navigate = useNavigate()
  const params = useParams()
  const dramaIdParam = params.dramaId
  const isCreate = dramaIdParam === "new" || !dramaIdParam

  const [activeTab, setActiveTab] = useState<"base" | "episodes">("base")

  const [loading, setLoading] = useState(false)
  const [saving, setSaving] = useState(false)

  const [form, setForm] = useState({
    dramaId: undefined as MicroDramaDTO["dramaId"],
    title: "",
    coverUrl: "",
    description: "",
    totalEpisodes: "",
    singleDramaPrice: "",
    status: "1",
    sort: "",
  })

  const [episodes, setEpisodes] = useState<DramaEpisode[]>([])

  // episodes pagination
  const [epPage, setEpPage] = useState(1)
  const epSize = 12

  const epTotalPages = useMemo(
    // 额外预留 1 个“新增剧集”块：若当前页已满，则“+块”会出现在下一页
    () => Math.max(1, Math.ceil((episodes.length + 1) / epSize)),
    [episodes.length],
  )

  const pageEpisodes = useMemo(() => {
    const start = (epPage - 1) * epSize
    return episodes.slice(start, start + epSize)
  }, [episodes, epPage])

  const shouldShowPlusCard = pageEpisodes.length < epSize

  // episode dialog
  const [episodeDialogOpen, setEpisodeDialogOpen] = useState(false)
  const [editingEpisodeIndex, setEditingEpisodeIndex] = useState<number | null>(null)
  const [episodeForm, setEpisodeForm] = useState<EpisodeFormState>({
    episodeNum: "",
    episodeTitle: "",
    duration: "",
    singleEpisodePrice: "",
    videoUrl: "",
  })

  async function fetchDetail(dramaId: string) {
    setLoading(true)
    try {
      const res = await http.get<Result<MicroDramaDTO>>(`/microDramas/detail/${dramaId}`)
      const data = res.data?.data
      if (!data) throw new Error("empty data")

      setForm({
        dramaId: data.dramaId,
        title: data.title ?? "",
        coverUrl: data.coverUrl ?? "",
        description: data.description ?? "",
        totalEpisodes: data.totalEpisodes == null ? "" : String(data.totalEpisodes),
        singleDramaPrice: data.singleDramaPrice == null ? "" : String(data.singleDramaPrice),
        status: data.status == null ? "1" : String(data.status),
        sort: data.sort == null ? "" : String(data.sort),
      })
      const rawEpisodes = Array.isArray(data.episodes) ? data.episodes : []
      setEpisodes(rawEpisodes as DramaEpisode[])
    } catch (e) {
      console.error(e)
      toast.error("加载短剧详情失败")
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    if (!isCreate && dramaIdParam) {
      fetchDetail(dramaIdParam)
    } else {
      setForm((f) => ({
        ...f,
        dramaId: undefined,
      }))
      setEpisodes([])
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [dramaIdParam])

  useEffect(() => {
    // keep episodes page valid when deleting
    if (epPage > epTotalPages) setEpPage(epTotalPages)
  }, [epPage, epTotalPages])

  function openCreateEpisode() {
    setEditingEpisodeIndex(null)
    setEpisodeForm({
      episodeNum: String(episodes.length + 1),
      episodeTitle: "",
      duration: "",
      singleEpisodePrice: "",
      videoUrl: "",
    })
    setEpisodeDialogOpen(true)
  }

  function openEditEpisode(indexInAll: number) {
    const ep = episodes[indexInAll]
    if (!ep) return
    setEditingEpisodeIndex(indexInAll)
    setEpisodeForm({
      episodeNum: String(ep.episodeNum ?? ""),
      episodeTitle: ep.episodeTitle ?? "",
      duration: ep.duration == null ? "" : String(ep.duration),
      singleEpisodePrice: ep.singleEpisodePrice == null ? "" : String(ep.singleEpisodePrice),
      videoUrl: ep.videoUrl ?? "",
    })
    setEpisodeDialogOpen(true)
  }

  function saveEpisodeFromDialog() {
    const episodeNum = Number(episodeForm.episodeNum)
    if (!Number.isInteger(episodeNum) || episodeNum <= 0) {
      toast.error("请输入正确的集数（正整数）")
      return
    }
    const episodeTitle = episodeForm.episodeTitle.trim()
    if (!episodeTitle) {
      toast.error("请输入剧集标题")
      return
    }

    const next: DramaEpisode = {
      episodeNum,
      episodeTitle,
      duration: toNumberOrUndefined(episodeForm.duration),
      singleEpisodePrice: toNumberOrUndefined(episodeForm.singleEpisodePrice),
      videoUrl: episodeForm.videoUrl.trim() ? episodeForm.videoUrl.trim() : undefined,
    }

    setEpisodes((prev) => {
      const cloned = [...prev]
      if (editingEpisodeIndex == null) {
        cloned.push(next)
      } else {
        cloned[editingEpisodeIndex] = { ...cloned[editingEpisodeIndex], ...next }
      }
      // keep stable order by episodeNum then sort
      cloned.sort((a, b) => (a.episodeNum ?? 0) - (b.episodeNum ?? 0))
      return cloned
    })

    setEpisodeDialogOpen(false)
    toast.success(editingEpisodeIndex == null ? "已添加剧集（未提交）" : "已更新剧集（未提交）")
  }

  function deleteEpisode(indexInAll: number) {
    const ep = episodes[indexInAll]
    if (!ep) return
    const confirmed = window.confirm(`确定删除第${ep.episodeNum ?? "-"}集吗？`)
    if (!confirmed) return
    setEpisodes((prev) => prev.filter((_, idx) => idx !== indexInAll))
    toast.success("已删除剧集（未提交）")
  }

  async function handleSaveAll() {
    const title = form.title.trim()
    if (!title) {
      toast.error("请输入短剧名称")
      setActiveTab("base")
      return
    }

    const totalEpisodesNumber = form.totalEpisodes.trim() ? Number(form.totalEpisodes) : undefined
    if (totalEpisodesNumber != null && !Number.isInteger(totalEpisodesNumber)) {
      toast.error("总集数必须为整数")
      setActiveTab("base")
      return
    }

    const priceNumber = form.singleDramaPrice.trim() ? Number(form.singleDramaPrice) : undefined
    if (priceNumber != null && !Number.isFinite(priceNumber)) {
      toast.error("请输入正确的单剧价格")
      setActiveTab("base")
      return
    }

    const sortNumber = form.sort.trim() ? Number(form.sort) : undefined
    if (sortNumber != null && !Number.isInteger(sortNumber)) {
      toast.error("排序必须为整数")
      setActiveTab("base")
      return
    }

    setSaving(true)
    try {
      const payload: MicroDramaDTO = {
        dramaId: isCreate ? undefined : form.dramaId ?? dramaIdParam,
        title,
        coverUrl: form.coverUrl.trim() ? form.coverUrl.trim() : undefined,
        description: form.description.trim() ? form.description.trim() : undefined,
        totalEpisodes: totalEpisodesNumber ?? episodes.length,
        singleDramaPrice: priceNumber,
        status: Number(form.status) as 0 | 1,
        sort: sortNumber,
        episodes: episodes.map((ep) => ({
          ...ep,
          dramaId: isCreate ? undefined : form.dramaId ?? dramaIdParam,
        })),
      }

      const res = await http.post<Result<boolean>>("/microDramas/saveOrUpdate", payload)
      const ok = res.data?.data
      if (!ok) throw new Error(res.data?.msg || res.data?.message || "save failed")
      toast.success(isCreate ? "新增成功" : "保存成功")
      navigate("/dramas")
    } catch (e) {
      console.error(e)
      toast.error(isCreate ? "新增失败" : "保存失败")
    } finally {
      setSaving(false)
    }
  }

  const headerTitle = isCreate ? "新增短剧" : `编辑短剧 #${dramaIdParam}`

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 className="text-2xl font-semibold tracking-tight">{headerTitle}</h1>
          <p className="mt-1 text-sm text-muted-foreground">基础信息与剧集管理在同一页面维护</p>
        </div>
        <div className="flex items-center gap-2">
          <Button variant="outline" onClick={() => navigate("/dramas")} disabled={saving}>
            返回列表
          </Button>
          <Button onClick={handleSaveAll} disabled={saving}>
            {saving ? "保存中..." : "保存"}
          </Button>
        </div>
      </div>

      <Tabs value={activeTab} onValueChange={(v) => setActiveTab(v as "base" | "episodes")}>
        <TabsList>
          <TabsTrigger value="base">基础信息</TabsTrigger>
          <TabsTrigger value="episodes">剧集管理</TabsTrigger>
        </TabsList>

        <TabsContent value="base">
          <Card>
            <CardHeader>
              <CardTitle>基础信息</CardTitle>
              <CardDescription>填写短剧的基本信息（保存后生效）</CardDescription>
            </CardHeader>
            <CardContent>
              {loading && <div className="py-8 text-sm text-muted-foreground">加载中...</div>}

              {!loading && (
                <div className="grid gap-4">
                  <div className="grid gap-2">
                    <Label htmlFor="title">短剧名称</Label>
                    <Input
                      id="title"
                      placeholder="请输入短剧名称"
                      value={form.title}
                      onChange={(e) => setForm((f) => ({ ...f, title: e.target.value }))}
                    />
                  </div>

                  <div className="grid gap-2">
                    <Label htmlFor="coverUrl">封面 URL</Label>
                    <Input
                      id="coverUrl"
                      placeholder="https://..."
                      value={form.coverUrl}
                      onChange={(e) => setForm((f) => ({ ...f, coverUrl: e.target.value }))}
                    />
                  </div>

                  <div className="grid gap-2">
                    <Label htmlFor="description">简介</Label>
                    <textarea
                      id="description"
                      placeholder="请输入简介"
                      value={form.description}
                      onChange={(e) => setForm((f) => ({ ...f, description: e.target.value }))}
                      className="min-h-[96px] w-full rounded-md border bg-background px-3 py-2 text-sm outline-none ring-offset-background focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2"
                    />
                  </div>

                  <div className="grid gap-4 sm:grid-cols-2">
                    <div className="grid gap-2">
                      <Label htmlFor="totalEpisodes">总集数</Label>
                      <Input
                        id="totalEpisodes"
                        type="number"
                        min={0}
                        placeholder="留空则按剧集数量自动计算"
                        value={form.totalEpisodes}
                        onChange={(e) => setForm((f) => ({ ...f, totalEpisodes: e.target.value }))}
                      />
                    </div>
                    <div className="grid gap-2">
                      <Label htmlFor="singleDramaPrice">单剧订阅价格（TON）</Label>
                      <Input
                        id="singleDramaPrice"
                        type="number"
                        min={0}
                        step="0.01"
                        placeholder="例如：0.99"
                        value={form.singleDramaPrice}
                        onChange={(e) => setForm((f) => ({ ...f, singleDramaPrice: e.target.value }))}
                      />
                    </div>
                  </div>

                  <div className="grid gap-4 sm:grid-cols-2">
                    <div className="grid gap-2">
                      <Label>状态</Label>
                      <select
                        value={form.status}
                        onChange={(e) => setForm((f) => ({ ...f, status: e.target.value }))}
                        className="h-9 rounded-md border bg-background px-3 text-sm"
                      >
                        <option value="1">上架</option>
                        <option value="0">下架</option>
                      </select>
                    </div>
                    <div className="grid gap-2">
                      <Label htmlFor="sort">排序</Label>
                      <Input
                        id="sort"
                        type="number"
                        placeholder="数值越大越靠前"
                        value={form.sort}
                        onChange={(e) => setForm((f) => ({ ...f, sort: e.target.value }))}
                      />
                    </div>
                  </div>
                </div>
              )}
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="episodes">
          <Card>
            <CardHeader className="flex flex-row items-start justify-between gap-4">
              <div>
                <CardTitle>剧集管理</CardTitle>
                <CardDescription>每页 12 个剧集块，支持新增、编辑、删除（保存后提交）</CardDescription>
              </div>
              <Button variant="outline" onClick={openCreateEpisode} disabled={saving}>
                <Plus className="mr-1 h-4 w-4" />
                新增剧集
              </Button>
            </CardHeader>
            <CardContent>
              <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
                {pageEpisodes.map((ep, idx) => {
                  const globalIndex = (epPage - 1) * epSize + idx
                  return (
                    <div
                      key={`${ep.episodeId ?? "new"}-${ep.episodeNum}-${ep.episodeTitle}`}
                      className="relative overflow-hidden rounded-xl border bg-card p-3 text-card-foreground shadow-sm transition hover:shadow-md"
                    >
                      <div className="flex items-start justify-between gap-2">
                        <div className="text-xs font-medium text-muted-foreground">
                          第{ep.episodeNum ?? "-"}集
                        </div>
                        <div className="flex items-center gap-1">
                          <Button
                            size="icon-sm"
                            variant="ghost"
                            onClick={() => openEditEpisode(globalIndex)}
                            title="编辑剧集"
                          >
                            <Pencil className="h-4 w-4" />
                          </Button>
                          <Button
                            size="icon-sm"
                            variant="ghost"
                            onClick={() => deleteEpisode(globalIndex)}
                            title="删除剧集"
                          >
                            <Trash2 className="h-4 w-4" />
                          </Button>
                        </div>
                      </div>

                      <div className="mt-6 flex items-center justify-center">
                        <button
                          type="button"
                          className="inline-flex h-12 w-12 items-center justify-center rounded-full border bg-background text-foreground shadow-sm transition hover:bg-muted"
                          onClick={() => toast.message("播放功能预留（待接入播放器）")}
                          aria-label="播放"
                        >
                          <Play className="h-5 w-5" />
                        </button>
                      </div>

                      <div className="mt-6 space-y-1">
                        <div className="truncate text-sm font-medium" title={ep.episodeTitle}>
                          {ep.episodeTitle}
                        </div>
                        <div className="flex flex-wrap gap-x-3 gap-y-1 text-xs text-muted-foreground">
                          <div>时长：{ep.duration ?? "-"}s</div>
                          <div>价格：{ep.singleEpisodePrice ?? "-"} TON</div>
                        </div>
                      </div>
                    </div>
                  )
                })}

                {shouldShowPlusCard && (
                  <button
                    type="button"
                    onClick={openCreateEpisode}
                    className="flex min-h-[180px] items-center justify-center rounded-xl border border-dashed bg-card text-card-foreground transition hover:bg-muted/30"
                    aria-label="新增剧集"
                  >
                    <div className="flex flex-col items-center gap-2 text-muted-foreground">
                      <Plus className="h-8 w-8" />
                      <div className="text-sm">新增剧集</div>
                    </div>
                  </button>
                )}
              </div>

              <div className="mt-5 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
                <div className="text-sm text-muted-foreground">
                  共 {episodes.length} 个剧集，当前第 {epPage} / {epTotalPages} 页
                </div>
                <div className="flex flex-wrap items-center gap-2">
                  <Button
                    variant="outline"
                    size="sm"
                    disabled={epPage <= 1}
                    onClick={() => setEpPage(1)}
                  >
                    首页
                  </Button>
                  <Button
                    variant="outline"
                    size="sm"
                    disabled={epPage <= 1}
                    onClick={() => setEpPage((p) => Math.max(1, p - 1))}
                  >
                    上一页
                  </Button>
                  <Button
                    variant="outline"
                    size="sm"
                    disabled={epPage >= epTotalPages}
                    onClick={() => setEpPage((p) => Math.min(epTotalPages, p + 1))}
                  >
                    下一页
                  </Button>
                </div>
              </div>
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>

      <Dialog open={episodeDialogOpen} onOpenChange={setEpisodeDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>{editingEpisodeIndex == null ? "新增剧集" : "编辑剧集"}</DialogTitle>
          </DialogHeader>

          <div className="grid gap-4 py-2">
            <div className="grid gap-2 sm:grid-cols-2">
              <div className="grid gap-2">
                <Label htmlFor="episodeNum">集数</Label>
                <Input
                  id="episodeNum"
                  type="number"
                  min={1}
                  value={episodeForm.episodeNum}
                  onChange={(e) => setEpisodeForm((f) => ({ ...f, episodeNum: e.target.value }))}
                />
              </div>
              <div className="grid gap-2">
                <Label htmlFor="duration">播放时长（秒）</Label>
                <Input
                  id="duration"
                  type="number"
                  min={0}
                  value={episodeForm.duration}
                  onChange={(e) =>
                    setEpisodeForm((f) => ({ ...f, duration: e.target.value }))
                  }
                />
              </div>
            </div>

            <div className="grid gap-2">
              <Label htmlFor="episodeTitle">剧集标题</Label>
              <Input
                id="episodeTitle"
                placeholder="例如：第一集"
                value={episodeForm.episodeTitle}
                onChange={(e) => setEpisodeForm((f) => ({ ...f, episodeTitle: e.target.value }))}
              />
            </div>

            <div className="grid gap-2 sm:grid-cols-2">
              <div className="grid gap-2">
                <Label htmlFor="singleEpisodePrice">价格（TON）</Label>
                <Input
                  id="singleEpisodePrice"
                  type="number"
                  min={0}
                  step="0.01"
                  value={episodeForm.singleEpisodePrice}
                  onChange={(e) =>
                    setEpisodeForm((f) => ({ ...f, singleEpisodePrice: e.target.value }))
                  }
                />
              </div>
              <div className="grid gap-2">
                <Label htmlFor="videoUrl">视频地址（预留）</Label>
                <Input
                  id="videoUrl"
                  placeholder="https://..."
                  value={episodeForm.videoUrl}
                  onChange={(e) => setEpisodeForm((f) => ({ ...f, videoUrl: e.target.value }))}
                />
              </div>
            </div>
          </div>

          <DialogFooter>
            <Button variant="outline" onClick={() => setEpisodeDialogOpen(false)}>
              取消
            </Button>
            <Button onClick={saveEpisodeFromDialog}>保存</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  )
}

