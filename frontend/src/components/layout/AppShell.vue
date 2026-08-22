<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import AppNavbar from './AppNavbar.vue'
import AppToast from '../common/AppToast.vue'

const router = useRouter()
const mainRef = ref(null)

// focus-on-route-change 準則：每次換頁，把鍵盤/螢幕閱讀器焦點移到內容區開頭，
// 不然使用者按 Tab 還是會停在上一頁捲動到的位置，體驗會很奇怪
router.afterEach(() => {
  // 用 nextTick 等新頁面內容渲染完成後再 focus，這裡用 requestAnimationFrame 簡化等待一個畫面更新週期
  // { preventScroll: true }：避免 focus() 觸發瀏覽器預設的「捲動到可視範圍」行為，這裡只是想搬焦點、不是想搬畫面
  requestAnimationFrame(() => mainRef.value?.focus({ preventScroll: true }))
})
</script>

<template>
  <div class="shell">
    <!-- Skip link：預設藏在畫面外，鍵盤使用者按第一個 Tab 就會看到，可以直接跳過選單 -->
    <a href="#main-content" class="skip-link">跳到主要內容</a>

    <!-- 外層「畫框」：整個系統（選單＋內容）包在一個淺色的托盤裡，四周露出頁面底色的邊界，
         呼應參考圖裡「整個介面像放在一個資料夾/畫框裡」的感覺。托盤底色刻意跟裡面的白色卡片（Navbar/表格）不同，
         不然兩層都是白色卡疊在一起會糊成一片，看不出「外層」的存在。 -->
    <div class="shell-frame">
      <!-- 側邊欄跟獨立的警示圖示都併進 Navbar 的橫向選單了（見 AppNavbar.vue），
           版面回到單欄：Navbar 貼齊畫框頂端（sticky），下面是內容區 -->
      <div class="shell-main">
        <AppNavbar />
        <main id="main-content" ref="mainRef" class="content" tabindex="-1">
          <RouterView v-slot="{ Component, route }">
            <!-- 用 route.path 當 key，確保切換頁面（含帶參數的路由）時元件會重新建立，資料不會殘留 -->
            <component :is="Component" :key="route.path" />
          </RouterView>
        </main>
      </div>
    </div>

    <AppToast />                                              <!-- 全站共用的通知訊息，固定在右下角 -->
  </div>
</template>

<style scoped>
.shell {
  min-height: 100dvh;                                         /* 用 dvh 而不是 vh，行動裝置網址列收合時不會跳動 */
}

.shell-frame {
  /* 這個底色本來只跟著內容高度走，內容短的頁面（例如警示列表沒幾筆）底色就短一截，
     頁面之間切換時底色範圍忽大忽小、看起來很怪。改成至少撐滿一個畫面高度（扣掉自己上下的 margin），
     內容不夠長時底色也會填滿整個可視範圍；內容真的比一個畫面還長，還是可以繼續往下延伸，不會被裁掉。 */
  min-height: calc(100dvh - 2 * var(--space-5));
  max-width: 1328px;                                          /* 比內容欄寬一點，四周留一圈可見的頁面底色，形成「畫框」感 */
  margin: var(--space-5) auto;
  padding: var(--space-4);
  background: var(--color-brand-100);                         /* 托盤底色跟裡面的白色卡片（Navbar/表格）拉開，兩層才分得清楚 */
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-card);
}

.shell-main {
  display: flex;
  flex-direction: column;
}

.content {
  flex: 1;
  padding-bottom: var(--space-2);
  width: 100%;
}
.content:focus {
  outline: none;                                              /* 這裡的 focus 只是給程式跳轉用，不需要顯示外框 */
}

.skip-link {
  position: fixed;
  top: -48px;                                                 /* 平常藏在畫面外，不佔版面也不擋到滑鼠操作 */
  left: var(--space-4);
  z-index: 2000;
  padding: var(--space-2) var(--space-4);
  background: var(--color-brand-900);
  color: var(--color-text-inverse);
  border-radius: var(--radius-sm);
  transition: top var(--transition-fast);
}
.skip-link:focus {
  top: var(--space-4);                                        /* 鍵盤 Tab 到這個連結時才滑入畫面內 */
}

@media (max-width: 1024px) {
  .content { padding-left: 0; padding-right: 0; }
}
</style>
