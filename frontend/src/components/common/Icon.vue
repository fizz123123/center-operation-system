<script setup>
// 全站統一的圖示元件：所有圖示都用同一套 24x24、線寬 1.75 的 SVG 手繪路徑，
// 不使用 emoji（emoji 在不同作業系統/瀏覽器長得不一樣，也沒辦法用 CSS 控制顏色跟粗細）。
// 用法：<Icon name="grid" />，需要語意時（例如純圖示按鈕）外層要自己補 aria-label。

const props = defineProps({
  name: { type: String, required: true },                 // 圖示名稱，對應下面 PATHS 的 key
  size: { type: [Number, String], default: 20 },          // 圖示大小（px）
})

// 每個圖示是一組 <path>/<line>/<circle> 的 SVG 內容字串，直接用 v-html 插入，
// 圖示新增時只要在這裡加一筆，元件本身完全不用改。
const PATHS = {
  grid: '<rect x="3" y="3" width="7" height="7" rx="1.5"/><rect x="14" y="3" width="7" height="7" rx="1.5"/><rect x="3" y="14" width="7" height="7" rx="1.5"/><rect x="14" y="14" width="7" height="7" rx="1.5"/>',
  // 品牌 Logo 用：中心節點連著四個衛星節點，象徵「中心」把各模組（人員/課程/進度/路徑）串起來
  hub: '<circle cx="12" cy="12" r="3"/><circle cx="12" cy="4" r="1.6"/><circle cx="12" cy="20" r="1.6"/><circle cx="4" cy="12" r="1.6"/><circle cx="20" cy="12" r="1.6"/><path d="M12 8.2V9M12 15v.8M8.2 12H9M15 12h.8"/>',
  users: '<circle cx="9" cy="8" r="3.25"/><path d="M3 20c0-3.3 2.7-6 6-6s6 2.7 6 6"/><circle cx="17" cy="9" r="2.5"/><path d="M15.5 14.2c2.6.4 4.5 2.6 4.5 5.3"/>',
  book: '<path d="M4 5.5C4 4.7 4.7 4 5.5 4H12v16H5.5A1.5 1.5 0 0 1 4 18.5v-13Z"/><path d="M20 5.5c0-.8-.7-1.5-1.5-1.5H12v16h6.5a1.5 1.5 0 0 0 1.5-1.5v-13Z"/>',
  route: '<circle cx="6" cy="6" r="2.25"/><circle cx="18" cy="18" r="2.25"/><path d="M6 8.25V13a3 3 0 0 0 3 3h6"/>',
  clipboard: '<rect x="5" y="4" width="14" height="17" rx="2"/><rect x="9" y="2.5" width="6" height="3" rx="1"/><path d="M8.5 11h7M8.5 15h7M8.5 19h4"/>',
  plus: '<path d="M12 5v14M5 12h14"/>',
  pencil: '<path d="M4 20h4L18.5 9.5a2 2 0 0 0 0-2.8l-1.2-1.2a2 2 0 0 0-2.8 0L4 16v4Z"/><path d="M13 6.5 17.5 11"/>',
  close: '<path d="M6 6l12 12M18 6 6 18"/>',
  check: '<path d="M5 12.5 10 17.5 19 7"/>',
  'alert-triangle': '<path d="M12 4 21.5 20H2.5L12 4Z"/><path d="M12 10v4.5"/><circle cx="12" cy="17.3" r="0.9" fill="currentColor" stroke="none"/>',
  clock: '<circle cx="12" cy="12" r="8.5"/><path d="M12 7.5V12l3 2"/>',
  'chevron-down': '<path d="M6 9l6 6 6-6"/>',
  'chevron-left': '<path d="M15 6l-6 6 6 6"/>',
  'chevron-right': '<path d="M9 6l6 6-6 6"/>',
  search: '<circle cx="10.5" cy="10.5" r="6.5"/><path d="M20 20l-4.3-4.3"/>',
  refresh: '<path d="M20 11a8 8 0 1 0-2.3 5.7"/><path d="M20 5v6h-6"/>',
  'sort-asc': '<path d="M7 15V5M4 8l3-3 3 3"/><path d="M13 6h7M13 12h5M13 18h3"/>',
  'sort-desc': '<path d="M7 5v10M4 12l3 3 3-3"/><path d="M13 6h3M13 12h5M13 18h7"/>',
  logout: '<path d="M9 4H6.5A2.5 2.5 0 0 0 4 6.5v11A2.5 2.5 0 0 0 6.5 20H9"/><path d="M15 8l4 4-4 4M19 12H9"/>',
  bell: '<path d="M6 16.5V10a6 6 0 1 1 12 0v6.5l1.4 2.3a1 1 0 0 1-.85 1.5H5.45a1 1 0 0 1-.85-1.5L6 16.5Z"/><path d="M10 20a2 2 0 0 0 4 0"/>',
  // 篩選圖示：三條橫線＋實心圓鈕，像調節滑桿，是常見的「Filters」圖示畫法
  filter: '<path d="M4 6h16"/><circle cx="8" cy="6" r="2.2" fill="currentColor" stroke="none"/><path d="M4 12h16"/><circle cx="16" cy="12" r="2.2" fill="currentColor" stroke="none"/><path d="M4 18h16"/><circle cx="11" cy="18" r="2.2" fill="currentColor" stroke="none"/>',
}
</script>

<template>
  <svg
    :width="size"
    :height="size"
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    stroke-width="1.75"
    stroke-linecap="round"
    stroke-linejoin="round"
    aria-hidden="true"
    v-html="PATHS[name] ?? ''"
  ></svg>
  <!--
    aria-hidden="true"：圖示預設視為裝飾用。
    如果某處是「純圖示按鈕」（旁邊沒有文字），呼叫端要自己在 <button> 上加 aria-label，
    而不是改這個共用元件（因為同一個圖示在不同地方語意不一定相同）。
  -->
</template>
