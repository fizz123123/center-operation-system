/**
 * 把後端回傳的「狀態代碼」統一轉換成畫面要顯示的「文字 + 顏色 + 圖示」。
 * 集中寫在這裡的原因：狀態徽章在 Person / Course / Enrollment / Dashboard 好幾個頁面都會用到，
 * 如果每個頁面各自寫一份 if/else 對照表，改顏色或改文案時會漏改，所以抽成共用的對照表。
 *
 * 每個 meta 物件都同時給「顏色」跟「文字/圖示」，符合 color-not-only 準則
 * （不能只靠顏色分辨狀態，色盲使用者或黑白列印時會看不出差異）。
 */

// ---- 人員狀態（PersonStatus）----
// 在學原本用 brand-600（跟按鈕/連結同一色），容易被誤認成可點擊的東西，後來改成 charcoal（brand-900）——
// 但 charcoal 文字 vs 灰階文字，兩個徽章都是「淡色底＋深色字」的同一種樣式，只差色相，
// 放在表格裡快速掃過去時區隔感不夠強（這個 8 色色票能當小字用的深色只有 3 個，色相選擇本來就有限）。
// 改成「形狀語言」而不只是「顏色」做區隔：在學＝實心徽章（深底白字，視覺份量重、一眼就是「開」的狀態），
// 停用＝維持原本的淡色徽章（視覺份量輕、一眼就是「關/非現用」狀態）。實心 vs 淡色的對比，
// 比單純換色相更直覺，也符合 color-not-only 準則（形狀/圖示不同，不是只靠顏色分辨）。
const PERSON_STATUS = {
  ACTIVE: { label: '在學', color: 'var(--color-brand-900)', icon: 'check', solid: true },
  INACTIVE: { label: '停用', color: 'var(--color-text-muted)', icon: 'close', solid: false },
}
export function personStatusMeta(status) {
  return PERSON_STATUS[status] ?? { label: status ?? '未知', color: 'var(--color-text-muted)', icon: 'clock' }
}
export const PERSON_STATUS_OPTIONS = Object.keys(PERSON_STATUS)        // 給「編輯人員」表單的狀態下拉選單用

// ---- 學習狀態（EnrollmentStatus）----
const ENROLLMENT_STATUS = {
  NOT_STARTED: { label: '尚未開始', color: 'var(--color-status-not-started)', icon: 'clock' },
  IN_PROGRESS: { label: '進行中', color: 'var(--color-status-in-progress)', icon: 'refresh' },
  COMPLETED: { label: '已完成', color: 'var(--color-status-completed)', icon: 'check' },
}
export function enrollmentStatusMeta(status) {
  return ENROLLMENT_STATUS[status] ?? { label: status ?? '未知', color: 'var(--color-text-muted)', icon: 'clock' }
}
export const ENROLLMENT_STATUS_OPTIONS = Object.keys(ENROLLMENT_STATUS)  // 給下拉選單用

// ---- 警示優先權（Alert priority：3 高 / 2 中 / 1 低）----
const ALERT_PRIORITY = {
  3: { label: '高', color: 'var(--color-priority-high)', icon: 'alert-triangle' },
  2: { label: '中', color: 'var(--color-priority-medium)', icon: 'alert-triangle' },
  1: { label: '低', color: 'var(--color-priority-low)', icon: 'alert-triangle' },
}
export function alertPriorityMeta(priority) {
  return ALERT_PRIORITY[priority] ?? { label: `優先權 ${priority}`, color: 'var(--color-text-muted)', icon: 'alert-triangle' }
}
