import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getAlerts } from '../api/alert.js'

// 警示資料的共用 store：Dashboard 頁的「優先警示」面板、Navbar 選單裡的警示圖示共用同一份「預覽」資料
// （只抓前幾筆，不是完整清單）。完整清單改成分頁瀏覽，交給 AlertsPage.vue 自己直接呼叫 getAlerts() 管理，
// 不透過這個 store——這個 store 存在的目的只是讓 Dashboard／Navbar 不用各自重打一次 API 要預覽資料。
//
// 注意：docs/03_api_spec.md 第 8 節只定義了 GET /api/alerts（查詢），沒有定義刪除/清除警示的 API，
// 所以這裡故意不做「清除」功能——之前做過一版只在前端本地移除、不呼叫後端的假清除，
// 但那不是文件要求的功能，重新整理頁面後清掉的警示還會再出現，容易讓人誤會這是真的有效果，所以拿掉。
const PREVIEW_SIZE = 5                                          // Dashboard 只預覽前 4 則，多抓 1 則當緩衝就夠，不需要抓一大批

export const useAlertsStore = defineStore('alerts', () => {
  const alerts = ref([])                                        // 前幾筆警示，給 Dashboard 預覽／Navbar 判斷用，不是完整清單
  const totalCount = ref(0)                                     // 警示總數（來自分頁回應的 totalElements），Navbar 顯示「N 則待處理」要用這個，不能用 alerts.length
  const loading = ref(false)
  const loaded = ref(false)                                     // 是否已經成功載入過一次，避免每個用到的元件都重複打一次 API

  async function load() {
    if (loaded.value) return                                    // 已經載入過就不重複打 API；真的需要強制重新整理請用 refresh()
    await refresh()
  }

  async function refresh() {
    loading.value = true
    try {
      const result = await getAlerts({ page: 0, size: PREVIEW_SIZE })
      alerts.value = result.content
      totalCount.value = result.totalElements
      loaded.value = true
    } finally {
      loading.value = false
    }
  }

  return { alerts, totalCount, loading, load, refresh }
})
