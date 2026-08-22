import { createApp } from 'vue'                          // Vue 3 建立應用程式的入口函式
import { createPinia } from 'pinia'                       // Pinia：全域狀態管理（這裡只拿來放共用的 Toast 通知）
import App from './App.vue'                               // 最外層元件
import router from './router'                             // 路由設定（見 router/index.js）
import './assets/styles/base.css'                         // 全域樣式（含 reset 與設計 token）

const app = createApp(App)

app.use(createPinia())                                    // 註冊 Pinia，讓任何元件都能用 useXxxStore()
app.use(router)                                            // 註冊路由，讓 <router-view> 能運作

app.mount('#app')                                          // 掛載到 index.html 裡的 <div id="app">
