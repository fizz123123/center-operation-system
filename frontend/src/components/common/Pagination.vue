<script setup>
import { computed } from 'vue'
import Icon from './Icon.vue'

// 共用的分頁列 UI：抽出來給 DataTable.vue（表格清單）跟 AlertsPage.vue（卡片清單，不是表格）共用，
// 兩邊的資料呈現方式不一樣（一個是 <table>、一個是 <ul> 卡片），但分頁邏輯跟外觀應該長得一樣，
// 不應該各自複製一份、之後改樣式要改兩處。
//
// 這個元件本身不知道資料是什麼，只根據 page / pageSize / totalItems 算頁碼，換頁時 emit update:page，
// 由呼叫端自己決定換頁後要做什麼（通常是重新呼叫分頁 API）。
const props = defineProps({
  page: { type: Number, default: 1 },                          // 目前頁碼，1-indexed
  pageSize: { type: Number, default: 10 },
  totalItems: { type: Number, default: 0 },
})
const emit = defineEmits(['update:page'])

const totalPages = computed(() => Math.max(1, Math.ceil(props.totalItems / props.pageSize)))

// 頁碼按鈕：資料不多時全部顯示；超過 7 頁就在中間用「…」省略，只留頭尾跟目前頁附近
const pageNumbers = computed(() => {
  const total = totalPages.value
  if (total <= 7) return Array.from({ length: total }, (_, i) => i + 1)
  const current = props.page
  const pages = new Set([1, 2, total - 1, total, current - 1, current, current + 1])
  return [...pages].filter((p) => p >= 1 && p <= total).sort((a, b) => a - b)
})

function goToPage(page) {
  emit('update:page', Math.min(Math.max(1, page), totalPages.value))
}

const rangeStart = computed(() => (props.page - 1) * props.pageSize + 1)
const rangeEnd = computed(() => Math.min(props.page * props.pageSize, props.totalItems))
</script>

<template>
  <div v-if="totalItems > pageSize" class="pagination">
    <span class="pagination-summary">
      顯示第 <strong class="num">{{ rangeStart }}–{{ rangeEnd }}</strong> 筆，共 <strong class="num">{{ totalItems }}</strong> 筆
    </span>
    <div class="pagination-controls">
      <button
        type="button"
        class="page-nav-btn"
        :disabled="page === 1"
        aria-label="上一頁"
        @click="goToPage(page - 1)"
      >
        <Icon name="chevron-left" :size="14" />
      </button>

      <template v-for="(p, index) in pageNumbers" :key="p">
        <!-- 頁碼不連續時（例如 2 之後跳到 8）插入省略符號，不然使用者會以為中間頁碼漏掉了 -->
        <span v-if="index > 0 && p - pageNumbers[index - 1] > 1" class="page-ellipsis" aria-hidden="true">···</span>
        <button
          type="button"
          class="page-btn"
          :class="{ 'page-btn--active': p === page }"
          :aria-current="p === page ? 'page' : undefined"
          :aria-label="`第 ${p} 頁`"
          @click="goToPage(p)"
        >
          {{ p }}
        </button>
      </template>

      <button
        type="button"
        class="page-nav-btn"
        :disabled="page === totalPages"
        aria-label="下一頁"
        @click="goToPage(page + 1)"
      >
        <Icon name="chevron-right" :size="14" />
      </button>
    </div>
  </div>
</template>

<style scoped>
.pagination {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3);
  padding: var(--space-4);
  border-top: 1px solid var(--color-border);
  flex-wrap: wrap;
}

.pagination-summary {
  font-size: var(--font-size-sm);
  color: var(--color-text-muted);
}
.pagination-summary strong { color: var(--color-text-primary); font-weight: 700; }

.pagination-controls {
  display: flex;
  align-items: center;
  gap: 6px;
}

/* 上一頁／下一頁改成有邊框的圓形圖示按鈕，跟數字頁碼用不同的形狀語言區分「導覽動作」跟「頁碼本身」 */
.page-nav-btn {
  display: grid;
  place-items: center;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  border: 1px solid var(--color-border);
  background: var(--color-bg-surface);
  color: var(--color-text-secondary);
}
.page-nav-btn:hover:not(:disabled) { border-color: var(--color-brand-600); color: var(--color-brand-600); }
.page-nav-btn:disabled { opacity: 0.35; cursor: not-allowed; }

/* 數字頁碼平常不畫底色/邊框，只有目前頁用實心圓角方塊標出來——
   跟站上其他地方習慣用的滿圓角膠囊刻意做出區隔，頁碼本身看起來更像「座標」而不是另一顆按鈕 */
.page-btn {
  display: grid;
  place-items: center;
  min-width: 30px;
  height: 30px;
  padding: 0 6px;
  border-radius: var(--radius-sm);
  background: none;
  color: var(--color-text-muted);
  font-size: var(--font-size-sm);
  font-weight: 600;
}
.page-btn:hover:not(:disabled):not(.page-btn--active) { color: var(--color-text-primary); }
.page-btn--active {
  background: var(--color-brand-600);
  color: var(--color-text-inverse);                             /* 白字 on brand-600 對比 4.86:1，通過 AA（同套配色邏輯見 tokens.css）*/
  box-shadow: 0 2px 6px -1px color-mix(in srgb, var(--color-brand-600) 55%, transparent);
}

.page-ellipsis {
  padding: 0 2px;
  color: var(--color-text-muted);
  font-size: var(--font-size-xs);
  letter-spacing: -1px;
}
</style>
