import axios from "axios"
import { setupAxiosAuth } from "@/api/axiosAuth"
import { resolveApiBase } from "@/config/apiBase"

export const http = axios.create({
  baseURL: resolveApiBase(import.meta.env.VITE_ADMIN_API_BASE, "/admin-api"),
  timeout: 30_000,
})

setupAxiosAuth(http)
