/**
 * 開發階段的「假資料」來源。
 *
 * 為什麼需要這個檔案：
 * 後端（Member A）目前只有 Spring Boot 專案骨架，Controller/Service/Repository 都還沒實作，
 * 所以呼叫 /api/... 現在一定會失敗。如果前端要等後端全部做完才能開始刻頁面，時間會來不及。
 * 所以這裡先照 docs/03_api_spec.md 訂的資料格式，準備一份記憶體內的假資料，
 * 讓每個頁面「現在就能被打開、被操作、被口試展示」，等後端 API 好了會自動切換成真資料（見 api/http.js）。
 *
 * 這份假資料全部存在瀏覽器記憶體裡，重新整理頁面就會重置，這是預期行為（純前端假資料本來就不會永久保存）。
 */

// ---- 內部小工具：從 1 開始遞增產生新 id，並用一個假的 ISO 時間字串模擬 created_at ----
function makeIdCounter(startAt) {
  let current = startAt
  return () => ++current
}

// =====================================================================================
// 人員（Person）－ Module 1
// 從 5 筆擴充到 20 筆：主要是為了在畫面上實際看到分頁效果（DataTable 預設一頁 10 筆，
// 20 筆會出現 2 頁），也讓之後跟後端討論「200 筆測試資料要不要做分頁 API」之前，
// 先在前端假資料上有個更接近真實資料量的畫面可以看。前 5 筆維持原本內容不動，
// 因為 mockAlerts／Dashboard 下面的假警示文字有直接點名「陳雅婷」「王志豪」，改掉會對不起來。
// =====================================================================================
export const mockPeople = [
  { id: 1, name: '林俊傑', email: 'brian.lin@example.com', phone: '0912-345-678', status: 'ACTIVE' },
  { id: 2, name: '陳雅婷', email: 'yating.chen@example.com', phone: '0922-111-222', status: 'ACTIVE' },
  { id: 3, name: '王志豪', email: 'chihhao.wang@example.com', phone: '0933-222-333', status: 'ACTIVE' },
  { id: 4, name: '黃詩涵', email: 'shihhan.huang@example.com', phone: '0955-444-555', status: 'INACTIVE' },
  { id: 5, name: '李冠廷', email: 'kuanting.lee@example.com', phone: '0966-555-666', status: 'ACTIVE' },
  { id: 6, name: '張家豪', email: 'jiahao.zhang@example.com', phone: '0911-222-333', status: 'ACTIVE' },
  { id: 7, name: '林芳如', email: 'fangru.lin@example.com', phone: '0922-333-444', status: 'ACTIVE' },
  { id: 8, name: '吳建宏', email: 'jianhong.wu@example.com', phone: '0933-444-555', status: 'ACTIVE' },
  { id: 9, name: '蔡欣怡', email: 'hsinyi.tsai@example.com', phone: '0944-555-666', status: 'INACTIVE' },
  { id: 10, name: '許博文', email: 'bowen.hsu@example.com', phone: '0955-666-777', status: 'ACTIVE' },
  { id: 11, name: '楊淑芬', email: 'shufen.yang@example.com', phone: '0966-777-888', status: 'ACTIVE' },
  { id: 12, name: '郭彥廷', email: 'yanting.kuo@example.com', phone: '0977-888-999', status: 'ACTIVE' },
  { id: 13, name: '何佩珊', email: 'peishan.ho@example.com', phone: '0988-999-000', status: 'ACTIVE' },
  { id: 14, name: '賴俊宏', email: 'junhong.lai@example.com', phone: '0912-111-222', status: 'INACTIVE' },
  { id: 15, name: '周美玲', email: 'meiling.chou@example.com', phone: '0923-222-333', status: 'ACTIVE' },
  { id: 16, name: '徐志明', email: 'chihming.hsu@example.com', phone: '0934-333-444', status: 'ACTIVE' },
  { id: 17, name: '潘怡君', email: 'yichun.pan@example.com', phone: '0945-444-555', status: 'ACTIVE' },
  { id: 18, name: '曾國強', email: 'kuochiang.tseng@example.com', phone: '0956-555-666', status: 'ACTIVE' },
  { id: 19, name: '高雅慧', email: 'yahui.kao@example.com', phone: '0967-666-777', status: 'INACTIVE' },
  { id: 20, name: '江柏翰', email: 'bohan.chiang@example.com', phone: '0978-777-888', status: 'ACTIVE' },
]
const nextPersonId = makeIdCounter(mockPeople.length)

// =====================================================================================
// 課程（Course）－ Module 2
// prerequisiteIds：課程本身的先修課程 id 清單。
// 這個欄位不是 docs/03_api_spec.md 定義的 CourseResponse 欄位（規格書只有新增先修關係的 POST API，
// 沒有定義「查詢某課程先修清單」的 GET API），所以先用前端本地資料模擬顯示，
// 等 Member A 補上對應的查詢端點後，這裡改成直接吃後端回傳值即可，UI 不用改。
// =====================================================================================
export const mockCourses = [
  { id: 1, code: 'JAVA01', name: 'Java Basic', description: 'Java 語言基礎語法與物件導向入門', prerequisiteIds: [] },
  { id: 2, code: 'OOP01', name: 'OOP 物件導向設計', description: '封裝、繼承、多型與設計原則', prerequisiteIds: [1] },
  { id: 3, code: 'DS01', name: 'Data Structure 資料結構', description: 'HashTable、Graph、Heap 等常見資料結構', prerequisiteIds: [2] },
  { id: 4, code: 'ALG01', name: 'Algorithm 演算法', description: 'BFS/DFS、排序、拓樸排序等核心演算法', prerequisiteIds: [3] },
  { id: 5, code: 'DB01', name: 'Database 資料庫', description: '關聯式資料庫設計與 SQL 查詢', prerequisiteIds: [1] },
]
const nextCourseId = makeIdCounter(mockCourses.length)

// =====================================================================================
// 學習紀錄（Enrollment）－ Module 3
// personId + courseId 對應到上面 mockPeople / mockCourses 的 id
//
// 前 6 筆是原本手寫的資料，跟 Dashboard 假警示文字（陳雅婷、王志豪那幾則）對得上，維持不動；
// id 7 開始是配合人員擴充到 20 筆新補的，用小迴圈依人員 id 輪流分配課程／狀態，
// 只是為了讓畫面有足夠的資料量可以測分頁，不是真的演算法邏輯，所以用簡單的迴圈產生就好，
// 不需要為了「假資料」也手動一筆一筆刻。
// =====================================================================================
export const mockEnrollments = [
  { id: 1, personId: 1, courseId: 1, courseName: 'Java Basic', status: 'COMPLETED' },
  { id: 2, personId: 1, courseId: 2, courseName: 'OOP 物件導向設計', status: 'IN_PROGRESS' },
  { id: 3, personId: 2, courseId: 1, courseName: 'Java Basic', status: 'COMPLETED' },
  { id: 4, personId: 2, courseId: 5, courseName: 'Database 資料庫', status: 'NOT_STARTED' },
  { id: 5, personId: 3, courseId: 1, courseName: 'Java Basic', status: 'IN_PROGRESS' },
  { id: 6, personId: 5, courseId: 3, courseName: 'Data Structure 資料結構', status: 'NOT_STARTED' },
]

// 人員 4（原本沒有任何學習紀錄）+ 人員 6~20（新增的 15 位）各補 1~3 筆學習紀錄
const ENROLLMENT_STATUS_CYCLE = ['COMPLETED', 'IN_PROGRESS', 'NOT_STARTED']
let generatedEnrollmentId = mockEnrollments.length + 1
mockPeople
  .filter((p) => p.id === 4 || p.id >= 6)
  .forEach((person, idx) => {
    const courseCount = (idx % 3) + 1                          // 1~3 門課輪流，數量不會每個人都一樣
    for (let i = 0; i < courseCount; i++) {
      const course = mockCourses[(idx + i) % mockCourses.length]
      mockEnrollments.push({
        id: generatedEnrollmentId++,
        personId: person.id,
        courseId: course.id,
        courseName: course.name,
        status: ENROLLMENT_STATUS_CYCLE[(idx + i) % ENROLLMENT_STATUS_CYCLE.length],
      })
    }
  })

const nextEnrollmentId = makeIdCounter(mockEnrollments.length)

// =====================================================================================
// 警示（Alert）－ Module 5：MaxHeap 依 priority 由高到低排序後的展示結果
// priority：3 = HIGH、2 = MEDIUM、1 = LOW（對應 docs/03_api_spec.md 第 8 節）
// =====================================================================================
export const mockAlerts = [
  { id: 1, priority: 3, message: '陳雅婷已 30 天未更新任何課程進度' },
  { id: 2, priority: 3, message: 'Data Structure 資料結構課程完成率低於 40%' },
  { id: 3, priority: 2, message: '王志豪的 Java Basic 課程進行中已超過預期時間' },
  { id: 4, priority: 1, message: 'Database 資料庫課程新增一筆先修關係，建議確認學習路徑' },
]

// =====================================================================================
// 各種「假資料版」的 API 實作
// 命名跟真正的 api/person.js 等模組一一對應，這樣 http.js 才能無痛切換兩邊
// =====================================================================================

function clone(value) {
  return JSON.parse(JSON.stringify(value))                  // 回傳資料的複本，避免外部直接改到內部陣列
}

// =====================================================================================
// 分頁／搜尋／排序／篩選的假資料模擬
//
// 為什麼要在假資料這層也做這件事：Person/Course/Enrollment/Alert 這幾份清單之後會測到 200 筆以上，
// 分頁、搜尋、排序都改成後端做（同學會補上對應的 API），前端不再自己抓全部資料回來處理。
// 在後端 API 真的做好之前，這裡的假資料也要模擬同一套行為（吃同樣的 page/size/sort/search 參數，
// 回傳同樣格狀的分頁結果），這樣切換到真的後端時，畫面邏輯完全不用改，只是資料來源換掉而已。
//
// 回傳格式對應 Spring Data 的 Page<T>（同學那邊如果直接讓 Controller 回傳 Page<T>，
// Spring Boot 序列化出來就是這個形狀，前端不用再另外包一層）：
//   { content, totalElements, totalPages, number, size }
// page 是 0-indexed（配合 Spring Data 的慣例），呼叫端自己把畫面上「第幾頁」-1 轉換過去。
function paginateList(list, { page = 0, size = 10, sort = '', search = '', searchFields = [], filters = {} } = {}) {
  let result = list

  // 篩選：filters 是 { 欄位名: 期望值 } 的組合，例如 { status: 'ACTIVE' }；值是空字串/undefined 就跳過不篩
  for (const [field, value] of Object.entries(filters)) {
    if (value === undefined || value === null || value === '') continue
    result = result.filter((item) => String(item[field]) === String(value))
  }

  // 搜尋：search 對 searchFields 列出的欄位做模糊比對（不分大小寫），符合其中一個欄位就算命中
  if (search && search.trim()) {
    const q = search.trim().toLowerCase()
    result = result.filter((item) => searchFields.some((field) => String(item[field] ?? '').toLowerCase().includes(q)))
  }

  // 排序：sort 格式是「欄位,方向」（例如 "name,asc"），對應 Spring Data Pageable 的 sort 參數格式
  if (sort) {
    const [field, dir] = sort.split(',')
    const mul = dir === 'desc' ? -1 : 1
    result = [...result].sort((a, b) => {
      const av = a[field]
      const bv = b[field]
      if (av == null) return 1
      if (bv == null) return -1
      return av > bv ? mul : av < bv ? -mul : 0
    })
  }

  const totalElements = result.length
  const totalPages = Math.max(1, Math.ceil(totalElements / size))
  const start = page * size
  return {
    content: result.slice(start, start + size),
    totalElements,
    totalPages,
    number: page,
    size,
  }
}

export const mockApi = {
  // ---- Person ----
  getPeople: (params = {}) => {
    const { page = 0, size = 10, sort = '', search = '', status = '' } = params
    return clone(paginateList(mockPeople, { page, size, sort, search, searchFields: ['name', 'email'], filters: { status } }))
  },
  getPerson: (id) => clone(mockPeople.find((p) => p.id === Number(id))),
  createPerson: (payload) => {
    const created = { id: nextPersonId(), status: 'ACTIVE', ...payload }
    mockPeople.push(created)
    return clone(created)
  },
  updatePerson: (id, payload) => {
    const target = mockPeople.find((p) => p.id === Number(id))
    if (!target) throw new Error('找不到指定人員')
    Object.assign(target, payload)
    return clone(target)
  },
  getPersonEnrollments: (personId, params = {}) => {
    const { page = 0, size = 10, sort = '' } = params
    const personEnrollments = mockEnrollments.filter((e) => e.personId === Number(personId))
    return clone(paginateList(personEnrollments, { page, size, sort }))
  },

  // ---- Course ----
  getCourses: (params = {}) => {
    const { page = 0, size = 10, sort = '', search = '' } = params
    return clone(paginateList(mockCourses, { page, size, sort, search, searchFields: ['code', 'name'] }))
  },
  createCourse: (payload) => {
    const created = { id: nextCourseId(), prerequisiteIds: [], ...payload }
    mockCourses.push(created)
    return clone(created)
  },
  updateCourse: (id, payload) => {
    const target = mockCourses.find((c) => c.id === Number(id))
    if (!target) throw new Error('找不到指定課程')
    Object.assign(target, payload)
    return clone(target)
  },
  addPrerequisite: (courseId, prerequisiteId) => {
    const target = mockCourses.find((c) => c.id === Number(courseId))
    if (!target) throw new Error('找不到指定課程')
    if (!target.prerequisiteIds.includes(Number(prerequisiteId))) {
      target.prerequisiteIds.push(Number(prerequisiteId))
    }
    return clone(target)
  },
  // 拓樸排序（Topological Sort）、Graph 走訪（BFS/DFS）屬於資料結構與演算法模組的範圍，
  // 依 docs/05_git_task_breakdown.md 第 4 節是「共同完成」的部分，不是前端該實作的地方；
  // 之前這裡自己寫過一版 DFS 排序，等於在假資料裡重做了演算法模組的工作、而且沒有處理循環先修的例外狀況，
  // 容易在口試時混淆「這段排序邏輯到底是誰寫的」。現在改成固定的示範結果（純資料，沒有任何排序運算），
  // 前端只負責「顯示」GET /api/courses/learning-path 回傳的陣列，真正的排序完全交給後端的 CourseGraph + TopologicalSort。
  getLearningPath: () => ['Java Basic', 'OOP 物件導向設計', 'Data Structure 資料結構', 'Algorithm 演算法', 'Database 資料庫'],

  // ---- Enrollment ----
  createEnrollment: (payload) => {
    const course = mockCourses.find((c) => c.id === Number(payload.courseId))
    const created = {
      id: nextEnrollmentId(),
      personId: Number(payload.personId),
      courseId: Number(payload.courseId),
      courseName: course?.name ?? '未知課程',
      status: 'NOT_STARTED',
    }
    mockEnrollments.push(created)
    return clone(created)
  },
  updateEnrollmentStatus: (id, status) => {
    const target = mockEnrollments.find((e) => e.id === Number(id))
    if (!target) throw new Error('找不到指定學習紀錄')
    target.status = status
    return clone(target)
  },

  // ---- Dashboard ----
  getDashboardSummary: () => {
    const total = mockEnrollments.length
    const completed = mockEnrollments.filter((e) => e.status === 'COMPLETED').length
    return {
      totalPeople: mockPeople.length,
      totalCourses: mockCourses.length,
      totalEnrollments: total,
      completionRate: total === 0 ? 0 : Number(((completed / total) * 100).toFixed(1)),
    }
  },

  // ---- Alert ----
  getAlerts: (params = {}) => {
    const { page = 0, size = 10, priority = '' } = params
    // 先依 priority 由高到低排序模擬 MaxHeap 輸出順序，再交給 paginateList 篩選/切頁
    // （不傳 sort 參數給 paginateList，這樣它就不會用別的排序方式蓋掉這裡先排好的順序）
    const sorted = [...mockAlerts].sort((a, b) => b.priority - a.priority)
    return clone(paginateList(sorted, { page, size, filters: { priority } }))
  },
}
