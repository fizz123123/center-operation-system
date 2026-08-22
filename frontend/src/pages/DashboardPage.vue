<script setup>
import { computed, onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { storeToRefs } from 'pinia'
import StatCard from '../components/common/StatCard.vue'
import Badge from '../components/common/Badge.vue'
import Icon from '../components/common/Icon.vue'
import { getDashboardSummary } from '../api/dashboard.js'
import { alertPriorityMeta } from '../utils/statusMeta.js'
import { useToastStore } from '../stores/toast.js'
import { useAlertsStore } from '../stores/alerts.js'
import { extractErrorMessage } from '../utils/errorMessage.js'

// Module 4：Dashboard 分析頁
// 對應 GET /api/dashboard（總覽數字）＋ GET /api/alerts（MaxHeap 依優先權排序後的警示清單）

const toast = useToastStore()
// 警示資料吃共用的 alerts store（跟 Navbar 選單裡的警示圖示共用同一份「預覽」資料，只有前幾則，不是完整清單）
const alertsStore = useAlertsStore()
const { alerts, totalCount: alertsTotalCount, loading: loadingAlerts } = storeToRefs(alertsStore)

const summary = ref({ totalPeople: 0, totalCourses: 0, totalEnrollments: 0, completionRate: 0 })
const loadingSummary = ref(true)

async function loadSummary() {
  loadingSummary.value = true
  try {
    summary.value = await getDashboardSummary()
  } catch (error) {
    toast.error(extractErrorMessage(error, '讀取 Dashboard 統計資料失敗'))
  } finally {
    loadingSummary.value = false
  }
}

// Dashboard 只先預覽前 4 則，完整清單交給獨立的 /alerts 頁面（見 pages/AlertsPage.vue），
// 避免這裡的面板隨警示數量增加而越拉越長
const previewAlerts = computed(() => alerts.value.slice(0, 4))

onMounted(() => {
  loadSummary()                                                // 兩支 API 互不相依，平行呼叫，不用 await 排隊等
  // load()：只在 store 還沒載入過的時候才打 API，避免每次切回 Dashboard 頁都重打一次；
  // 真的需要強制拉最新資料時用 refresh()。
  alertsStore.load().catch(() => toast.error('讀取警示清單失敗'))
})

// 完成率甜甜圈：用 SVG stroke-dasharray 畫圓環進度，不用額外裝圖表套件，畫面元素少、好在口試現場逐行解釋
const CIRCUMFERENCE = 2 * Math.PI * 54                          // 半徑 54 的圓周長
function dashOffset(percent) {
  const clamped = Math.min(100, Math.max(0, percent))
  return CIRCUMFERENCE * (1 - clamped / 100)
}
</script>

<template>
  <div class="dashboard">
    <!-- 四張 KPI 卡片：總學員數 / 總課程數 / 課程完成率 / 總註冊數
         其中「總註冊數」用 variant="filled" 當本頁的主打指標卡（呼應參考圖裡整片實色的 Activity/highlight 卡），
         其餘三張維持白底卡片，讓畫面有明確的視覺重點，而不是四張長得一樣重要 -->
    <section class="stat-grid">
      <StatCard label="總學員數" :value="summary.totalPeople" icon="users" accent="var(--color-brand-600)" :loading="loadingSummary" />
      <StatCard label="總課程數" :value="summary.totalCourses" icon="book" accent="var(--color-brand-400)" :loading="loadingSummary" />
      <StatCard label="課程完成率" :value="`${summary.completionRate}%`" icon="check" accent="var(--color-accent-warn)" :loading="loadingSummary" />
      <StatCard label="總註冊數（本學期活動量）" :value="summary.totalEnrollments" icon="clipboard" variant="filled" :loading="loadingSummary" />
    </section>

    <section class="dashboard-grid">
      <!-- 完成率甜甜圈圖：把 completionRate 用視覺化方式再呈現一次，跟上面卡片的數字互相呼應 -->
      <article class="panel">
        <h2>整體完成率</h2>
        <div class="donut-wrap">
          <svg viewBox="0 0 120 120" width="160" height="160" role="img" :aria-label="`整體課程完成率 ${summary.completionRate}%`">
            <circle cx="60" cy="60" r="54" fill="none" stroke="var(--color-border)" stroke-width="12" />
            <circle
              cx="60" cy="60" r="54" fill="none"
              stroke="var(--color-brand-600)" stroke-width="12" stroke-linecap="round"
              :stroke-dasharray="CIRCUMFERENCE"
              :stroke-dashoffset="dashOffset(summary.completionRate)"
              transform="rotate(-90 60 60)"
              class="donut-progress"
            />
            <text x="60" y="66" text-anchor="middle" class="donut-text">{{ summary.completionRate }}%</text>
          </svg>
          <!-- 螢幕閱讀器/無法看圖表的使用者，仍然可以從這個表格拿到一樣的資訊（data-table 準則） -->
          <table class="sr-only-table">
            <caption>完成率數據</caption>
            <tbody>
              <tr><th scope="row">已完成</th><td>{{ summary.completionRate }}%</td></tr>
              <tr><th scope="row">未完成</th><td>{{ (100 - summary.completionRate).toFixed(1) }}%</td></tr>
            </tbody>
          </table>
        </div>
        <!-- 這裡原本寫的是「計算方式：已完成筆數 ÷ 總註冊筆數」，但那是算法，不是這個數字存在的用意。
             改成說明這個百分比對「做決策」的意義：數字偏低代表什麼、該不該採取行動，
             讓看的人一眼知道「這個數字為什麼重要」，而不是「這個數字怎麼算出來的」 -->
        <p class="panel-note">數字愈低，代表愈多學員修課修到一半沒完成，可能需要主動關心進度、提供協助。</p>
      </article>

      <!-- 警示清單：展示 MaxHeap 依優先權排序的成果，優先權愈高（HIGH）排愈前面。
           這裡只預覽前 4 則，完整清單（含分頁）在獨立的警示列表頁 -->
      <article class="panel alert-panel">
        <div class="panel-header">
          <h2>優先警示（MaxHeap 排序）</h2>
          <RouterLink v-if="alertsTotalCount > 0" to="/alerts" class="view-all-link">查看全部</RouterLink>
        </div>

        <ul v-if="!loadingAlerts && previewAlerts.length > 0" class="alert-list">
          <li v-for="alert in previewAlerts" :key="alert.id" class="alert-item">
            <Badge :color="alertPriorityMeta(alert.priority).color" :label="`${alertPriorityMeta(alert.priority).label}優先權`" :icon="alertPriorityMeta(alert.priority).icon" />
            <span class="alert-message">{{ alert.message }}</span>
          </li>
        </ul>

        <div v-else-if="loadingAlerts" class="alert-skeleton" aria-hidden="true">
          <span v-for="n in 3" :key="n"></span>
        </div>

        <div v-else class="empty-state">
          <Icon name="check" :size="24" />
          <p>目前沒有任何警示，一切正常。</p>
        </div>
      </article>
    </section>
  </div>
</template>

<style scoped>
.dashboard { display: flex; flex-direction: column; gap: var(--space-5); }

.stat-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));  /* 自動決定每列放幾張卡，窄螢幕自動變一欄 */
  gap: var(--space-4);
}

.dashboard-grid {
  display: grid;
  grid-template-columns: minmax(260px, 360px) 1fr;
  gap: var(--space-4);
}
@media (max-width: 900px) {
  .dashboard-grid { grid-template-columns: 1fr; }               /* 窄螢幕改成單欄堆疊 */
}

.panel {
  background: var(--color-bg-surface);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-card);
  padding: var(--space-5);
}
.panel h2 {
  font-size: var(--font-size-md);
  margin: 0 0 var(--space-4);
}
.panel-note {
  margin: var(--space-4) 0 0;
  text-align: center;
  font-size: var(--font-size-xs);
  color: var(--color-text-muted);
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3);
  margin-bottom: var(--space-4);
}
.panel-header h2 { margin: 0; }                                 /* 包在 header 裡時改由 .panel-header 統一控制間距 */
.view-all-link {
  font-size: var(--font-size-xs);
  font-weight: 600;
  color: var(--color-brand-600);
  white-space: nowrap;
}

.donut-wrap {
  display: flex;
  justify-content: center;
}
.donut-progress {
  transition: stroke-dashoffset 600ms ease;                     /* 數字進場時圓環用動畫畫出來，而不是瞬間出現 */
}
.donut-text {
  font-size: 20px;
  font-weight: 700;
  fill: var(--color-text-primary);
}

/* 這個表格只給螢幕閱讀器使用，視覺上隱藏但不能用 display:none（那樣連 AT 都讀不到） */
.sr-only-table {
  position: absolute;
  width: 1px; height: 1px;
  overflow: hidden;
  clip: rect(0 0 0 0);
  white-space: nowrap;
}


.alert-list { display: flex; flex-direction: column; gap: var(--space-3); }
.alert-item {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3);
  border-radius: var(--radius-sm);
  border: 1px solid var(--color-border);
}
.alert-message { font-size: var(--font-size-sm); }

.alert-skeleton { display: flex; flex-direction: column; gap: var(--space-3); }
.alert-skeleton span {
  height: 44px;
  border-radius: var(--radius-sm);
  background: linear-gradient(90deg, var(--color-border) 25%, #eef1ee 50%, var(--color-border) 75%);
  background-size: 200% 100%;
  animation: shimmer 1.4s ease-in-out infinite;
}
@keyframes shimmer { 0% { background-position: 200% 0; } 100% { background-position: -200% 0; } }

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-6);
  color: var(--color-text-muted);
}
</style>
