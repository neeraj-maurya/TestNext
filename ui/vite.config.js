import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// Vite config with a dev proxy so the UI can call the Spring Boot backend at localhost:8080
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false
      }
    }
  }
})
