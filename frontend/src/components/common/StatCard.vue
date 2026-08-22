<script setup>
import Icon from './Icon.vue'

// Dashboard 用的 KPI 卡片：一個大數字 + 標籤 + 圖示。
// 依 chart 準則「少於 4 個資料點就用 stat card，不用畫折線圖」，
// Dashboard 的四個總覽數字（總人數/總課程數/總註冊數/完成率）用這個元件呈現最合適。
defineProps({
  label: { type: String, required: true },                 // 例如「總學員數」
  value: { type: [String, Number], required: true },       // 例如 50，或 "75.5%"
  icon: { type: String, required: true },
  accent: { type: String, default: 'var(--color-brand-600)' }, // 圖示底色，讓四張卡可以有些微差異但仍在同一色系內
  loading: { type: Boolean, default: false },
  // 'default'：白底卡片（一般用）；'filled'：整張卡填滿品牌色的「主打指標」卡片。
  // 參考圖裡幾乎每個 Dashboard 都會挑一張 KPI 卡用實色塊突顯（例如 image1 的 Activity 卡），
  // 四張卡都用白底會太平、缺乏視覺重點，所以留這個 variant 讓頁面自己選一張數字最想被看到的卡片套用。
  variant: { type: String, default: 'default' },
})
</script>

<template>
  <div class="stat-card" :class="{ 'stat-card--filled': variant === 'filled' }">
    <!-- 裝飾用的角落色塊：純粹視覺豐富度，不代表任何數據，所以用 aria-hidden 排除在無障礙樹之外，
         這樣才不會被誤會成「這裡藏著看不到的圖表資訊」 -->
    <div class="stat-glow" :style="{ '--accent': accent }" aria-hidden="true"></div>

    <div class="stat-top">
      <div class="stat-icon" :style="{ '--accent': accent }">
        <Icon :name="icon" :size="20" />
      </div>
      <span class="stat-label">{{ label }}</span>
    </div>

    <!-- loading 時顯示灰色骨架條，避免顯示 0 造成使用者誤會「真的是 0 筆資料」（loading-chart 準則） -->
    <span v-if="loading" class="stat-skeleton" aria-hidden="true"></span>
    <span v-else class="stat-value num">{{ value }}</span>
  </div>
</template>

<style scoped>
.stat-card {
  position: relative;
  overflow: hidden;                                          /* 蓋住裝飾色塊超出卡片邊界的部分 */
  display: flex;
  flex-direction: column;                                     /* 改成上下排列（icon+標籤在上、大數字在下），比左右排列更有層次 */
  gap: var(--space-4);
  padding: var(--space-5);
  background: var(--color-bg-surface);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-card);
  transition: box-shadow var(--transition-base), transform var(--transition-base);
}
.stat-card:hover { box-shadow: var(--shadow-card-hover); transform: translateY(-2px); }

.stat-glow {
  position: absolute;
  top: -30px;
  right: -30px;
  width: 110px;
  height: 110px;
  border-radius: 50%;
  background: radial-gradient(circle, var(--accent) 0%, transparent 72%);
  opacity: 0.16;                                              /* 很淡的裝飾光暈，增加卡片的豐富度但不搶走數字的注意力 */
  pointer-events: none;
}

.stat-top {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  position: relative;
}

/* 實色主打卡片：用品牌色雙色漸層取代白底，圖示 chip 改成半透明白圈，文字改白色。
   對比度：白字 on 52796F(brand-600) = 4.86:1、白字 on 354F52(brand-800) = 8.78:1，兩端都通過 AA，
   漸層中間值一定落在這兩者之間，所以整個漸層範圍內白字都是安全的。 */
.stat-card--filled {
  background: linear-gradient(135deg, var(--color-brand-600), var(--color-brand-800));
  box-shadow: 0 10px 24px -6px rgba(53, 79, 82, 0.45);
}
.stat-card--filled .stat-glow { opacity: 0.25; background: radial-gradient(circle, #ffffff 0%, transparent 72%); }
.stat-card--filled .stat-icon {
  background: rgba(255, 255, 255, 0.18);
  color: var(--color-text-inverse);
}
.stat-card--filled .stat-label { color: rgba(255, 255, 255, 0.78); }
.stat-card--filled .stat-value { color: var(--color-text-inverse); }
.stat-card--filled .stat-skeleton { background: rgba(255, 255, 255, 0.25); animation: none; }

.stat-icon {
  display: grid;
  place-items: center;
  width: 40px;
  height: 40px;
  flex-shrink: 0;
  border-radius: var(--radius-md);
  background: color-mix(in srgb, var(--accent) 18%, white);
  color: var(--accent);
}

.stat-label {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
  min-width: 0;
}

.stat-value {
  font-size: var(--font-size-2xl);
  font-weight: 700;
  color: var(--color-text-primary);
  line-height: 1.1;
  position: relative;
}

.stat-skeleton {
  display: block;
  width: 64px;
  height: 28px;
  border-radius: var(--radius-sm);
  background: linear-gradient(90deg, var(--color-border) 25%, #eef1ee 50%, var(--color-border) 75%);
  background-size: 200% 100%;
  animation: shimmer 1.4s ease-in-out infinite;
}

@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}
</style>
