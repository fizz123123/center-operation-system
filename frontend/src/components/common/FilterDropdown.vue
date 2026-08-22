<script setup>
import Icon from './Icon.vue'

// 通用篩選下拉：改用原生 <select>，展開清單時就是瀏覽器原生的下拉選單樣式——
// 跟表格「操作」欄那個原生狀態下拉（已完成／進行中／尚未開始）長得一樣，不用自己刻一份選單面板
// （原本自己刻的浮動面板打開後不好看，原生的最簡單也最一致）。
defineProps({
  label: { type: String, default: '篩選' },                  // 找不到對應 aria-label 時的備援文字
  options: { type: Array, required: true },                  // [{ value, label }]，第一筆視為「全部／不篩選」
  modelValue: { type: String, required: true },
  icon: { type: String, default: 'filter' },                 // 這個元件本質就是「圖示＋原生 select」，狀態篩選以外的場合（例如選擇學員）可以換一顆語意更合的圖示
})
defineEmits(['update:modelValue'])
</script>

<template>
  <div class="filter-select">
    <Icon :name="icon" :size="16" class="filter-icon" />
    <select
      :value="modelValue"
      :aria-label="label"
      @change="$emit('update:modelValue', $event.target.value)"
    >
      <option v-for="opt in options" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
    </select>
  </div>
</template>

<style scoped>
.filter-select {
  position: relative;
  display: inline-flex;
  align-items: center;
}

.filter-icon {
  position: absolute;
  left: 12px;
  color: var(--color-text-muted);
  pointer-events: none;
}

/* 跟 .search-box input／.status-select 用同一套外觀語言，維持全站表單元件一致 */
select {
  min-height: 40px;
  padding: 0 var(--space-3) 0 36px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-pill);
  background: var(--color-bg-surface);
  color: var(--color-text-primary);
  font-size: var(--font-size-sm);
  font-weight: 600;
  cursor: pointer;
}
select:focus-visible {
  outline: none;
  border-color: var(--color-brand-600);
  box-shadow: 0 0 0 3px color-mix(in srgb, var(--color-brand-600) 20%, transparent);
}
</style>
