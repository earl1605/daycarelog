// Capitalizes the first letter of each word segment (segments split on space,
// hyphen, and apostrophe) without touching any other character. Unlike a
// full Title Case formatter, this never lowercases the rest of a word — so
// deliberate casing like "McDonald" or "DeSilva" survives untouched. Meant to
// run on every keystroke; same-length output keeps cursor position math trivial.
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
