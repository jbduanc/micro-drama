import { useEffect, useMemo, useState } from "react"
import { useNavigate } from "react-router-dom"
import { toast } from "sonner"

import { http } from "@/api/http"
import type { MicroDramaDTO, MicroDramaStatus, Result, TablePageInfo } from "@/api/drama/types"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"

export default function DramaListPage() {
  const navigate = useNavigate()

  const [loading, setLoading] = useState(false)
  const [page, setPage] = useState(1)
  const [size, setSize] = useState(10)
  const [total, setTotal] = useState(0)
  const [rows, setRows] = useState<MicroDramaDTO[]>([])

  const [keyword, setKeyword] = useState("")
  const [status, setStatus] = useState<"all" | "1" | "0">("all")

  const totalPages = useMemo(() => Math.max(1, Math.ceil(total / size)), [total, size])

  const requestBody = useMemo<MicroDramaDTO>(
    () => ({
      page,
      size,
      title: keyword.trim() ? keyword.trim() : undefined,
      status: status === "all" ? undefined : (Number(status) as MicroDramaStatus),
    }),
    [page, size, keyword, status],
  )

  async function fetchList() {
    setLoading(true)
    try {
      const res = await http.post<TablePageInfo<MicroDramaDTO>>("/microDramas/pageList", requestBody)
      setRows(res.data?.data ?? res.data?.list ?? [])
      setTotal(res.data?.total ?? 0)
    } catch (e) {
      console.error(e)
      toast.error("加载短剧列表失败")
      setRows([])
      setTotal(0)
    } finally {
      setLoading(false)
    }
  }

  async function handleDelete(dramaId?: MicroDramaDTO["dramaId"]) {
    if (!dramaId) return
    const confirmed = window.confirm("确定删除该短剧吗？将级联删除所有剧集。")
    if (!confirmed) return
    try {
      const res = await http.post<Result<boolean>>(`/microDramas/delete/${dramaId}`)
      const ok = res.data?.data
      if (!ok) throw new Error(res.data?.msg || res.data?.message || "delete failed")
      toast.success("删除成功")
      const nextTotal = Math.max(0, total - 1)
      const nextPages = Math.max(1, Math.ceil(nextTotal / size))
      if (page > nextPages) setPage(nextPages)
      else await fetchList()
    } catch (e) {
      console.error(e)
      toast.error("删除失败")
    }
  }

  useEffect(() => {
    fetchList()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [page, size, keyword, status])

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-semibold tracking-tight">短剧管理</h1>
          <p className="mt-1 text-sm text-muted-foreground">支持分页、搜索、新增、编辑、删除</p>
        </div>
        <Button onClick={() => navigate("/dramas/new")}>新增短剧</Button>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>短剧列表</CardTitle>
          <CardDescription>新增/编辑将进入独立页面（基础信息 + 剧集管理）</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="mb-4 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
            <div className="flex flex-1 flex-col gap-2 sm:flex-row sm:items-center">
              <div className="w-full sm:max-w-xs">
                <Input
                  placeholder="搜索短剧名称"
                  value={keyword}
                  onChange={(e) => {
                    setPage(1)
                    setKeyword(e.target.value)
                  }}
                />
              </div>
              <select
                value={status}
                onChange={(e) => {
                  setPage(1)
                  setStatus(e.target.value as "all" | "1" | "0")
                }}
                className="h-9 rounded-md border bg-background px-3 text-sm"
              >
                <option value="all">全部状态</option>
                <option value="1">上架</option>
                <option value="0">下架</option>
              </select>
            </div>

            <div className="flex items-center gap-2">
              <Button
                variant="outline"
                onClick={() => {
                  setKeyword("")
                  setStatus("all")
                  setPage(1)
                }}
              >
                重置
              </Button>
              <Button variant="outline" onClick={fetchList} disabled={loading}>
                刷新
              </Button>
            </div>
          </div>

          <Table>
            <TableHeader>
              <TableRow>
                <TableHead className="w-[120px]">ID</TableHead>
                <TableHead>名称</TableHead>
                <TableHead className="w-[120px]">封面</TableHead>
                <TableHead className="w-[120px]">总集数</TableHead>
                <TableHead className="w-[140px]">单剧价格（TON）</TableHead>
                <TableHead className="w-[120px]">状态</TableHead>
                <TableHead className="w-[120px]">排序</TableHead>
                <TableHead className="w-[220px] text-right">操作</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {loading && (
                <TableRow>
                  <TableCell colSpan={8} className="py-8 text-center text-sm text-muted-foreground">
                    加载中...
                  </TableCell>
                </TableRow>
              )}

              {!loading && rows.length === 0 && (
                <TableRow>
                  <TableCell colSpan={8} className="py-10 text-center text-sm text-muted-foreground">
                    暂无数据
                  </TableCell>
                </TableRow>
              )}

              {!loading &&
                rows.map((row) => (
                  <TableRow key={String(row.dramaId ?? row.title ?? Math.random())}>
                    <TableCell>{row.dramaId ?? "-"}</TableCell>
                    <TableCell className="font-medium">{row.title ?? "-"}</TableCell>
                    <TableCell>
                      {row.coverUrl ? (
                        <img
                          src={row.coverUrl}
                          alt="cover"
                          className="h-10 w-10 rounded-md object-cover ring-1 ring-foreground/10"
                        />
                      ) : (
                        <span className="text-sm text-muted-foreground">-</span>
                      )}
                    </TableCell>
                    <TableCell>{row.totalEpisodes ?? "-"}</TableCell>
                    <TableCell>{row.singleDramaPrice ?? "-"}</TableCell>
                    <TableCell>
                      <span
                        className={
                          row.status === 1
                            ? "inline-flex rounded-full bg-emerald-50 px-2 py-0.5 text-xs font-medium text-emerald-700 ring-1 ring-emerald-600/20"
                            : "inline-flex rounded-full bg-zinc-100 px-2 py-0.5 text-xs font-medium text-zinc-700 ring-1 ring-zinc-600/20"
                        }
                      >
                        {row.status === 1 ? "上架" : "下架"}
                      </span>
                    </TableCell>
                    <TableCell>{row.sort ?? "-"}</TableCell>
                    <TableCell className="text-right">
                      <div className="inline-flex gap-2">
                        <Button
                          size="sm"
                          variant="outline"
                          onClick={() => navigate(`/dramas/${row.dramaId}`)}
                          disabled={!row.dramaId}
                        >
                          编辑
                        </Button>
                        <Button
                          size="sm"
                          variant="destructive"
                          onClick={() => handleDelete(row.dramaId)}
                          disabled={!row.dramaId}
                        >
                          删除
                        </Button>
                      </div>
                    </TableCell>
                  </TableRow>
                ))}
            </TableBody>
          </Table>

          <div className="mt-4 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
            <div className="text-sm text-muted-foreground">
              共 {total} 条，当前第 {page} / {totalPages} 页
            </div>
            <div className="flex flex-wrap items-center gap-2">
              <Button variant="outline" size="sm" disabled={page <= 1} onClick={() => setPage(1)}>
                首页
              </Button>
              <Button
                variant="outline"
                size="sm"
                disabled={page <= 1}
                onClick={() => setPage((p) => Math.max(1, p - 1))}
              >
                上一页
              </Button>
              <Button
                variant="outline"
                size="sm"
                disabled={page >= totalPages}
                onClick={() => setPage((p) => Math.min(totalPages, p + 1))}
              >
                下一页
              </Button>
              <select
                value={String(size)}
                onChange={(e) => {
                  setPage(1)
                  setSize(Number(e.target.value))
                }}
                className="h-8 rounded-md border bg-background px-2 text-sm"
              >
                <option value="10">10 条/页</option>
                <option value="20">20 条/页</option>
                <option value="50">50 条/页</option>
              </select>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}
