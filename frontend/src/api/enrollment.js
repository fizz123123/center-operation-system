import { http, withMockFallback } from './http.js'
import { mockApi } from './mockData.js'

// 對應 docs/03_api_spec.md 第 5 節「Enrollment API」

// POST /api/enrollments －學員註冊課程，payload: { personId, courseId }
export function createEnrollment(payload) {
  return withMockFallback(
    () => http.post('/enrollments', payload),
    () => mockApi.createEnrollment(payload),
  )
}

// PUT /api/enrollments/{id} －更新學習狀態，body: { status }
// status 只能是 NOT_STARTED / IN_PROGRESS / COMPLETED 三種（docs/02_database_design.md 列舉值）
export function updateEnrollmentStatus(id, status) {
  return withMockFallback(
    () => http.put(`/enrollments/${id}`, { status }),
    () => mockApi.updateEnrollmentStatus(id, status),
  )
}
