/**
 * 簡單的 debounce：連續呼叫時，只有「停下來 wait 毫秒之後」的最後一次會真的執行。
 * 用在搜尋框上——改成伺服器端搜尋之後，如果每打一個字就打一次 API，
 * 使用者打一個詞可能會連續發出好幾支請求，浪費流量也容易讓畫面因為回應順序不一致而閃爍。
 * 沒有另外裝 lodash 之類的套件，這幾行邏輯自己寫比較划算。
 */
export function debounce(fn, wait = 400) {
  let timer = null
  return (...args) => {
    clearTimeout(timer)
    timer = setTimeout(() => fn(...args), wait)
  }
}
