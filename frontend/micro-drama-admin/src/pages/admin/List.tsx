import { useEffect, useMemo, useState } from "react"
import { toast } from "sonner"

import { http } from "@/api/http"
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"

type SysUser = {
  id: number
  googleEmail: string
  nickname: string
  avatar?: string
  status: 0 | 1
  createTime?: string
  updateTime?: string
}

type UserPageListRequest = {
  page: number
  size: number
  nickname?: string
  googleEmail?: string
  status?: 0 | 1
}

type TablePageInfo<T> = {
  data?: T[]
  list?: T[]
  total: number
}

function getInitials(name?: string) {
  const raw = (name ?? "").trim()
  if (!raw) return "U"
  // prefer latin initials, fallback to last 2 chars for CJK
  const parts = raw.split(/\s+/).filter(Boolean)
  if (parts.length >= 2) return `${parts[0][0] ?? ""}${parts[1][0] ?? ""}`.toUpperCase()
  const ascii = raw.replace(/[^\p{L}\p{N}]/gu, "")
  if (ascii && /[a-zA-Z]/.test(ascii)) return ascii.slice(0, 2).toUpperCase()
  return raw.slice(-2)
}

export default function AdminListPage() {
  const [loading, setLoading] = useState(false)
  const [page, setPage] = useState(1)
  const [size, setSize] = useState(10)
  const [total, setTotal] = useState(0)
  const [rows, setRows] = useState<SysUser[]>([])

  const [nickname, setNickname] = useState("")
  const [googleEmail, setGoogleEmail] = useState("")
  const [status, setStatus] = useState<"all" | "1" | "0">("all")

  const totalPages = useMemo(() => Math.max(1, Math.ceil(total / size)), [total, size])

  const requestBody = useMemo<UserPageListRequest>(
    () => ({
      page,
      size,
      nickname: nickname.trim() ? nickname.trim() : undefined,
      googleEmail: googleEmail.trim() ? googleEmail.trim() : undefined,
      status: status === "all" ? undefined : (Number(status) as 0 | 1),
    }),
    [page, size, nickname, googleEmail, status],
  )

  async function fetchList() {
    setLoading(true)
    try {
      const res = await http.post<TablePageInfo<SysUser>>("/sysUser/pageList", requestBody)
      setRows(res.data?.data ?? res.data?.list ?? [])
      setTotal(res.data?.total ?? 0)
    } catch (e) {
      console.error(e)
      toast.error("加载管理员列表失败")
      setRows([])
      setTotal(0)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchList()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [page, size, nickname, googleEmail, status])

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-semibold tracking-tight">管理员管理</h1>
        <p className="mt-1 text-sm text-muted-foreground">管理员账号与状态管理（分页查询）</p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>管理员列表</CardTitle>
          <CardDescription>支持分页、搜索、筛选、刷新</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="mb-4 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
            <div className="flex flex-1 flex-col gap-2 sm:flex-row sm:items-center">
              <div className="w-full sm:max-w-xs">
                <Input
                  placeholder="搜索昵称"
                  value={nickname}
                  onChange={(e) => {
                    setPage(1)
                    setNickname(e.target.value)
                  }}
                />
              </div>
              <div className="w-full sm:max-w-sm">
                <Input
                  placeholder="搜索 Google 邮箱"
                  value={googleEmail}
                  onChange={(e) => {
                    setPage(1)
                    setGoogleEmail(e.target.value)
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
                <option value="1">正常</option>
                <option value="0">禁用</option>
              </select>
            </div>

            <div className="flex items-center gap-2">
              <Button
                variant="outline"
                onClick={() => {
                  setNickname("")
                  setGoogleEmail("")
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
                <TableHead className="w-[120px]">头像</TableHead>
                <TableHead>昵称</TableHead>
                <TableHead>Google 邮箱</TableHead>
                <TableHead className="w-[120px]">状态</TableHead>
                <TableHead className="w-[220px]">创建时间</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {loading && (
                <TableRow>
                  <TableCell colSpan={6} className="py-8 text-center text-sm text-muted-foreground">
                    加载中...
                  </TableCell>
                </TableRow>
              )}

              {!loading && rows.length === 0 && (
                <TableRow>
                  <TableCell colSpan={6} className="py-10 text-center text-sm text-muted-foreground">
                    暂无数据
                  </TableCell>
                </TableRow>
              )}

              {!loading &&
                rows.map((u) => (
                  <TableRow key={u.id}>
                    <TableCell>{u.id}</TableCell>
                    <TableCell>
                      <Avatar>
                        <AvatarImage src={u.avatar} alt={u.nickname} />
                        <AvatarFallback>{getInitials(u.nickname)}</AvatarFallback>
                      </Avatar>
                    </TableCell>
                    <TableCell className="font-medium">{u.nickname ?? "-"}</TableCell>
                    <TableCell className="max-w-[360px] truncate">{u.googleEmail ?? "-"}</TableCell>
                    <TableCell>
                      <span
                        className={
                          u.status === 1
                            ? "inline-flex rounded-full bg-emerald-50 px-2 py-0.5 text-xs font-medium text-emerald-700 ring-1 ring-emerald-600/20"
                            : "inline-flex rounded-full bg-zinc-100 px-2 py-0.5 text-xs font-medium text-zinc-700 ring-1 ring-zinc-600/20"
                        }
                      >
                        {u.status === 1 ? "正常" : "禁用"}
                      </span>
                    </TableCell>
                    <TableCell className="text-sm text-muted-foreground">{u.createTime ?? "-"}</TableCell>
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
