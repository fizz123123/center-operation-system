const AUTO_MESSAGE_PREFIX = /^\[AUTO\]\s*/

// [AUTO] 是後端辨識系統警示的內部標記，不直接呈現在使用者介面。
export function toDisplayAlertMessage(message) {
  if (typeof message !== 'string') return ''
  return message.replace(AUTO_MESSAGE_PREFIX, '')
}
