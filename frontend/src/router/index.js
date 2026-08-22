import { createRouter, createWebHashHistory } from 'vue-router'

// 路由對應表：一個路徑 (path) 對應一個「頁面」元件（pages/ 目錄下）
// 元件用動態 import()，Vite 會自動把每個頁面切成獨立的 chunk，
// 使用者第一次進站只會下載 Dashboard 的程式碼，切到「課程管理」時才會另外載入，加快首次進站速度。
const routes = [
  {
    path: '/',
    name: 'dashboard',
    component: () => import('../pages/DashboardPage.vue'),
    meta: { title: 'Dashboard 總覽' },                       // 給 Navbar 顯示目前頁面標題用
  },
  {
    path: '/people',
    name: 'people',
    component: () => import('../pages/PersonListPage.vue'),
    meta: { title: '人員管理' },                              // Module 1
  },
  {
    path: '/people/:id',
    name: 'person-detail',
    component: () => import('../pages/PersonDetailPage.vue'),
    meta: { title: '學習記錄管理' },                          // Module 1 + Module 3：從人員詳情頁直接修課，標題改成反映這裡也是學習紀錄的管理入口
  },
  {
    path: '/courses',
    name: 'courses',
    component: () => import('../pages/CourseListPage.vue'),
    meta: { title: '課程管理' },                              // Module 2（含先修課程設定）
  },
  {
    path: '/learning-path',
    name: 'learning-path',
    component: () => import('../pages/LearningPathPage.vue'),
    meta: { title: '課程學習路徑' },                          // Module 5：Graph + 拓樸排序視覺化
  },
  {
    path: '/alerts',
    name: 'alerts',
    component: () => import('../pages/AlertsPage.vue'),
    meta: { title: '警示列表' },                              // Module 5：Heap 優先權排序的完整列表頁
  },
]

const router = createRouter({
  // 用 Hash 模式（網址會有 #）而不是 History 模式：
  // 前端最後是打包進 Spring Boot 的 src/main/resources/static 直接同源部署（見 vite.config.js），
  // 後端目前沒有設定「找不到路徑就回傳 index.html」的 SPA fallback（也不屬於前端能決定的範圍）。
  // History 模式下，直接重新整理 /people 這種子頁面，瀏覽器會真的對後端要求 GET /people，
  // 後端沒有這個路徑、也沒有對應的靜態檔案，就會回 404。
  // Hash 模式的網址是 /#/people，# 後面的部分瀏覽器不會送給伺服器，一定只會要求首頁 index.html，
  // 不需要後端另外設定，重新整理也不會壞。
  history: createWebHashHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }                                        // 每次切頁面都捲回頁面最上方，避免留在舊頁面的捲動位置
  },
})

export default router
