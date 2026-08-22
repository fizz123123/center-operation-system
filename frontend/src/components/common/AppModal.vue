<script setup>
import { onMounted, onUnmounted, ref } from 'vue'
import Icon from './Icon.vue'

const props = defineProps({
  title: { type: String, required: true },
})
const emit = defineEmits(['close'])                          // 父層監聽 close 事件來關閉 Modal（v-if 控制顯示）

const dialogRef = ref(null)

// 鍵盤使用者按 Esc 可以關閉 Modal（modal-escape 準則），
// 這裡監聽整個 window 而不是只有 Modal 內部，避免焦點不在 Modal 裡面時按 Esc 沒反應
function handleKeydown(event) {
  if (event.key === 'Escape') emit('close')
}

onMounted(() => {
  window.addEventListener('keydown', handleKeydown)
  // Modal 開啟時把焦點移進對話框，讓螢幕閱讀器/鍵盤使用者知道焦點已經跳到這裡
  dialogRef.value?.focus()
})
onUnmounted(() => {
  window.removeEventListener('keydown', handleKeydown)
})
</script>

<template>
  <Teleport to="body">
    <!-- 遮罩：點擊背景等同按下關閉，滑鼠使用者的快捷方式 -->
    <div class="overlay" @click.self="emit('close')">
      <div
        ref="dialogRef"
        class="dialog"
        role="dialog"
        aria-modal="true"
        :aria-label="title"
        tabindex="-1"
      >
        <header class="dialog-header">
          <h2>{{ title }}</h2>
          <button type="button" class="icon-btn" aria-label="關閉視窗" @click="emit('close')">
            <Icon name="close" :size="18" />
          </button>
        </header>

        <div class="dialog-body">
          <slot />                                            <!-- 表單內容由呼叫端提供 -->
        </div>
      </div>
    </div>
  </Teleport>
</template>

<style scoped>
.overlay {
  position: fixed;
  inset: 0;                                                   /* 鋪滿整個視窗 */
  background: rgba(47, 62, 70, 0.45);                         /* 用品牌炭黑當遮罩色，而不是純黑，跟整體色調一致 */
  display: grid;
  place-items: center;
  padding: var(--space-4);
  z-index: var(--z-modal);
}

.dialog {
  width: min(480px, 100%);
  max-height: 90vh;
  overflow-y: auto;
  background: var(--color-bg-surface);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-modal);
}
.dialog:focus { outline: none; }                              /* Modal 容器本身不需要顯示焦點框，內部的表單元件才需要 */

.dialog-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-4) var(--space-5);
  border-bottom: 1px solid var(--color-border);
  position: sticky;
  top: 0;
  background: var(--color-bg-surface);
}
.dialog-header h2 {
  margin: 0;
  font-size: var(--font-size-lg);
}

.icon-btn {
  display: grid;
  place-items: center;
  width: 32px;
  height: 32px;
  border-radius: var(--radius-sm);
  background: transparent;
  border: none;
  color: var(--color-text-secondary);
}
.icon-btn:hover { background: var(--color-brand-100); }

.dialog-body { padding: var(--space-5); }

/* Modal 進出場動畫：從觸發位置附近淡入 + 輕微縮放，符合 modal-motion 準則 */
.overlay { animation: fade-in var(--transition-base); }
.dialog { animation: pop-in var(--transition-base); }
@keyframes fade-in { from { opacity: 0; } to { opacity: 1; } }
@keyframes pop-in { from { opacity: 0; transform: scale(0.96) translateY(8px); } to { opacity: 1; transform: none; } }
</style>
