import { useEffect, useMemo, useRef, useState } from "react"
import { useNavigate, useParams } from "react-router-dom"
import { toast } from "sonner"
import { Pencil, Play, Plus, Trash2, Upload, X } from "lucide-react"

import { dramaService } from "@/api/drama/service"
import type { DramaEpisode, MicroDramaDTO } from "@/api/drama/types"
import { uploadFileWithSts, videoService } from "@/api/video/service"
import type { DeleteVideoItem } from "@/api/video/types"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"

type EpisodeFormState = {
  episodeNum: string
  title: string
  duration: string
  price: string
  videoAssetId: string
  /** 直传 OSS 对应的 object key，保存剧集时用于通知转码 */
  videoFileKey: string
}

type DiscardedVideo = {
  videoId: string
  fileKey: string
}

function toNumberOrUndefined(value: string) {
  const n = Number(value)
  return Number.isFinite(n) ? n : undefined
}

function buildEpisodeRawFileKey(dramaId: string, episodeId: string): string {
  return `raw/${dramaId.trim()}/${episodeId.trim()}.mp4`
}

function parseEpisodeFromRaw(raw: Record<string, unknown>): DramaEpisode {
  const id =
    raw.id != null && String(raw.id).trim() !== "" ? String(raw.id).trim() : undefined

  const title = String(raw.title ?? raw.episodeTitle ?? "")
  const videoAssetIdRaw = raw.videoAssetId ?? raw.video_asset_id ?? raw.videoUrl
  const videoAssetId =
    videoAssetIdRaw != null && String(videoAssetIdRaw).trim() !== ""
      ? String(videoAssetIdRaw).trim()
      : undefined

  const priceRaw = raw.price ?? raw.singleEpisodePrice
  const price =
    priceRaw != null && priceRaw !== "" && Number.isFinite(Number(priceRaw))
      ? Number(priceRaw)
      : undefined

  const duration =
    raw.duration != null && raw.duration !== "" && Number.isFinite(Number(raw.duration))
      ? Number(raw.duration)
      : undefined

  return {
    ...(id ? { id } : {}),
    episodeNum: Number(raw.episodeNum ?? 0) || 0,
    title,
    videoAssetId,
    duration,
    price,
  }
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
    id: undefined as string | undefined,
    title: "",
    coverUrl: "",
    description: "",
    totalEpisodes: "",
    price: "",
    status: "1",
    sort: "",
  })

  const [episodes, setEpisodes] = useState<DramaEpisode[]>([])

  const [epPage, setEpPage] = useState(1)
  const epSize = 12

  const epTotalPages = useMemo(
    () => Math.max(1, Math.ceil((episodes.length + 1) / epSize)),
    [episodes.length],
  )

  const pageEpisodes = useMemo(() => {
    const start = (epPage - 1) * epSize
    return episodes.slice(start, start + epSize)
  }, [episodes, epPage])

  const shouldShowPlusCard = pageEpisodes.length < epSize

  const [episodeDialogOpen, setEpisodeDialogOpen] = useState(false)
  const [editingEpisodeIndex, setEditingEpisodeIndex] = useState<number | null>(null)
  const [episodeForm, setEpisodeForm] = useState<EpisodeFormState>({
    episodeNum: "",
    title: "",
    duration: "",
    price: "",
    videoAssetId: "",
    videoFileKey: "",
  })
  const [originalVideoAssetId, setOriginalVideoAssetId] = useState("")
  const [discardedVideos, setDiscardedVideos] = useState<DiscardedVideo[]>([])
  const [uploadProgress, setUploadProgress] = useState<number | null>(null)
  const [uploading, setUploading] = useState(false)
  const [savingEpisode, setSavingEpisode] = useState(false)
  const [playingVideoId, setPlayingVideoId] = useState<string | null>(null)
  const videoFileInputRef = useRef<HTMLInputElement>(null)

  async function fetchDetail(dramaId: string) {
    setLoading(true)
    try {
      const res = await dramaService.detail(dramaId)
      const data = res?.data
      if (!data) throw new Error("empty data")

      setForm({
        id: data.id,
        title: data.title ?? "",
        coverUrl: data.coverUrl ?? "",
        description: data.description ?? "",
        totalEpisodes: data.totalEpisodes == null ? "" : String(data.totalEpisodes),
        price: data.price == null ? "" : String(data.price),
        status: data.status == null ? "1" : String(data.status),
        sort: data.sort == null ? "" : String(data.sort),
      })
      const rawEpisodes = Array.isArray(data.episodes) ? data.episodes : []
      setEpisodes(rawEpisodes.map((raw) => parseEpisodeFromRaw(raw as Record<string, unknown>)))
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
        id: undefined,
      }))
      setEpisodes([])
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [dramaIdParam])

  useEffect(() => {
    if (epPage > epTotalPages) setEpPage(epTotalPages)
  }, [epPage, epTotalPages])

  function resolveDramaId(): string {
    return String(form.id ?? dramaIdParam ?? "").trim()
  }

  function resolveEpisodeIdForUpload(episodeNum: number, episodeId?: string): string {
    if (episodeId?.trim()) return episodeId.trim()
    return `num-${episodeNum}`
  }

  function resetEpisodeVideoSession(savedVideoId?: string) {
    setOriginalVideoAssetId(savedVideoId ?? "")
    setDiscardedVideos([])
    setUploadProgress(null)
    setUploading(false)
  }

  function markCurrentVideoDiscarded() {
    const vid = episodeForm.videoAssetId.trim()
    const key = episodeForm.videoFileKey.trim()
    if (!vid) return
    setDiscardedVideos((prev) => {
      if (prev.some((d) => d.videoId === vid)) return prev
      return [...prev, { videoId: vid, fileKey: key }]
    })
  }

  function clearEpisodeVideo() {
    markCurrentVideoDiscarded()
    setEpisodeForm((f) => ({ ...f, videoAssetId: "", videoFileKey: "" }))
    setUploadProgress(null)
  }

  function openCreateEpisode() {
    setEditingEpisodeIndex(null)
    resetEpisodeVideoSession()
    setEpisodeForm({
      episodeNum: String(episodes.length + 1),
      title: "",
      duration: "",
      price: "",
      videoAssetId: "",
      videoFileKey: "",
    })
    setEpisodeDialogOpen(true)
  }

  function openEditEpisode(indexInAll: number) {
    const ep = episodes[indexInAll]
    if (!ep) return
    const dramaId = resolveDramaId()
    const episodeId = resolveEpisodeIdForUpload(ep.episodeNum ?? 0, ep.id)
    const videoFileKey =
      dramaId && episodeId ? buildEpisodeRawFileKey(dramaId, episodeId) : ""
    setEditingEpisodeIndex(indexInAll)
    resetEpisodeVideoSession(ep.videoAssetId ?? "")
    setEpisodeForm({
      episodeNum: String(ep.episodeNum ?? ""),
      title: ep.title ?? "",
      duration: ep.duration == null ? "" : String(ep.duration),
      price: ep.price == null ? "" : String(ep.price),
      videoAssetId: ep.videoAssetId ?? "",
      videoFileKey,
    })
    setEpisodeDialogOpen(true)
  }

  async function handleSelectVideoFile(file: File) {
    const dramaId = resolveDramaId()
    if (!dramaId) {
      toast.error("请先保存短剧基础信息后再上传视频")
      return
    }
    const episodeNum = Number(episodeForm.episodeNum)
    if (!Number.isInteger(episodeNum) || episodeNum <= 0) {
      toast.error("请先填写正确的集数")
      return
    }
    const prevEp =
      editingEpisodeIndex != null ? episodes[editingEpisodeIndex] : undefined
    const episodeId = resolveEpisodeIdForUpload(episodeNum, prevEp?.id)

    if (episodeForm.videoAssetId.trim()) {
      markCurrentVideoDiscarded()
    }

    setUploading(true)
    setUploadProgress(0)
    try {
      const contentType = file.type || "video/mp4"
      const sts = await videoService.fetchSts({
        dramaId,
        episodeId,
        contentType,
      })
      await uploadFileWithSts(file, sts, setUploadProgress)
      setEpisodeForm((f) => ({
        ...f,
        videoAssetId: sts.videoId,
        videoFileKey: sts.fileKey,
      }))
      setUploadProgress(100)
      toast.success("视频已上传，请点击确定保存剧集并提交转码")
    } catch (e) {
      console.error(e)
      toast.error(e instanceof Error ? e.message : "视频上传失败")
    } finally {
      setUploading(false)
    }
  }

  async function handlePlayEpisode(ep: DramaEpisode) {
    const videoId = ep.videoAssetId?.trim()
    if (!videoId) {
      toast.error("该剧集尚未关联视频")
      return
    }
    setPlayingVideoId(videoId)
    try {
      const data = await videoService.play(videoId)
      if (data.status !== "READY") {
        toast.error(`视频尚未转码完成（状态：${data.status}），暂不可播放`)
        return
      }
      window.open(data.playUrl, "_blank", "noopener,noreferrer")
    } catch (e) {
      console.error(e)
      toast.error(e instanceof Error ? e.message : "获取播放地址失败")
    } finally {
      setPlayingVideoId(null)
    }
  }

  function collectVideosToDeleteOnSave(finalVideoId: string): DeleteVideoItem[] {
    const items: DeleteVideoItem[] = []
    const seen = new Set<string>()

    const add = (videoId: string, fileKey?: string) => {
      const id = videoId.trim()
      if (!id || id === finalVideoId || seen.has(id)) return
      seen.add(id)
      items.push({ videoId: id, ...(fileKey?.trim() ? { fileKey: fileKey.trim() } : {}) })
    }

    for (const d of discardedVideos) {
      add(d.videoId, d.fileKey)
    }
    if (originalVideoAssetId.trim()) {
      add(originalVideoAssetId)
    }
    return items
  }

  /** 本会话内是否变更过视频（含重新上传、移除、换新 ID） */
  function episodeVideoChanged(finalVideoId: string): boolean {
    const finalId = finalVideoId.trim()
    const savedId = originalVideoAssetId.trim()
    if (discardedVideos.length > 0) return true
    return finalId !== savedId
  }

  async function saveEpisodeFromDialog() {
    const episodeNum = Number(episodeForm.episodeNum)
    if (!Number.isInteger(episodeNum) || episodeNum <= 0) {
      toast.error("请输入正确的集数（正整数）")
      return
    }
    const title = episodeForm.title.trim()
    if (!title) {
      toast.error("请输入剧集标题")
      return
    }

    const prevEp = editingEpisodeIndex != null ? episodes[editingEpisodeIndex] : undefined
    const finalVideoId = episodeForm.videoAssetId.trim()
    const dramaId = resolveDramaId()
    const episodeId = resolveEpisodeIdForUpload(episodeNum, prevEp?.id)
    const fileKey = episodeForm.videoFileKey.trim()
    const videoChanged = episodeVideoChanged(finalVideoId)

    setSavingEpisode(true)
    try {
      if (videoChanged) {
        const toDelete = collectVideosToDeleteOnSave(finalVideoId)
        if (toDelete.length > 0) {
          await videoService.deleteVideos(toDelete)
        }

        if (finalVideoId && fileKey && dramaId && episodeId) {
          await videoService.notifyTranscode({
            videoId: finalVideoId,
            fileKey,
            dramaId,
            episodeId,
          })
        }
      }

      const next: DramaEpisode = {
        ...(prevEp?.id ? { id: prevEp.id } : {}),
        episodeNum,
        title,
        duration: toNumberOrUndefined(episodeForm.duration),
        price: toNumberOrUndefined(episodeForm.price),
        videoAssetId: finalVideoId || undefined,
      }

      setEpisodes((prev) => {
        const cloned = [...prev]
        if (editingEpisodeIndex == null) {
          cloned.push(next)
        } else {
          cloned[editingEpisodeIndex] = { ...cloned[editingEpisodeIndex], ...next }
        }
        cloned.sort((a, b) => (a.episodeNum ?? 0) - (b.episodeNum ?? 0))
        return cloned
      })

      setEpisodeDialogOpen(false)
      const toastMsg = (() => {
        if (editingEpisodeIndex == null) {
          return finalVideoId && videoChanged
            ? "已添加剧集，转码任务已提交（请保存短剧）"
            : "已添加剧集（请保存短剧）"
        }
        if (!videoChanged) return "已更新剧集（视频未变更）"
        return finalVideoId
          ? "已更新剧集，旧视频已清理并提交转码（请保存短剧）"
          : "已更新剧集，已移除视频关联（请保存短剧）"
      })()
      toast.success(toastMsg)
    } catch (e) {
      console.error(e)
      toast.error(e instanceof Error ? e.message : "保存剧集失败")
    } finally {
      setSavingEpisode(false)
    }
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

    const priceNumber = form.price.trim() ? Number(form.price) : undefined
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
      const dramaUuid = String(form.id ?? dramaIdParam ?? "").trim()
      if (!isCreate && !dramaUuid) {
        toast.error("短剧 ID 无效")
        return
      }

      const payload: MicroDramaDTO = {
        ...(!isCreate ? { id: dramaUuid } : {}),
        title,
        coverUrl: form.coverUrl.trim() ? form.coverUrl.trim() : undefined,
        description: form.description.trim() ? form.description.trim() : undefined,
        totalEpisodes: totalEpisodesNumber ?? episodes.length,
        price: priceNumber,
        status: Number(form.status) as 0 | 1,
        sort: sortNumber,
        episodes: episodes.map((ep) => ({
          ...(ep.id ? { id: ep.id } : {}),
          episodeNum: ep.episodeNum,
          title: ep.title,
          videoAssetId: ep.videoAssetId,
          duration: ep.duration,
          price: ep.price,
        })),
      }

      const res = await dramaService.saveOrUpdate(payload)
      const ok = res?.data
      if (!ok) throw new Error(res?.msg || res?.message || "save failed")
      toast.success(isCreate ? "新增成功" : "保存成功")
      navigate("/dramas")
    } catch (e) {
      console.error(e)
      toast.error(isCreate ? "新增失败" : "保存失败")
    } finally {
      setSaving(false)
    }
  }

  const headerTitle = isCreate ? "新增短剧" : `编辑短剧 ${dramaIdParam}`

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
                      <Label htmlFor="price">单剧订阅价格（TON）</Label>
                      <Input
                        id="price"
                        type="number"
                        min={0}
                        step="0.01"
                        placeholder="例如：0.99"
                        value={form.price}
                        onChange={(e) => setForm((f) => ({ ...f, price: e.target.value }))}
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
                      key={`${ep.id ?? "new"}-${ep.episodeNum}-${ep.title}`}
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
                          className="inline-flex h-12 w-12 items-center justify-center rounded-full border bg-background text-foreground shadow-sm transition hover:bg-muted disabled:opacity-50"
                          onClick={() => handlePlayEpisode(ep)}
                          disabled={!ep.videoAssetId || playingVideoId === ep.videoAssetId}
                          aria-label="播放"
                          title={ep.videoAssetId ? "播放（需转码完成）" : "暂无视频"}
                        >
                          <Play className="h-5 w-5" />
                        </button>
                      </div>

                      <div className="mt-6 space-y-1">
                        <div className="truncate text-sm font-medium" title={ep.title}>
                          {ep.title}
                        </div>
                        <div className="flex flex-wrap gap-x-3 gap-y-1 text-xs text-muted-foreground">
                          <div>时长：{ep.duration ?? "-"}s</div>
                          <div>价格：{ep.price ?? "-"} TON</div>
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
            <DialogDescription>
              选择本地视频：先获取 STS 凭证，再直传 OSS Bucket 域名；上传完成后由 OSS 事件自动触发转码，无需手动回调。
            </DialogDescription>
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
              <Label htmlFor="epTitle">剧集标题</Label>
              <Input
                id="epTitle"
                placeholder="例如：第一集"
                value={episodeForm.title}
                onChange={(e) => setEpisodeForm((f) => ({ ...f, title: e.target.value }))}
              />
            </div>

            <div className="grid gap-2 sm:grid-cols-2">
              <div className="grid gap-2">
                <Label htmlFor="epPrice">价格（TON）</Label>
                <Input
                  id="epPrice"
                  type="number"
                  min={0}
                  step="0.01"
                  value={episodeForm.price}
                  onChange={(e) =>
                    setEpisodeForm((f) => ({ ...f, price: e.target.value }))
                  }
                />
              </div>
              <div className="grid gap-2">
                <Label>视频</Label>
                <input
                  ref={videoFileInputRef}
                  type="file"
                  accept="video/*"
                  className="hidden"
                  onChange={(e) => {
                    const file = e.target.files?.[0]
                    e.target.value = ""
                    if (file) void handleSelectVideoFile(file)
                  }}
                />
                <Button
                  type="button"
                  variant="outline"
                  className="w-full justify-start"
                  disabled={uploading || savingEpisode}
                  onClick={() => videoFileInputRef.current?.click()}
                >
                  <Upload className="mr-2 h-4 w-4" />
                  {uploading ? "上传中..." : "选择本地视频上传"}
                </Button>
                {uploadProgress != null && (
                  <div className="space-y-1">
                    <div className="h-2 w-full overflow-hidden rounded-full bg-muted">
                      <div
                        className="h-full bg-primary transition-all"
                        style={{ width: `${uploadProgress}%` }}
                      />
                    </div>
                    <p className="text-xs text-muted-foreground">上传进度 {uploadProgress}%</p>
                  </div>
                )}
                {(episodeForm.videoAssetId.trim() || originalVideoAssetId.trim()) && (
                  <div className="relative rounded-md border bg-muted/30 px-3 py-2 pr-8 text-xs">
                    <button
                      type="button"
                      className="absolute right-1 top-1 inline-flex h-6 w-6 items-center justify-center rounded-md text-muted-foreground hover:bg-muted hover:text-foreground"
                      onClick={clearEpisodeVideo}
                      title="移除视频关联"
                      aria-label="移除视频"
                    >
                      <X className="h-3.5 w-3.5" />
                    </button>
                    <div className="font-medium text-muted-foreground">当前视频 ID</div>
                    <div className="mt-0.5 break-all font-mono">
                      {episodeForm.videoAssetId.trim() || originalVideoAssetId.trim()}
                    </div>
                    {episodeForm.videoFileKey.trim() && (
                      <div className="mt-2 text-muted-foreground">
                        <span className="font-medium">OSS 路径</span>
                        <div className="mt-0.5 break-all font-mono">{episodeForm.videoFileKey}</div>
                      </div>
                    )}
                    <p className="mt-2 text-muted-foreground">
                      可多次上传试片；点击「确定」后才会删除旧视频 ID、用当前 ID 提交转码。
                    </p>
                  </div>
                )}
              </div>
            </div>
          </div>

          <DialogFooter>
            <Button
              variant="outline"
              onClick={() => setEpisodeDialogOpen(false)}
              disabled={savingEpisode || uploading}
            >
              取消
            </Button>
            <Button onClick={() => void saveEpisodeFromDialog()} disabled={savingEpisode || uploading}>
              {savingEpisode ? "保存中..." : "保存"}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  )
}
