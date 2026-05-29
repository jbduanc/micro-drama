import Hls from "hls.js"
import { useEffect, useRef } from "react"

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
  playUrl: string
  title: string
}

export function VideoPreviewDialog({
  open,
  onOpenChange,
  playUrl,
  title,
}: VideoPreviewDialogProps) {
  const videoRef = useRef<HTMLVideoElement>(null)

  useEffect(() => {
    if (!open || !playUrl.trim()) return

    const video = videoRef.current
    if (!video) return

    let hls: Hls | null = null

    const cleanup = () => {
      if (hls) {
        hls.destroy()
        hls = null
      }
      video.pause()
      video.removeAttribute("src")
      video.load()
    }

    if (Hls.isSupported()) {
      hls = new Hls({ enableWorker: true })
      hls.loadSource(playUrl)
      hls.attachMedia(video)
      hls.on(Hls.Events.MANIFEST_PARSED, () => {
        void video.play().catch(() => {})
      })
      hls.on(Hls.Events.ERROR, (_event, data) => {
        if (data.fatal) {
          cleanup()
        }
      })
      return cleanup
    }

    if (video.canPlayType("application/vnd.apple.mpegurl")) {
      video.src = playUrl
      void video.play().catch(() => {})
      return cleanup
    }

    return cleanup
  }, [open, playUrl])

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-3xl gap-4">
        <DialogHeader>
          <DialogTitle>{title}</DialogTitle>
          <DialogDescription>预览转码后的 HLS 流（管理端）</DialogDescription>
        </DialogHeader>
        <video
          ref={videoRef}
          controls
          playsInline
          className="aspect-video w-full rounded-lg bg-black"
        />
      </DialogContent>
    </Dialog>
  )
}
