// Heuristic password-strength scoring (0-5): length and character-variety
// checks, no dictionary/breach lookups. Good enough to nudge users away from
// weak passwords without a network call. Same status-severity colors used
// elsewhere in the app (NutritionalStatusBadge, immunization coverage).
export function getPasswordStrength(password) {
  if (!password) return { score: 0, label: '', color: null }

  let score = 0
  if (password.length >= 8) score++
  if (password.length >= 12) score++
  if (/[a-z]/.test(password) && /[A-Z]/.test(password)) score++
  if (/\d/.test(password)) score++
  if (/[^a-zA-Z0-9]/.test(password)) score++

  if (password.length < 8) return { score, label: 'Too short', color: '#d03b3b' }
  if (score <= 2) return { score, label: 'Weak', color: '#d03b3b' }
  if (score <= 3) return { score, label: 'Medium', color: '#fab219' }
  return { score, label: 'Strong', color: '#0ca30c' }
}
