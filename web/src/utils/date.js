// Date.toISOString() converts to UTC first, which silently rolls the date back
// a day for any timezone ahead of UTC (e.g. Philippines, UTC+8) whenever the local
// time is before the UTC offset (before 8am PHT). Use this instead everywhere a
// "today" or "this date" YYYY-MM-DD string is needed from a local Date object.
export function toLocalDateString(date = new Date()) {
  const year  = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day   = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}
