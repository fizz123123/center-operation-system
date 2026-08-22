<script setup>
import { onMounted, ref, watch } from 'vue'
import Badge from '../components/common/Badge.vue'
import Icon from '../components/common/Icon.vue'
import FilterDropdown from '../components/common/FilterDropdown.vue'
import Pagination from '../components/common/Pagination.vue'
import { getAlerts } from '../api/alert.js'
import { alertPriorityMeta } from '../utils/statusMeta.js'
import { useToastStore } from '../stores/toast.js'
import { extractErrorMessage } from '../utils/errorMessage.js'

// 獨立的警示列表頁：直接呼叫 getAlerts() 自己管理分頁狀態，不透過 stores/alerts.js 那個共用 store——
// 那個 store 現在只給 Dashboard／Navbar 抓「前幾筆預覽」用（見該檔案的說明），這裡要瀏覽完整分頁清單，
// 兩邊需求不一樣，各自處理反而更單純。
//
// 沒有清除/刪除功能：docs/03_api_spec.md 第 8 節只定義了 GET /api/alerts，沒有刪除警示的 API，
// 之前做過一版純前端本地清除，跟文件需求對不上，重新整理頁面清掉的警示還會再出現，容易誤導，所以拿掉。
const toast = useToastStore()

const PAGE_SIZE = 10
const alerts = ref([])
const loading = ref(true)
const totalItems = ref(0)
const page = ref(1)

// ---- 依優先權篩選：priority 是數字（3 高／2 中／1 低），現在會實際打 API（帶 priority query 參數） ----
const priorityFilter = ref('ALL')
// 選項文字用完整敘述（高優先權／中優先權／低優先權），不要只顯示「高／中／低」單字，不然篩選下拉選單裡看起來語意不清楚
const PRIORITY_FILTER_OPTIONS = [
  { value: 'ALL', label: '全部' },
  { value: '3', label: `${alertPriorityMeta(3).label}優先權` },
  { value: '2', label: `${alertPriorityMeta(2).label}優先權` },
  { value: '1', label: `${alertPriorityMeta(1).label}優先權` },
]

async function loadAlerts() {
  loading.value = true
  try {
    const result = await getAlerts({
      page: page.value - 1,
      size: PAGE_SIZE,
      priority: priorityFilter.value === 'ALL' ? '' : priorityFilter.value,
    })
    alerts.value = result.content
    totalItems.value = result.totalElements
  } catch (error) {
    toast.error(extractErrorMessage(error, '讀取警示清單失敗'))
  } finally {
    loading.value = false
  }
}

function handlePageChange(newPage) {
  page.value = newPage
  loadAlerts()
}

watch(priorityFilter, () => {
  page.value = 1
  loadAlerts()
})

onMounted(loadAlerts)
</script>

<template>
  <div class="page">
    <div class="page-toolbar">
      <FilterDropdown v-model="priorityFilter" label="依優先權篩選" :options="PRIORITY_FILTER_OPTIONS" />
    </div>

    <div v-if="loading" class="alert-skeleton" aria-hidden="true">
      <span v-for="n in 4" :key="n"></span>
    </div>

    <template v-else-if="alerts.length > 0">
      <ul class="alert-list">
        <li v-for="alert in alerts" :key="alert.id" class="alert-item">
          <Badge
            :color="alertPriorityMeta(alert.priority).color"
            :label="`${alertPriorityMeta(alert.priority).label}優先權`"
            :icon="alertPriorityMeta(alert.priority).icon"
          />
          <span class="alert-message">{{ alert.message }}</span>
        </li>
      </ul>
      <Pagination :page="page" :page-size="PAGE_SIZE" :total-items="totalItems" @update:page="handlePageChange" />
    </template>

    <div v-else class="empty-state">
      <Icon name="check" :size="24" />
      <p v-if="priorityFilter !== 'ALL'">沒有符合篩選條件的警示。</p>
      <p v-else>目前沒有任何警示，一切正常。</p>
    </div>
  </div>
</template>

<style scoped>
.page { display: flex; flex-direction: column; gap: var(--space-4); }

.page-toolbar {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: var(--space-4);
}

.alert-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
  background: var(--color-bg-surface);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-card);
  padding: var(--space-4);
}
.alert-item {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3);
  border-radius: var(--radius-sm);
  border: 1px solid var(--color-border);
}
.alert-message { flex: 1; font-size: var(--font-size-sm); }

.alert-skeleton {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
  background: var(--color-bg-surface);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-card);
  padding: var(--space-4);
}
.alert-skeleton span {
  height: 56px;
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
  background: var(--color-bg-surface);
  border-radius: var(--radius-lg);
}
</style>
