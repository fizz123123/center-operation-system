<script setup>
import Icon from './Icon.vue'

// 通用狀態徽章：顏色 + 圖示 + 文字一起出現，不會只靠顏色傳達意義。
// 顏色用 CSS 變數字串傳進來（例如 'var(--color-priority-high)'），統一取用 tokens.css 裡的語意色。
defineProps({
  color: { type: String, required: true },
  label: { type: String, required: true },
  icon: { type: String, default: null },                    // 圖示可省略（例如只是中性標籤時）
  // 實心樣式：深色底、白字，跟預設的「淡色底＋深色字」拉開視覺重量，不只是換個色相。
  // 用在同一組狀態裡「開／關」對比需要更直覺的場合（例如在學 vs 停用），
  // 因為 8 色色票能當小字用的深色只有 3 個（見 tokens.css），光靠換色相區分兩個狀態常常不夠明顯，
  // 用「實心 vs 淡色」的形狀語言差異，比單純换色更一眼看得出來。
  solid: { type: Boolean, default: false },
})
</script>

<template>
  <span class="badge" :class="{ 'badge--solid': solid }" :style="{ '--badge-color': color }">
    <Icon v-if="icon" :name="icon" :size="12" />
    {{ label }}
  </span>
</template>

<style scoped>
.badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 3px 10px;
  border-radius: 999px;
  font-size: var(--font-size-xs);
  font-weight: 600;
  color: var(--badge-color);
  background: color-mix(in srgb, var(--badge-color) 14%, white);  /* 用同一個顏色淡化當底色，主色當文字/圖示色 */
  border: 1px solid color-mix(in srgb, var(--badge-color) 35%, white);
  white-space: nowrap;
}

.badge--solid {
  color: var(--color-text-inverse);
  background: var(--badge-color);
  border-color: var(--badge-color);
}
</style>
