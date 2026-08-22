import { http, withMockFallback } from './http.js'
import { mockApi } from './mockData.js'

// 對應 docs/03_api_spec.md 第 6 節「Dashboard API」
// GET /api/dashboard －回傳 { totalPeople, totalCourses, totalEnrollments, completionRate }
export function getDashboardSummary() {
  return withMockFallback(
    () => http.get('/dashboard'),
    () => mockApi.getDashboardSummary(),
  )
}
