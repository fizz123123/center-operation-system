import { http, withMockFallback } from './http.js'
import { mockApi } from './mockData.js'

// 對應 docs/03_api_spec.md 第 4 節「Course API」與第 7 節「Course Graph API」

// GET /api/courses －查詢課程清單（分頁）
// params: { page（0-indexed）, size, sort（例如 "code,asc"）, search（比對代碼/名稱） }
// 回傳 Page<CourseResponse>：{ content, totalElements, totalPages, number, size }
// 需要「全部課程」的地方（例如先修課程下拉選單、學習路徑頁）自己傳大一點的 size（例如 500）涵蓋預期最大筆數，
// 不需要另外開一支「查全部」的 API。
export function getCourses(params = {}) {
  return withMockFallback(
    () => http.get('/courses', { params }),
    () => mockApi.getCourses(params),
  )
}

// POST /api/courses －新增課程，payload 對應 CourseCreateRequest { code, name, description }
export function createCourse(payload) {
  return withMockFallback(
    () => http.post('/courses', payload),
    () => mockApi.createCourse(payload),
  )
}

// PUT /api/courses/{id} －修改課程
export function updateCourse(id, payload) {
  return withMockFallback(
    () => http.put(`/courses/${id}`, payload),
    () => mockApi.updateCourse(id, payload),
  )
}

// POST /api/courses/{courseId}/prerequisites －建立課程先修關係，body: { prerequisiteId }
// 語意：courseId 這門課「需要先修完」prerequisiteId 那門課，這條關係就是 CourseGraph 的一條邊
export function addPrerequisite(courseId, prerequisiteId) {
  return withMockFallback(
    () => http.post(`/courses/${courseId}/prerequisites`, { prerequisiteId }),
    () => mockApi.addPrerequisite(courseId, prerequisiteId),
  )
}

// GET /api/courses/learning-path －取得拓樸排序後的課程學習路徑（BFS/DFS/Topological Sort 展示用）
export function getLearningPath() {
  return withMockFallback(
    () => http.get('/courses/learning-path'),
    () => mockApi.getLearningPath(),
  )
}
