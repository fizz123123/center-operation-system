import axios from 'axios'

// 統一的 axios 實例：baseURL 固定打 "/api"
// 開發模式：vite.config.js 的 server.proxy 會把 /api 轉發到 http://localhost:8080（後端）
// 正式上線：前端 build 完會跟後端同源部署（見 vite.config.js 的 build.outDir），/api 就是同一台主機，不需要跨網域設定
export const http = axios.create({
  baseURL: '/api',
  timeout: 8000,                                             // 依照 06_test_strategy 的效能目標（一般查詢 3 秒內回應）保留寬限
})

/**
 * 呼叫真實 API，回傳資料本體（已經拆過 response.data，呼叫端不用再 .data）。
 * 失敗時把原本的 axios error 原封不動往外丟，交給呼叫端的 catch 區塊處理。
 *
 * @param {() => Promise<any>} requestFn   實際呼叫 axios 的函式，回傳 axios 的 Promise
 * @returns {Promise<any>}
 */
export async function callApi(requestFn) {
  const response = await requestFn()
  return response.data
}
