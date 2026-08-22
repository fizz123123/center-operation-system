import { http, callApi } from './http.js'

// 對應 docs/03_api_spec.md 第 5 節「Enrollment API」

// POST /api/enrollments －學員註冊課程，payload: { personId, courseId }
export function createEnrollment(payload) {
  return callApi(() => http.post('/enrollments', payload))
}

// PUT /api/enrollments/{id} －更新學習狀態，body: { status }
// status 只能是 NOT_STARTED / IN_PROGRESS / COMPLETED 三種（docs/02_database_design.md 列舉值）
export function updateEnrollmentStatus(id, status) {
  return callApi(() => http.put(`/enrollments/${id}`, { status }))
}
