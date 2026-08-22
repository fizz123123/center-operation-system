import axios from 'axios'
import { useApiStatusStore } from '../stores/apiStatus.js'

// 統一的 axios 實例：baseURL 固定打 "/api"
// 開發模式：vite.config.js 的 server.proxy 會把 /api 轉發到 http://localhost:8080（後端）
// 正式上線：前端 build 完會跟後端同源部署（見 vite.config.js 的 build.outDir），/api 就是同一台主機，不需要跨網域設定
export const http = axios.create({
  baseURL: '/api',
  timeout: 8000,                                             // 依照 06_test_strategy 的效能目標（一般查詢 3 秒內回應）保留寬限
})

/**
 * 判斷這個錯誤「是不是後端真的有處理、只是回傳業務錯誤」。
 *
 * 依 docs/03_api_spec.md 第 11 節，後端的錯誤回應一定長這樣：
 *   { "timestamp": "...", "status": 400, "message": "Email already exists" }
 * 只要 error.response.data 符合這個形狀（至少有 message 字串），就代表：
 *   - 後端真的有在跑
 *   - 這支 API 真的有處理這個請求
 *   - 只是依業務邏輯判斷這筆資料不合法（例如 Email 重複）
 * 這種情況「不應該」被當成後端不存在、改用假資料頂著，那樣會讓畫面看起來像成功了，
 * 但實際上資料庫根本沒有真的寫進去，跟畫面顯示的內容不一致。
 *
 * 反過來，如果是 timeout、網路斷線、或是開發模式下 vite proxy 打不到後端時回的 502
 * （這種 502 是 vite 自己產生的錯誤頁，不是後端 controller 回的，data 不會符合上面的形狀），
 * 才代表「後端真的不存在／還沒做好」，這時候才適合 fallback 用假資料。
 */
function isRealBackendError(error) {
  const data = error?.response?.data
  return Boolean(data && typeof data === 'object' && typeof data.message === 'string')
}

/**
 * 呼叫真實 API；只有在「後端真的不存在／連不上」時才 fallback 用假資料。
 * 如果後端有回應、只是業務邏輯判斷這筆資料不合法（例如 400 Email 重複），
 * 會把原本的 axios error 原封不動往外丟，讓呼叫端的 catch 區塊照正常錯誤處理流程顯示真正的錯誤訊息，
 * 不會被誤判成「後端不存在」而悄悄改用假資料、讓畫面看起來成功。
 *
 * @param {() => Promise<any>} requestFn   實際呼叫 axios 的函式，回傳 axios 的 Promise
 * @param {() => any} mockFn               對應的假資料函式（來自 api/mockData.js 的 mockApi）
 * @returns {Promise<any>}                 統一回傳「資料本體」（已經拆過 response.data，呼叫端不用再 .data）
 */
export async function withMockFallback(requestFn, mockFn) {
  const apiStatus = useApiStatusStore()
  try {
    const response = await requestFn()
    apiStatus.reportRealApiSuccess()                          // 成功打到真的後端，把 Navbar 上的假資料提示收起來
    return response.data
  } catch (error) {
    if (isRealBackendError(error)) {
      // 後端真的有回應、真的處理過這個請求，只是判定失敗——這是「真的錯誤」，直接往外丟，
      // 不能 fallback 用假資料，不然畫面會顯示成功，但資料庫其實沒有真的更新，兩邊會兜不起來。
      apiStatus.reportRealApiSuccess()                        // 這也代表後端是活的，一樣把假資料提示收起來
      throw error
    }
    console.warn('[API fallback] 連不到後端（非業務錯誤），改用本地假資料展示：', error?.message ?? error)
    apiStatus.reportMockFallback(error?.message ?? '未知錯誤')
    return mockFn()
  }
}
