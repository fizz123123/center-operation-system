<script setup>
import Icon from './Icon.vue'

// 小型統計數字：跟 Dashboard 用的 StatCard 是同一種「數字＋標籤＋圖示」概念，
// 但做成扁平的小藥丸樣式，不帶陰影──這是刻意的：之前試過浮在導覽列跟工具列中間當獨立卡片，
// 回饋是看起來很突兀，才改成扁平的小藥丸。
// 用法有兩種：透過 Teleport 塞進 AppNavbar.vue 的 #navbar-stats-target（例如人員管理），
// 或直接放在頁面內容裡（例如課程管理，放在工具列裡貼著新增按鈕）。
defineProps({
  label: { type: String, required: true },
  value: { type: [String, Number], required: true },
  icon: { type: String, required: true },
  accent: { type: String, default: 'var(--color-brand-600)' },
  // labelFirst：文字順序改成「標籤 數字」（例如「課程總數 5」），預設維持「數字 標籤」（例如「5 課程總數」）。
  // 只是排版順序的選項，不影響其他頁面既有的用法。
  labelFirst: { type: Boolean, default: false },
})
</script>

<template>
  <div class="mini-stat">
    <span class="mini-stat-icon" :style="{ '--accent': accent }">
      <Icon :name="icon" :size="14" />
    </span>
    <template v-if="labelFirst">
      <span class="mini-stat-label">{{ label }}</span>
      <span class="mini-stat-value num">{{ value }}</span>
    </template>
    <template v-else>
      <span class="mini-stat-value num">{{ value }}</span>
      <span class="mini-stat-label">{{ label }}</span>
    </template>
  </div>
</template>

<style scoped>
.mini-stat {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 5px 12px;
  border-radius: var(--radius-pill);
  /* 改成白底＋邊框：這個元件現在有兩種使用場合——貼在 Navbar 的白色卡片上，或直接放在頁面內容裡
     （頁面內容區的底色是 shell-frame 的淺鼠尾草綠 --color-brand-100）。之前用淡底色 brand-100 當背景，
     放在同樣是 brand-100 的頁面托盤底色上時，底色完全融在一起、外框直接消失不見；
     白底＋邊框在兩種底色上都看得出清楚的輪廓，不會因為放的位置不同而跟底色融合 */
  background: var(--color-bg-surface);
  border: 1px solid var(--color-border);
}

.mini-stat-icon {
  display: grid;
  place-items: center;
  color: var(--accent);
}

.mini-stat-value {
  font-size: var(--font-size-sm);
  font-weight: 700;
  color: var(--color-text-primary);
}

.mini-stat-label {
  font-size: var(--font-size-xs);
  color: var(--color-text-muted);
  white-space: nowrap;
}
</style>
