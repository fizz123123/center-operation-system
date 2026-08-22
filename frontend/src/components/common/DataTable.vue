<script setup>
import Icon from './Icon.vue'
import Pagination from './Pagination.vue'

// 通用資料表格：Person / Course / Enrollment 幾個列表頁都靠這個元件畫表格，
// 只有欄位定義（columns）跟每列資料（rows）不同，排序按鈕/空狀態/loading 骨架都在這裡統一處理一次。
// 分頁列的 UI/邏輯抽到 Pagination.vue，跟不是表格的 AlertsPage.vue 共用。
//
// 這個元件本身不做排序/分頁的實際運算——排序、分頁、搜尋現在都改成呼叫後端 API 做（配合 200 筆以上的測試資料量，
// 前端自己排序/切頁不切實際）。呼叫端負責：拿到目前頁的 rows 傳進來、監聽 sort-change 換排序參數重新呼叫 API、
// 監聽 update:page 換頁碼重新呼叫 API。這個元件只負責把「目前這一頁該長什麼樣子」畫出來。
const props = defineProps({
  columns: { type: Array, required: true },                  // [{ key, label, sortable?, numeric?, width? }]
  rows: { type: Array, required: true },                      // 目前這一頁的資料（已經是伺服器排序/篩選/切頁後的結果）
  rowKey: { type: String, default: 'id' },                   // 用來做 :key 的欄位名稱
  loading: { type: Boolean, default: false },
  emptyMessage: { type: String, default: '目前沒有資料' },
  page: { type: Number, default: 1 },                        // 目前頁碼，1-indexed（給畫面顯示用，呼叫 API 時記得自己轉成後端的 0-indexed）
  pageSize: { type: Number, default: 10 },
  totalItems: { type: Number, default: 0 },                  // 伺服器回傳的總筆數（Page<T> 的 totalElements）
  sortBy: { type: String, default: '' },                     // 目前排序欄位的 key，空字串代表沒有指定排序
  sortDir: { type: String, default: 'asc' },                 // 'asc' | 'desc'
})

const emit = defineEmits(['update:page', 'sort-change'])

function toggleSort(column) {
  if (!column.sortable) return
  if (props.sortBy !== column.key) {
    emit('sort-change', { key: column.key, dir: 'asc' })
  } else {
    // 同一欄再點一次就反轉排序方向
    emit('sort-change', { key: column.key, dir: props.sortDir === 'asc' ? 'desc' : 'asc' })
  }
}

function ariaSortFor(column) {
  if (!column.sortable) return undefined
  if (props.sortBy !== column.key) return 'none'
  return props.sortDir === 'asc' ? 'ascending' : 'descending'
}
</script>

<template>
  <div class="table-wrap">                                    <!-- 外層 overflow-x:auto，手機/窄螢幕表格可以左右滑，不會把頁面撐寬 -->
    <table>
      <thead>
        <tr>
          <th
            v-for="col in columns"
            :key="col.key"
            :class="{ sortable: col.sortable, numeric: col.numeric }"
            :aria-sort="ariaSortFor(col)"
            :style="col.width ? { width: col.width } : null"
          >
            <button
              v-if="col.sortable"
              type="button"
              class="sort-btn"
              @click="toggleSort(col)"
            >
              {{ col.label }}
              <Icon
                :name="sortBy === col.key && sortDir === 'desc' ? 'sort-desc' : 'sort-asc'"
                :size="14"
              />
            </button>
            <span v-else>{{ col.label }}</span>
          </th>
        </tr>
      </thead>

      <tbody>
        <!-- Loading：畫幾列骨架列，避免表格瞬間從空白跳成有資料，造成畫面跳動 -->
        <template v-if="loading">
          <tr v-for="n in 4" :key="`skeleton-${n}`" class="skeleton-row">
            <td v-for="col in columns" :key="col.key"><span class="skeleton-bar"></span></td>
          </tr>
        </template>

        <tr v-else-if="rows.length === 0">
          <td :colspan="columns.length" class="empty-cell">{{ emptyMessage }}</td>
        </tr>

        <tr v-else v-for="row in rows" :key="row[rowKey]">
          <td v-for="col in columns" :key="col.key" :class="{ numeric: col.numeric }">
            <!-- 具名插槽 cell-<key>：需要客製化顯示內容（例如徽章、按鈕）時，呼叫端可以覆蓋預設純文字顯示 -->
            <slot :name="`cell-${col.key}`" :row="row">
              <span :class="{ num: col.numeric }">{{ row[col.key] }}</span>
            </slot>
          </td>
        </tr>
      </tbody>
    </table>

    <Pagination
      v-if="!loading"
      :page="page"
      :page-size="pageSize"
      :total-items="totalItems"
      @update:page="$emit('update:page', $event)"
    />
  </div>
</template>

<style scoped>
.table-wrap {
  overflow-x: auto;                                            /* horizontal-scroll 準則：頁面本身不能橫向捲，捲動限制在表格容器內 */
  background: var(--color-bg-surface);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-card);
}

table {
  width: 100%;
  border-collapse: collapse;
  font-size: var(--font-size-sm);
  /* table-layout:fixed 讓欄寬只依「表頭第一列」決定一次，之後 tbody 內容再怎麼變（例如狀態下拉選單切換文字長度）
     都不會讓欄位跟著重新計算寬度——這是「切換狀態時表格閃爍/欄位跳動」的根本原因，改這裡一次修好，
     不用每個用到 DataTable 的頁面各自想辦法。有指定 col.width 的欄位用該寬度，沒指定的欄位由瀏覽器平均分配剩餘空間。 */
  table-layout: fixed;
}

thead th {
  text-align: left;
  padding: var(--space-3) var(--space-4);
  /* 表頭原本整片塗滿淺鼠尾草綠，大面積色塊是「悶」的原因之一；
     改成白底 + 弱化的灰綠標籤文字 + 底線，跟參考圖裡乾淨的表格風格一致，色彩留給徽章/按鈕才有重點 */
  background: var(--color-bg-surface);
  border-bottom: 1px solid var(--color-border);
  color: var(--color-text-muted);
  font-weight: 600;
  font-size: var(--font-size-xs);
  letter-spacing: 0.02em;
  text-transform: uppercase;
  white-space: nowrap;
  position: sticky;
  top: 0;
}
thead th.numeric { text-align: right; }

.sort-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  background: none;
  border: none;
  padding: 0;
  font: inherit;
  font-weight: 600;
  color: inherit;
}
.sort-btn:hover { color: var(--color-brand-600); }

tbody td {
  padding: var(--space-3) var(--space-4);
  border-top: 1px solid var(--color-border);
  vertical-align: middle;
  white-space: nowrap;                                         /* 預設不換行，避免電話號碼、代碼這類短資料被擠成三行；
                                                                     真的需要換行的欄位（例如課程描述）由呼叫端在自己的 cell 內容上覆寫 */
  overflow: hidden;
  text-overflow: ellipsis;                                     /* 欄寬固定後，極端情況下內容比欄位寬時用刪節號收掉，不會撐開表格 */
}
tbody td.numeric { text-align: right; }

/* 操作欄的圖示按鈕（AppButton icon-only）滑鼠移過去會往上彈出文字提示，
   如果這個儲存格還是 overflow:hidden，提示彈出時一超過儲存格上緣就會被剪掉，
   看起來像是被上一列擋住。這個儲存格本來就沒有需要省略號收掉的長文字，
   所以只針對「有 tooltip 的儲存格」開放 overflow:visible，其他欄位的省略號效果不受影響。 */
tbody td:has(.app-btn-tooltip) {
  overflow: visible;
}

tbody tr:hover { background: color-mix(in srgb, var(--color-brand-100) 55%, white); }

.empty-cell {
  text-align: center;
  color: var(--color-text-muted);
  padding: var(--space-6);
}

.skeleton-bar {
  display: block;
  height: 14px;
  width: 70%;
  border-radius: 4px;
  background: linear-gradient(90deg, var(--color-border) 25%, #eef1ee 50%, var(--color-border) 75%);
  background-size: 200% 100%;
  animation: shimmer 1.4s ease-in-out infinite;
}
@keyframes shimmer { 0% { background-position: 200% 0; } 100% { background-position: -200% 0; } }
</style>
