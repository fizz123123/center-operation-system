<script setup>
import { onMounted } from 'vue'
import { useRoute, RouterLink } from 'vue-router'
import { storeToRefs } from 'pinia'
import Icon from '../common/Icon.vue'
import { useAlertsStore } from '../../stores/alerts.js'

// 選單欄從左側直向側邊欄，改成這裡的橫向選單——整個導覽（含警示）現在都在 Navbar 裡，
// 不再另外拆一個側邊欄／獨立的警示圖示元件出去。
// Navbar 不是 sticky：往下捲動頁面內容時，Navbar 會跟著正常捲走（不固定在頂端）。
// 試過幾種「固定在頂端」的做法（整塊 sticky、只收合統計列、淡出淡入），
// 都會讓固定的區塊擋住底下的清單內容，所以最後決定回到最單純的做法：跟著頁面內容一起捲動就好。
const route = useRoute()

// 學習進度管理（Module 3）原本有獨立分頁，跟「人員詳情」下面的學習紀錄區塊功能完全重複
// （註冊課程／更新狀態／查詢紀錄都一樣，只是入口不同），所以拿掉獨立分頁，
// 統一從人員列表點進「人員詳情」處理，不需要再另外維護一份重複的 UI
const NAV_ITEMS = [
  { to: '/', label: 'Dashboard', icon: 'grid' },              // Module 4：總覽統計
  { to: '/people', label: '人員管理', icon: 'users' },         // Module 1 ＋ Module 3（學習紀錄併在人員詳情頁裡）
  { to: '/courses', label: '課程管理', icon: 'book' },         // Module 2
  { to: '/learning-path', label: '學習路徑', icon: 'route' },  // Module 5：Graph 視覺化
]

const alertsStore = useAlertsStore()
const { totalCount: alertsTotalCount } = storeToRefs(alertsStore)
onMounted(() => alertsStore.load())
</script>

<template>
  <header class="navbar">
    <div class="navbar-top">
      <div class="navbar-brand">
        <span class="brand-mark" aria-hidden="true"><Icon name="hub" :size="18" /></span>
        <div class="navbar-titles">
          <span class="brand-name">中心營運分析系統</span>
          <h1 class="page-title">{{ route.meta.title }}</h1>
        </div>
      </div>

      <!-- 橫向選單：跟原本側邊欄一樣是純圖示、hover/focus 才彈出文字提示，只是排列方向從直向改橫向，
           提示文字的彈出方向也要跟著從「往右」改成「往下」，不然橫排的圖示提示會互相疊在一起 -->
      <nav class="nav-row" aria-label="主要導覽">
        <RouterLink
          v-for="item in NAV_ITEMS"
          :key="item.to"
          :to="item.to"
          class="nav-item"
          active-class="nav-item--active"
          exact-active-class="nav-item--active"
          :aria-label="item.label"
        >
          <Icon :name="item.icon" :size="18" />
          <span class="nav-tooltip" aria-hidden="true">{{ item.label }}</span>
        </RouterLink>

        <!-- 警示合併成選單裡的最後一個圖示，不再是另外浮動的獨立按鈕 -->
        <RouterLink
          to="/alerts"
          class="nav-item nav-item--alert"
          active-class="nav-item--active"
          :aria-label="alertsTotalCount > 0 ? `警示列表，${alertsTotalCount} 則待處理` : '警示列表'"
        >
          <Icon name="alert-triangle" :size="18" />
          <span v-if="alertsTotalCount > 0" class="alert-dot" aria-hidden="true"></span>
          <span class="nav-tooltip" aria-hidden="true">警示列表</span>
        </RouterLink>
      </nav>
    </div>

    <!-- 頁面專屬的小型統計數字（例如人員管理的「學員總數／在學／停用」）透過 Teleport 塞進這裡顯示，
         不是每個頁面都有東西可以放，沒有內容時這一列不佔空間（見下面 :not(:empty) 的寫法）。
         靠右對齊，跟上面選單圖示同一側。 -->
    <div id="navbar-stats-target" class="navbar-stats"></div>
  </header>
</template>

<style scoped>
.navbar {
  /* 不是 sticky：跟著頁面內容一起捲動，往下捲動時會正常捲出畫面，不會固定佔住頂端擋住底下清單。 */
  display: flex;
  flex-direction: column;                                     /* 直向排列：上面標題+選單那排，下面是頁面統計數字（有才顯示） */
  min-height: var(--layout-navbar-height);
  padding: var(--space-3) var(--space-6);
  margin-bottom: var(--space-4);
  background: var(--color-bg-surface);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-card);
}

.navbar-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-4);
}

.navbar-stats {
  display: flex;
  align-items: center;
  justify-content: flex-end;                                   /* 靠右對齊，跟上面選單圖示同一側 */
  gap: var(--space-2);
  flex-wrap: wrap;
}
.navbar-stats:not(:empty) {
  margin-top: var(--space-3);
  padding-top: var(--space-3);
  border-top: 1px solid var(--color-border);                   /* 用一條線把統計列跟上面的標題/選單列分開，只有真的有統計數字時才畫 */
}

.navbar-brand {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  min-width: 0;
}

.brand-mark {
  display: grid;
  place-items: center;
  width: 36px;
  height: 36px;
  flex-shrink: 0;
  border-radius: var(--radius-md);
  background: linear-gradient(145deg, var(--color-brand-400), var(--color-brand-600));
  color: var(--color-text-inverse);
}

.navbar-titles {
  display: flex;
  flex-direction: column;
  gap: 1px;
  min-width: 0;
}

.brand-name {
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 0.03em;
  color: var(--color-text-muted);
}

.page-title {
  margin: 0;
  font-size: var(--font-size-lg);
  font-weight: 700;
}

.nav-row {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-shrink: 0;
}

.nav-item {
  position: relative;                                         /* 給 .nav-tooltip 做定位基準 */
  display: grid;
  place-items: center;
  width: 40px;
  height: 40px;
  border-radius: var(--radius-pill);
  color: var(--color-text-muted);
  transition: background var(--transition-fast), color var(--transition-fast);
}
.nav-item:hover {
  background: var(--color-brand-100);
  color: var(--color-text-primary);
  text-decoration: none;
}
.nav-item--active {
  /* 白字 on brand-900 對比 11.06:1，橫向選單裡最顯眼也最安全的組合，跟原本側邊欄的選中樣式一致 */
  background: var(--color-brand-900);
  color: var(--color-text-inverse);
}

.alert-dot {
  position: absolute;
  top: 6px;
  right: 6px;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--color-accent-warn);
  border: 2px solid var(--color-bg-surface);                  /* 白色描邊讓紅點在圖示上更清楚 */
}

/* 提示文字改成往下彈出（原本側邊欄是直向排列、提示往右彈；現在橫向排列，往右彈會跟隔壁圖示疊在一起） */
.nav-tooltip {
  position: absolute;
  top: calc(100% + 8px);
  left: 50%;
  transform: translateX(-50%) translateY(-4px);
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
.nav-item:hover .nav-tooltip,
.nav-item:focus-visible .nav-tooltip {
  opacity: 1;
  transform: translateX(-50%) translateY(0);
}
</style>
