import { flushSync } from 'react-dom'

export function capitalizeFirstLetters(value) {
  if (!value) return value

  let result = ''
  let atSegmentStart = true
  for (const char of value) {
    result += atSegmentStart ? char.toUpperCase() : char
    atSegmentStart = char === ' ' || char === '-' || char === "'"
  }
  return result
}

export function handleCapitalizedNameInput(setter) {
  return e => {
    const el = e.target
    const start = el.selectionStart
    const end = el.selectionEnd
    const transformed = capitalizeFirstLetters(el.value)
    flushSync(() => setter(transformed))
    el.setSelectionRange(start, end)
  }
}
