import axios from "axios"
import { setupAxiosAuth } from "@/api/axiosAuth"
import { resolveApiBase } from "@/config/apiBase"

/** 短剧等内容域 HTTP（dev：/content-api + Vite 代理；prod：api.dramadjbo.com） */
const baseURL = resolveApiBase(
  import.meta.env.VITE_CONTENT_API_BASE,
  "/content-api",
)

export const contentHttp = axios.create({
  baseURL,
  timeout: 30_000,
})

setupAxiosAuth(contentHttp)
