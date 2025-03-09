import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  server: {
    proxy: {
      '^/images': {
        target: 'http://localhost:8181' // Spring boot backend address
      }
    }
  },
  build: {
    outDir: 'target/dist',
    assetsDir: 'static'
  }
})


