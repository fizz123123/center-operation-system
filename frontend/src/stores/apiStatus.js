import { defineStore } from 'pinia'
import { ref } from 'vue'

// 記錄「目前資料是不是假資料（Mock）」的全域狀態。
//
// 背景：後端（Member A 負責）目前還只有 Spring Boot 專案骨架，Controller/Service 都還沒寫，
// 所以前端呼叫 /api/... 現在一定會失敗。為了讓 UI 可以先做、先демо，
// src/api/http.js 在真實 API 呼叫失敗時，會 fallback 用本地假資料頂著，
// 並把這裡的 usingMock 設為 true，讓 Navbar 顯示一個提示徽章，
// 誠實告知現在看到的是示範資料、不是真的資料庫內容（口試被問到才不會被誤會是造假）。
//
// 等後端 API 真的完成、回應成功，usingMock 會自動變回 false，畫面完全不用改程式碼。
export const useApiStatusStore = defineStore('apiStatus', () => {
  const usingMock = ref(false)                                // 是否曾經 fallback 到假資料
  const lastErrorMessage = ref('')                            // 最近一次真實 API 失敗的原因（除錯用）

  function reportMockFallback(message) {
    usingMock.value = true
    lastErrorMessage.value = message
  }

  function reportRealApiSuccess() {
    usingMock.value = false
  }

  return { usingMock, lastErrorMessage, reportMockFallback, reportRealApiSuccess }
})
