import { http, withMockFallback } from './http.js'
import { mockApi } from './mockData.js'

// 對應 docs/03_api_spec.md 第 8 節「Alert API」
// GET /api/alerts －查詢警示清單（分頁），後端用 MaxHeap 依 priority（3 高 / 2 中 / 1 低）排序後回傳
// params: { page（0-indexed）, size, priority（只看單一優先權時傳 '1'/'2'/'3'） }
// 回傳 Page<AlertResponse>：{ content, totalElements, totalPages, number, size }
export function getAlerts(params = {}) {
  return withMockFallback(
    () => http.get('/alerts', { params }),
    () => mockApi.getAlerts(params),
  )
}
