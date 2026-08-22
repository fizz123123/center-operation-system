import { http, callApi } from './http.js'

// 對應 docs/03_api_spec.md 第 6 節「Dashboard API」
// GET /api/dashboard －回傳 { totalPeople, totalCourses, totalEnrollments, completionRate }
export function getDashboardSummary() {
  return callApi(() => http.get('/dashboard'))
}
