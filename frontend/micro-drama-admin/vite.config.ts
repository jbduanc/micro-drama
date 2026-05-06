import { defineConfig } from "vite"
import react from "@vitejs/plugin-react"
import path from "path"

export default defineConfig(() => {
  // Local dev default: backend on host
  // Docker dev default: proxy to service name on app-network
  const adminApiTarget =
    process.env.VITE_ADMIN_API_TARGET ??
    process.env.ADMIN_API_TARGET ??
    "http://144.202.122.212:6001"

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
      },
    },
    resolve: {
      alias: {
        "@": path.resolve(__dirname, "./src"),
      },
    },
  }
})
