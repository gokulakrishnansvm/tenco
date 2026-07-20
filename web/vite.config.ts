import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

// Dev proxy: forwards /auth and /api to the local Spring Boot backend on :8080,
// so the browser has no CORS issues during development.
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      "/api": { target: "http://localhost:8080", changeOrigin: true },
      "/auth": { target: "http://localhost:8080", changeOrigin: true },
    },
  },
});
