import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// Dev server proxies the API to the Spring Boot app on :8080 so the browser
// talks to a single origin.
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': 'http://localhost:8080',
    },
  },
})
