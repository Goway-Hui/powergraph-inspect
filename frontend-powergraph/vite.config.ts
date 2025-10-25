import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src')
    }
  },
  server: {
    port: 3000,
    proxy: {
      '/api': {
        // 将目标指向后端的 /api 前缀，保持 rewrite 去前缀
        target: 'http://127.0.0.1:8080/api',
        changeOrigin: true,
        // 仍然移除前端路径中的 /api，最终请求会变成 http://127.0.0.1:8080/api/xxx
        rewrite: (path) => path.replace(/^\/api/, '')
      },
      // 图谱相关接口不带 /api 前缀，单独配置代理
      '/graph': {
        target: 'http://127.0.0.1:8080',
        changeOrigin: true,
        rewrite: (path) => path // 保持原样
      }
    }
  }
})