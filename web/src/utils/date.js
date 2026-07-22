export function toLocalDateString(date = new Date()) {
  const year  = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day   = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

// Backend serializes LocalDateTime with no timezone suffix (e.g. "2026-07-22T08:51:33").
// Those values are UTC wall-clock instants, so treat them as such by appending "Z" before
// letting the browser parse/convert them - otherwise `new Date(...)` reads the string as
// already being in the browser's local timezone, which is wrong by the server's UTC offset.
function parseUtc(isoString) {
  return new Date(isoString.endsWith('Z') ? isoString : `${isoString}Z`)
}

export function formatRelativeTime(isoString) {
  const date = parseUtc(isoString)
  const diffSec = Math.round((Date.now() - date.getTime()) / 1000)
  if (diffSec < 60) return 'just now'
  const diffMin = Math.round(diffSec / 60)
  if (diffMin < 60) return `${diffMin}m ago`
  const diffHr = Math.round(diffMin / 60)
  if (diffHr < 24) return `${diffHr}h ago`
  const diffDay = Math.round(diffHr / 24)
  if (diffDay < 7) return `${diffDay}d ago`
  return date.toLocaleDateString('en-PH', { timeZone: 'Asia/Manila', month: 'short', day: 'numeric', year: 'numeric' })
}

export function formatManilaDateTime(isoString) {
  return parseUtc(isoString).toLocaleString('en-PH', {
    timeZone: 'Asia/Manila', month: 'short', day: 'numeric', year: 'numeric', hour: 'numeric', minute: '2-digit',
  })
}
