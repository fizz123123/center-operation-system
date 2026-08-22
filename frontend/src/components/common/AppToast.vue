<script setup>
import { storeToRefs } from 'pinia'
import { useToastStore } from '../../stores/toast.js'
import Icon from './Icon.vue'

const store = useToastStore()
const { toasts } = storeToRefs(store)

// 不同類型對應不同圖示，讓「不是只靠顏色」表達成功/失敗（color-not-only 準則）
const ICON_BY_TYPE = { success: 'check', error: 'alert-triangle', info: 'clock' }
</script>

<template>
  <!-- aria-live="polite"：讓螢幕閱讀器會讀出新訊息，但不會搶走使用者目前的操作焦點 -->
  <div class="toast-region" aria-live="polite" role="status">
    <TransitionGroup name="toast">
      <div v-for="t in toasts" :key="t.id" class="toast" :class="`toast--${t.type}`">
        <Icon :name="ICON_BY_TYPE[t.type] ?? 'clock'" :size="16" />
        <span>{{ t.message }}</span>
        <button type="button" class="toast-close" aria-label="關閉通知" @click="store.dismiss(t.id)">
          <Icon name="close" :size="14" />
        </button>
      </div>
    </TransitionGroup>
  </div>
</template>

<style scoped>
.toast-region {
  position: fixed;
  right: var(--space-5);
  bottom: var(--space-5);
  z-index: var(--z-toast);
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
  width: min(340px, calc(100vw - 2 * var(--space-5)));
}

.toast {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-3) var(--space-4);
  border-radius: var(--radius-md);
  background: var(--color-brand-900);                       /* 統一用深底，三種類型只換左側圖示/邊條顏色 */
  color: var(--color-text-inverse);
  box-shadow: var(--shadow-modal);
  border-left: 4px solid var(--color-brand-400);
}
.toast--success { border-left-color: var(--color-brand-400); }
.toast--error { border-left-color: var(--color-accent-warn); }
.toast--info { border-left-color: var(--color-accent-alert); }

.toast span { flex: 1; font-size: var(--font-size-sm); }

.toast-close {
  background: none;
  border: none;
  color: inherit;
  display: grid;
  place-items: center;
  padding: var(--space-1);
  border-radius: var(--radius-sm);
  opacity: 0.75;
}
.toast-close:hover { opacity: 1; background: rgba(255, 255, 255, 0.12); }

/* 進場/退場動畫：只動 opacity + transform，符合 transform-performance 準則（不動 layout 屬性） */
.toast-enter-active, .toast-leave-active { transition: all var(--transition-base); }
.toast-enter-from { opacity: 0; transform: translateY(8px); }
.toast-leave-to { opacity: 0; transform: translateX(24px); }
</style>
