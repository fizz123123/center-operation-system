import { http, callApi } from './http.js'

// 對應 docs/03_api_spec.md 第 3 節「Person API」
// 每支函式只負責一件事：組出對的 HTTP method + 路徑，回傳資料本體。
// 頁面元件（pages/PersonListPage.vue）不會直接 import axios，一律透過這裡呼叫，
// 這樣未來如果要改路徑、加 header，只要改這一個檔案。

// GET /api/people －查詢人員清單（分頁）
// params: { page（0-indexed）, size, sort（例如 "name,asc"）, search（比對姓名/Email）, status（ACTIVE/INACTIVE） }
// 回傳 Spring Data 的 Page<PersonResponse> 形狀：{ content, totalElements, totalPages, number, size }
// 想拿到「全部」的呼叫端（例如下拉選單需要完整清單），可以自己傳一個夠大的 size（例如 500）涵蓋預期的最大筆數。
export function getPeople(params = {}) {
  return callApi(() => http.get('/people', { params }))
}

// GET /api/people/{id} －查詢單一人員
export function getPerson(id) {
  return callApi(() => http.get(`/people/${id}`))
}

// POST /api/people －新增人員，payload 對應 PersonCreateRequest { name, email, phone }
export function createPerson(payload) {
  return callApi(() => http.post('/people', payload))
}

// PUT /api/people/{id} －修改人員
export function updatePerson(id, payload) {
  return callApi(() => http.put(`/people/${id}`, payload))
}

// GET /api/people/{personId}/enrollments －查詢該學員的學習紀錄（分頁，Module 3 也會用到）
// 回傳格式同 getPeople()：Page<EnrollmentResponse>
export function getPersonEnrollments(personId, params = {}) {
  return callApi(() => http.get(`/people/${personId}/enrollments`, { params }))
}
