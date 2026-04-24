import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import path from "path";

export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      /**
       * Backend config:
       *   server.port = 8080
       *   server.servlet.context-path = /admin-api
       *
       * Frontend calls:
       *   /memberPlans/**
       *
       * Proxy to:
       *   http://localhost:8080/admin-api/memberPlans/**
       */
      "/memberPlans": {
        target: "http://localhost:8080",
        changeOrigin: true,
        rewrite: (p) => `/admin-api${p}`,
      },

      /**
       * Optional: if frontend later uses /admin-api/** directly.
       */
      "/admin-api": {
        target: "http://localhost:8080",
        changeOrigin: true,
      },
    },
  },
  resolve: {
    alias: {
      "@": path.resolve(__dirname, "./src"),
    },
  },
});
