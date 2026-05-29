import { defineConfig, loadEnv } from "vite"
import react from "@vitejs/plugin-react"
import path from "path"

export default defineConfig(({ mode }) => {
  // 与 .env.development 中 VITE_*_API_TARGET 一致；仅 pnpm dev 时代理，不参与生产 build
  const env = loadEnv(mode, process.cwd(), "")

  const adminApiTarget =
    env.VITE_ADMIN_API_TARGET || env.ADMIN_API_TARGET || "http://127.0.0.1:6001"
  const contentApiTarget =
    env.VITE_CONTENT_API_TARGET || env.CONTENT_API_TARGET || "http://127.0.0.1:6002"
  const videoApiTarget =
    env.VITE_VIDEO_API_TARGET || env.VIDEO_API_TARGET || "http://127.0.0.1:8080"

  return {
    plugins: [react()],
    server: {
      host: true,
      port: 5173,
      strictPort: true,
      proxy: {
        "/admin-api": {
          target: adminApiTarget,
          changeOrigin: true,
        },
        "/memberPlans": {
          target: adminApiTarget,
          changeOrigin: true,
          rewrite: (p) => `/admin-api${p}`,
        },
        "/content-api": {
          target: contentApiTarget,
          changeOrigin: true,
          rewrite: (p) => p.replace(/^\/content-api/, "") || "/",
        },
        "/video-api": {
          target: videoApiTarget,
          changeOrigin: true,
          rewrite: (p) => p.replace(/^\/video-api/, "") || "/",
        },
      },
    },
    resolve: {
      alias: {
        "@": path.resolve(__dirname, "./src"),
      },
    },
    optimizeDeps: {
      include: ["ali-oss"],
    },
  }
})
