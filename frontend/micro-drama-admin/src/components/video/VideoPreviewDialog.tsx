import Hls from "hls.js"
import { Loader2 } from "lucide-react"
import { useEffect, useRef, useState } from "react"

import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog"

type VideoPreviewDialogProps = {
  open: boolean
  onOpenChange: (open: boolean) => void
  title: string
  playUrl: string
  loading?: boolean
  error?: string
}

export function VideoPreviewDialog({
  open,
  onOpenChange,
  title,
  playUrl,
  loading = false,
  error = "",
}: VideoPreviewDialogProps) {
  const videoRef = useRef<HTMLVideoElement>(null)
  const [playerError, setPlayerError] = useState("")

  useEffect(() => {
    if (!open) {
      setPlayerError("")
    }
  }, [open])

  useEffect(() => {
    if (!open || loading || error.trim() || !playUrl.trim()) {
      return
    }

    let cancelled = false
    let hls: Hls | null = null
    let raf = 0

    const cleanup = () => {
      if (hls) {
        hls.destroy()
        hls = null
      }
      const video = videoRef.current
      if (video) {
        video.pause()
        video.removeAttribute("src")
        video.load()
      }
    }

    const attach = () => {
      if (cancelled) return
      const video = videoRef.current
      if (!video) {
        raf = requestAnimationFrame(attach)
        return
      }

      setPlayerError("")

      if (Hls.isSupported()) {
        hls = new Hls({ enableWorker: true })
        hls.loadSource(playUrl)
        hls.attachMedia(video)
        hls.on(Hls.Events.MANIFEST_PARSED, () => {
          void video.play().catch(() => {})
        })
        hls.on(Hls.Events.ERROR, (_event, data) => {
          if (data.fatal) {
            setPlayerError("HLS 加载失败，请检查分片是否可访问（私有桶需配置读权限）")
            cleanup()
          }
        })
        return
      }

      if (video.canPlayType("application/vnd.apple.mpegurl")) {
        video.src = playUrl
        void video.play().catch(() => {})
        return
      }

      setPlayerError("当前浏览器不支持 HLS 播放")
    }

    raf = requestAnimationFrame(attach)

    return () => {
      cancelled = true
      cancelAnimationFrame(raf)
      cleanup()
    }
  }, [open, playUrl, loading, error])

  const showVideo = !loading && !error.trim() && playUrl.trim()

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="z-[100] max-w-3xl gap-4 sm:max-w-3xl">
        <DialogHeader>
          <DialogTitle>{title}</DialogTitle>
          <DialogDescription>预览转码后的 HLS 流（管理端）</DialogDescription>
        </DialogHeader>

        {loading && (
          <div className="flex aspect-video items-center justify-center rounded-lg bg-muted">
            <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
            <span className="sr-only">正在获取播放地址</span>
          </div>
        )}

        {!loading && error.trim() && (
          <div className="rounded-lg border border-destructive/30 bg-destructive/10 px-4 py-3 text-sm text-destructive">
            {error}
          </div>
        )}

        {showVideo && (
          <>
            <video
              ref={videoRef}
              controls
              playsInline
              className="aspect-video w-full rounded-lg bg-black"
            />
            {playerError && (
              <p className="text-sm text-destructive">{playerError}</p>
            )}
          </>
        )}
      </DialogContent>
    </Dialog>
  )
}
