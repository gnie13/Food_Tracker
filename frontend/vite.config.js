import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// Dev server proxies the API to the Spring Boot app on :8080 so the browser
// talks to a single origin.
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      // 127.0.0.1, not localhost: Node resolves localhost to ::1 first, which
      // the Spring Boot server's IPv4 bind refuses. Override with VITE_API_TARGET.
      '/api': process.env.VITE_API_TARGET || 'http://127.0.0.1:8080',
    },
  },
})
