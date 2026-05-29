import axios from "axios"
import { setupAxiosAuth } from "@/api/axiosAuth"
import { resolveApiBase } from "@/config/apiBase"

/** 视频域 HTTP（dev：/video-api + Vite 代理；prod：api.dramadjbo.com） */
const baseURL = resolveApiBase(import.meta.env.VITE_VIDEO_API_BASE, "/video-api")

export const videoHttp = axios.create({
  baseURL,
  timeout: 120_000,
})

setupAxiosAuth(videoHttp)
