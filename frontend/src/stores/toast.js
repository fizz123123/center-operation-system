import { defineStore } from 'pinia'
import { ref } from 'vue'

// 全站共用的「通知訊息」狀態。
// 任何頁面呼叫 API 成功/失敗後，都呼叫這裡的方法跳出一則提示，
// 不用每個頁面自己刻一套 toast 邏輯。
export const useToastStore = defineStore('toast', () => {
  const toasts = ref([])                                    // 目前畫面上顯示的所有提示訊息
  let uid = 0

  function push(message, type = 'info', duration = 4000) {
    const id = ++uid
    toasts.value.push({ id, message, type })                 // type: 'success' | 'error' | 'info'

    // 提示訊息 3~5 秒後自動消失（toast-dismiss 準則），使用者不用手動關閉
    window.setTimeout(() => dismiss(id), duration)
    return id
  }

  function dismiss(id) {
    toasts.value = toasts.value.filter((t) => t.id !== id)
  }

  // 語意化的快捷方法，頁面端不用自己記 type 字串
  const success = (msg) => push(msg, 'success')
  const error = (msg) => push(msg, 'error', 6000)             // 錯誤訊息多留 2 秒，讓使用者看清楚原因

  return { toasts, push, dismiss, success, error }
})
