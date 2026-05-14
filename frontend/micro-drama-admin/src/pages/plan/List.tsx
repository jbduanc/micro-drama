import { useEffect, useMemo, useState } from 'react'
import { toast } from 'sonner'

import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import { http } from '@/api/http'

type MemberPlan = {
  id: string
  planName: string
  price: number
  durationDays: number
  status: 0 | 1
  createTime?: string
}

type PageListRequest = {
  page: number
  size: number
  planName?: string
  status?: 0 | 1
}

type TablePageInfo<T> = {
  // backend may return either `data` or `list`
  data?: T[]
  list?: T[]
  total: number
}

type Result<T> = {
  code?: number
  msg?: string
  message?: string
  data: T
}

export default function PlanListPage() {
  const [loading, setLoading] = useState(false)
  const [page, setPage] = useState(1)
  const [size, setSize] = useState(10)
  const [total, setTotal] = useState(0)
  const [rows, setRows] = useState<MemberPlan[]>([])

  const [keyword, setKeyword] = useState('')
  const [status, setStatus] = useState<'all' | '1' | '0'>('all')

  const [dialogOpen, setDialogOpen] = useState(false)
  const [saving, setSaving] = useState(false)
  const [editingId, setEditingId] = useState<string | null>(null)
  const [form, setForm] = useState({
    planName: '',
    price: '',
    durationDays: '',
    status: 1 as 0 | 1,
  })

  const totalPages = useMemo(() => Math.max(1, Math.ceil(total / size)), [total, size])

  const requestBody = useMemo<PageListRequest>(
    () => ({
      page,
      size,
      planName: keyword.trim() ? keyword.trim() : undefined,
      status: status === 'all' ? undefined : (Number(status) as 0 | 1),
    }),
    [page, size, keyword, status]
  )

  async function fetchList() {
    setLoading(true)
    try {
      const res = await http.post<TablePageInfo<MemberPlan>>('/memberPlans/pageList', requestBody)
      setRows(res.data?.data ?? res.data?.list ?? [])
      setTotal(res.data?.total ?? 0)
    } catch (e) {
      console.error(e)
      toast.error('加载会员套餐失败')
      setRows([])
      setTotal(0)
    } finally {
      setLoading(false)
    }
  }

  async function openCreate() {
    setEditingId(null)
    setForm({
      planName: '',
      price: '',
      durationDays: '',
      status: 1,
    })
    setDialogOpen(true)
  }

  async function openEdit(id: string) {
    setEditingId(id)
    setDialogOpen(true)
    try {
      const res = await http.get<Result<MemberPlan>>(`/memberPlans/getById/${id}`)
      const data = res.data?.data
      if (!data) throw new Error('empty data')
      setForm({
        planName: data.planName ?? '',
        price: String(data.price ?? ''),
        durationDays: String(data.durationDays ?? ''),
        status: (data.status ?? 1) as 0 | 1,
      })
    } catch (e) {
      console.error(e)
      toast.error('加载套餐详情失败')
    }
  }

  async function handleSave() {
    const planName = form.planName.trim()
    const priceNumber = Number(form.price)
    const daysNumber = Number(form.durationDays)

    if (!planName) {
      toast.error('请输入套餐名称')
      return
    }
    if (!Number.isFinite(priceNumber) || priceNumber < 0) {
      toast.error('请输入正确的价格')
      return
    }
    if (!Number.isInteger(daysNumber) || daysNumber <= 0) {
      toast.error('请输入正确的有效天数')
      return
    }

    setSaving(true)
    try {
      const payload = {
        id: editingId ?? undefined,
        planName,
        price: priceNumber,
        durationDays: daysNumber,
        status: form.status,
      }
      const res = await http.post<Result<boolean>>('/memberPlans/saveOrUpdate', payload)
      const ok = res.data?.data
      if (!ok) throw new Error(res.data?.msg || res.data?.message || 'save failed')
      toast.success(editingId ? '编辑成功' : '新增成功')
      setDialogOpen(false)
      await fetchList()
    } catch (e) {
      console.error(e)
      toast.error(editingId ? '编辑失败' : '新增失败')
    } finally {
      setSaving(false)
    }
  }

  async function handleDelete(id: string) {
    const confirmed = window.confirm('确定删除该会员套餐吗？')
    if (!confirmed) return
    try {
      const res = await http.post<Result<boolean>>(`/memberPlans/delete/${id}`)
      const ok = res.data?.data
      if (!ok) throw new Error(res.data?.msg || res.data?.message || 'delete failed')
      toast.success('删除成功')
      // 若删除后当前页为空，回退一页
      const nextTotal = Math.max(0, total - 1)
      const nextPages = Math.max(1, Math.ceil(nextTotal / size))
      if (page > nextPages) setPage(nextPages)
      else await fetchList()
    } catch (e) {
      console.error(e)
      toast.error('删除失败')
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
          <h1 className="text-2xl font-semibold tracking-tight">会员套餐管理</h1>
          <p className="mt-1 text-sm text-muted-foreground">管理会员时长与价格配置</p>
        </div>
        <Button onClick={openCreate}>新增套餐</Button>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>套餐列表</CardTitle>
          <CardDescription>支持分页、搜索、新增、编辑、删除</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="mb-4 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
            <div className="flex flex-1 flex-col gap-2 sm:flex-row sm:items-center">
              <div className="w-full sm:max-w-xs">
                <Input
                  placeholder="搜索套餐名称"
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
                  setStatus(e.target.value as 'all' | '1' | '0')
                }}
                className="h-9 rounded-md border bg-background px-3 text-sm"
              >
                <option value="all">全部状态</option>
                <option value="1">启用</option>
                <option value="0">禁用</option>
              </select>
            </div>

            <div className="flex items-center gap-2">
              <Button
                variant="outline"
                onClick={() => {
                  setKeyword('')
                  setStatus('all')
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
                <TableHead className="w-[160px]">时长（天）</TableHead>
                <TableHead className="w-[160px]">价格（TON）</TableHead>
                <TableHead className="w-[120px]">状态</TableHead>
                <TableHead className="w-[200px] text-right">操作</TableHead>
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
                rows.map((plan) => (
                  <TableRow key={plan.id}>
                    <TableCell className="max-w-[120px] truncate font-mono text-xs" title={plan.id}>
                      {plan.id}
                    </TableCell>
                    <TableCell className="font-medium">{plan.planName}</TableCell>
                    <TableCell>{plan.durationDays}</TableCell>
                    <TableCell>{plan.price}</TableCell>
                    <TableCell>
                      <span
                        className={
                          plan.status === 1
                            ? 'inline-flex rounded-full bg-emerald-50 px-2 py-0.5 text-xs font-medium text-emerald-700 ring-1 ring-emerald-600/20'
                            : 'inline-flex rounded-full bg-zinc-100 px-2 py-0.5 text-xs font-medium text-zinc-700 ring-1 ring-zinc-600/20'
                        }
                      >
                        {plan.status === 1 ? '启用' : '禁用'}
                      </span>
                    </TableCell>
                    <TableCell className="text-right">
                      <div className="inline-flex gap-2">
                        <Button size="sm" variant="outline" onClick={() => openEdit(plan.id)}>
                          编辑
                        </Button>
                        <Button
                          size="sm"
                          variant="destructive"
                          onClick={() => handleDelete(plan.id)}
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
                <option value="5">5 条/页</option>
                <option value="10">10 条/页</option>
                <option value="20">20 条/页</option>
                <option value="50">50 条/页</option>
              </select>
            </div>
          </div>
        </CardContent>
      </Card>

      <Dialog
        open={dialogOpen}
        onOpenChange={(open) => {
          setDialogOpen(open)
        }}
      >
        <DialogContent>
          <DialogHeader>
            <DialogTitle>{editingId ? '编辑套餐' : '新增套餐'}</DialogTitle>
            <DialogDescription>保存后将立即生效（根据后端逻辑）。</DialogDescription>
          </DialogHeader>

          <div className="grid gap-4 py-2">
            <div className="grid gap-2">
              <Label htmlFor="planName">套餐名称</Label>
              <Input
                id="planName"
                placeholder="例如：月卡/季卡/年卡"
                value={form.planName}
                onChange={(e) => setForm((f) => ({ ...f, planName: e.target.value }))}
              />
            </div>

            <div className="grid gap-2 sm:grid-cols-2">
              <div className="grid gap-2">
                <Label htmlFor="durationDays">有效天数</Label>
                <Input
                  id="durationDays"
                  type="number"
                  min={1}
                  placeholder="例如：30"
                  value={form.durationDays}
                  onChange={(e) => setForm((f) => ({ ...f, durationDays: e.target.value }))}
                />
              </div>
              <div className="grid gap-2">
                <Label htmlFor="price">价格（TON）</Label>
                <Input
                  id="price"
                  type="number"
                  min={0}
                  step="0.01"
                  placeholder="例如：19.9"
                  value={form.price}
                  onChange={(e) => setForm((f) => ({ ...f, price: e.target.value }))}
                />
              </div>
            </div>

            <div className="grid gap-2">
              <Label>状态</Label>
              <div className="flex items-center gap-2">
                <select
                  value={String(form.status)}
                  onChange={(e) =>
                    setForm((f) => ({ ...f, status: Number(e.target.value) as 0 | 1 }))
                  }
                  className="h-9 w-full rounded-md border bg-background px-3 text-sm"
                >
                  <option value="1">启用</option>
                  <option value="0">禁用</option>
                </select>
              </div>
            </div>
          </div>

          <DialogFooter>
            <Button variant="outline" onClick={() => setDialogOpen(false)} disabled={saving}>
              取消
            </Button>
            <Button onClick={handleSave} disabled={saving}>
              {saving ? '保存中...' : '保存'}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  )
}

