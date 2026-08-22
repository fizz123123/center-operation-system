<script setup>
import { computed } from 'vue'
import { RouterLink } from 'vue-router'

// 統一的按鈕元件：全站的按鈕外觀（主要/次要/危險）跟 loading 狀態都從這裡出，
// 避免每個頁面各自寫 <button class="...">，久了樣式會慢慢分裂。
//
// 有傳 `to` 時會渲染成 RouterLink（同樣套用這裡的按鈕樣式），沒有傳就是原生 <button>——
// 讓「導覽到別的頁面」跟「觸發動作」視覺上長得一樣，但語意上還是各自用對的 HTML 標籤
// （連結用 <a>、動作用 <button>），不會為了共用樣式犧牲語意。
//
// iconOnly：只顯示圖示、不顯示文字（例如表格裡的「編輯」「詳情」），
// 這時候一定要傳 tooltip——它同時是滑鼠停留/鍵盤 focus 時彈出的提示文字，也是螢幕閱讀器唸的名稱，
// 只給一個字串就兩件事都處理掉，不用呼叫端另外記得補 aria-label。
const props = defineProps({
  variant: { type: String, default: 'primary' },             // 'primary' | 'secondary' | 'ghost'
  type: { type: String, default: 'button' },                 // 傳給原生 <button type="">
  loading: { type: Boolean, default: false },                // 非同步操作進行中：顯示轉圈圈並停用按鈕
  disabled: { type: Boolean, default: false },
  to: { type: [String, Object], default: null },             // 有值時渲染成 RouterLink，導覽用
  iconOnly: { type: Boolean, default: false },
  tooltip: { type: String, default: '' },
})

const tag = props.to ? RouterLink : 'button'
const accessibleLabel = computed(() => (props.iconOnly ? props.tooltip : null))
</script>

<template>
  <component
    :is="tag"
    :to="to ?? undefined"
    :type="to ? undefined : type"
    class="app-btn"
    :class="[`app-btn--${variant}`, { 'app-btn--icon-only': iconOnly }]"
    :disabled="!to && (disabled || loading)"
    :aria-label="accessibleLabel"
  >
    <!-- loading 時顯示旋轉圈圈取代圖示，並且停用按鈕避免使用者重複點擊送出兩次（loading-buttons 準則） -->
    <span v-if="loading" class="spinner" aria-hidden="true"></span>
    <slot />
    <!-- 提示文字只在 iconOnly 時出現；aria-hidden 是因為內容已經由上面的 aria-label 唸過一次，避免螢幕閱讀器重複唸 -->
    <span v-if="iconOnly && tooltip" class="app-btn-tooltip" aria-hidden="true">{{ tooltip }}</span>
  </component>
</template>

<style scoped>
.app-btn {
  position: relative;                                          /* 給 icon-only 的 tooltip 做定位基準 */
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  min-height: 40px;
  padding: 0 var(--space-5);
  border-radius: var(--radius-pill);                          /* 膠囊造型，呼應參考圖裡的按鈕風格 */
  border: 1px solid transparent;
  font-weight: 600;
  font-size: var(--font-size-base);
  transition: background var(--transition-fast), border-color var(--transition-fast), transform 80ms ease;
}
.app-btn:hover { text-decoration: none; }                     /* RouterLink 預設 hover 會加底線，跟按鈕外觀不搭 */
.app-btn:active { transform: scale(0.97); }                  /* 按下輕微縮放回饋，scale-feedback 準則 */

.app-btn--icon-only {
  width: 36px;
  min-height: 36px;
  padding: 0;
}

.app-btn--primary {
  background: var(--color-brand-600);
  color: var(--color-text-inverse);
}
.app-btn--primary:hover:not(:disabled) { background: var(--color-brand-800); }

.app-btn--secondary {
  background: var(--color-bg-surface);
  border-color: var(--color-border);
  color: var(--color-text-primary);
}
.app-btn--secondary:hover:not(:disabled) { background: var(--color-brand-100); }

.app-btn--ghost {
  background: transparent;
  color: var(--color-brand-600);
}
.app-btn--ghost:hover:not(:disabled) { background: var(--color-brand-100); }

.spinner {
  width: 14px;
  height: 14px;
  border: 2px solid currentColor;
  border-right-color: transparent;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

/* 跟 AppNavbar.vue 的 .nav-tooltip 是同一套做法：預設藏起來，hover/focus 才滑出提示文字 */
.app-btn-tooltip {
  position: absolute;
  bottom: calc(100% + 8px);
  left: 50%;
  transform: translateX(-50%) translateY(4px);
  padding: 5px 10px;
  border-radius: var(--radius-sm);
  background: var(--color-brand-900);
  color: var(--color-text-inverse);
  font-size: var(--font-size-xs);
  font-weight: 600;
  white-space: nowrap;
  opacity: 0;
  pointer-events: none;
  transition: opacity var(--transition-fast), transform var(--transition-fast);
  z-index: 200;
}
.app-btn--icon-only:hover .app-btn-tooltip,
.app-btn--icon-only:focus-visible .app-btn-tooltip {
  opacity: 1;
  transform: translateX(-50%) translateY(0);
}
</style>
