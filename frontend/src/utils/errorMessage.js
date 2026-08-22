// 從 axios 錯誤物件裡取出後端回傳的真正錯誤訊息（依 docs/03_api_spec.md 第 11 節的錯誤格式 { message }），
// 抓不到就退回呼叫端提供的預設文字。跟 api/http.js 的 isRealBackendError 判斷邏輯是同一套依據，
// 目的是讓「Email 重複」「課程代碼重複」這類後端真的判斷過的錯誤，能被使用者看到實際原因，
// 而不是每次都只顯示「新增失敗」這種看不出問題在哪的通用文字。
export function extractErrorMessage(error, fallback) {
  return error?.response?.data?.message ?? fallback
}
