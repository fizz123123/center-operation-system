import { defineConfig } from 'vite'                        // Vite 設定檔輔助函式，提供型別提示
import vue from '@vitejs/plugin-vue'                        // 讓 Vite 認得 .vue 單一檔案元件

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],                                         // 啟用 Vue SFC 編譯

  server: {
    proxy: {
      // 開發模式下，前端呼叫 /api/xxx 會被轉發到後端 Spring Boot（預設 8080 埠）
      // 好處：前端程式碼永遠只認識 "/api"，正式上線改由 Spring Boot 直接同源提供靜態檔，不需要再改程式碼
      '/api': {
        target: 'http://localhost:8080',                    // 後端 Spring Boot 服務位址（Member A 負責）
        changeOrigin: true,                                  // 避免後端因 Host 標頭不同而拒絕請求
      },
    },
  },

  build: {
    // 依照專案 docs/04_project_architecture.md 第 15 節：
    // 前端最終需以靜態檔案形式放在 Spring Boot 的 src/main/resources/static 下（單體部署、不需處理 CORS）
    outDir: '../src/main/resources/static',
    emptyOutDir: true,                                       // 每次 build 前清空舊的靜態檔，避免殘留過期檔案
  },
})
