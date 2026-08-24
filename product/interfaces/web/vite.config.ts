/// <reference types="vitest/config" />
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'
import { resolve } from 'node:path'

export default defineConfig({
  plugins: [react(), tailwindcss()],
  build: {
    // Plain `npm run build` (used by CI's frontend job and `./arcogine check`)
    // writes to the local, gitignored default outDir. Only `./arcogine build`
    // sets ARCOGINE_DIST_WEB to stage output into the canonical dist/web/, so
    // that top-level dist/ is only ever mutated atomically by `./arcogine build`.
    outDir: process.env.ARCOGINE_DIST_WEB
      ? resolve(import.meta.dirname, process.env.ARCOGINE_DIST_WEB)
      : 'dist',
    emptyOutDir: true,
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:3000',
        changeOrigin: true,
      },
    },
  },
  test: {
    environment: 'jsdom',
    globals: true,
    setupFiles: ['./src/test/setup.ts'],
    include: ['src/**/*.test.{ts,tsx}'],
  },
})
