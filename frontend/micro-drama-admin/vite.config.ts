import { defineConfig } from "vite"
import react from "@vitejs/plugin-react"
import path from "path"

export default defineConfig(() => {
  // Local dev default: backend on host
  // Docker dev default: proxy to service name on app-network
  const adminApiTarget =
    process.env.VITE_ADMIN_API_TARGET ??
    process.env.ADMIN_API_TARGET ??
    "http://47.84.207.243:6001"

  const contentApiTarget =
    process.env.VITE_CONTENT_API_TARGET ??
    process.env.CONTENT_API_TARGET ??
    "http://127.0.0.1:6002"

  return {
    plugins: [react()],
    server: {
      host: true,
      port: 5173,
      strictPort: true,
      proxy: {
        // Main path used by axios baseURL: "/admin-api"
        "/admin-api": {
          target: adminApiTarget,
          changeOrigin: true,
        },

        // Backward-compat: old relative calls like "/memberPlans/**"
        "/memberPlans": {
          target: adminApiTarget,
          changeOrigin: true,
          rewrite: (p) => `/admin-api${p}`,
        },

        // 短剧管理：直连 micro-drama-content（与 axios contentHttp baseURL `/content-api` 对应）
        "/content-api": {
          target: contentApiTarget,
          changeOrigin: true,
          rewrite: (p) => p.replace(/^\/content-api/, "") || "/",
        },
      },
    },
    resolve: {
      alias: {
        "@": path.resolve(__dirname, "./src"),
      },
    },
  }
})
