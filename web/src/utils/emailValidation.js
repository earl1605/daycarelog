const MAX_LENGTH = 254
const LOCAL_PART = /^[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+)*$/
const DOMAIN_LABEL = /^[A-Za-z0-9]([A-Za-z0-9-]{0,61}[A-Za-z0-9])?$/
const TLD = /^[A-Za-z]{2,}$/

export function validateEmailFormat(rawEmail) {
  if (!rawEmail || !rawEmail.trim()) return invalid('Email address is required.')
  const email = rawEmail.trim()

  if (email.length > MAX_LENGTH) return invalid('Email address is too long.')
  if (/\s/.test(email)) return invalid('Email address cannot contain spaces.')

  const at = email.indexOf('@')
  if (at <= 0 || at !== email.lastIndexOf('@') || at === email.length - 1) {
    return invalid('Please enter a valid email address.')
  }

  const local = email.slice(0, at)
  const domain = email.slice(at + 1).toLowerCase()

  if (local.startsWith('.') || local.endsWith('.')) return invalid('Email address cannot start or end with a dot.')
  if (local.includes('..')) return invalid('Email address cannot contain consecutive dots.')
  if (!LOCAL_PART.test(local)) return invalid('Please enter a valid email address.')

  if (domain.startsWith('.') || domain.endsWith('.') || domain.startsWith('-') || domain.endsWith('-')) {
    return invalid('Please enter a valid email address.')
  }
  if (domain.includes('..')) return invalid('Email address cannot contain consecutive dots.')

  const labels = domain.split('.')
  if (labels.length < 2) return invalid('Email address is missing a domain extension (e.g. .com).')
  for (const label of labels) {
    if (!label || !DOMAIN_LABEL.test(label)) return invalid('Please enter a valid email address.')
  }
  const tld = labels[labels.length - 1]
  if (!TLD.test(tld)) return invalid('Email address is missing a valid domain extension (e.g. .com).')

  return { valid: true, message: null }
}

function invalid(message) {
  return { valid: false, message }
}

const TYPO_DOMAIN_MAP = {
  'gmial.com': 'gmail.com',
  'gnail.com': 'gmail.com',
  'gmai.com': 'gmail.com',
  'gmail.co': 'gmail.com',
  'yahooo.com': 'yahoo.com',
  'yaho.com': 'yahoo.com',
  'hotmial.com': 'hotmail.com',
  'hotmil.com': 'hotmail.com',
  'outlok.com': 'outlook.com',
  'outllok.com': 'outlook.com',
}

export function getEmailTypoSuggestion(rawEmail) {
  if (!rawEmail || !rawEmail.includes('@')) return null
  const at = rawEmail.lastIndexOf('@')
  const local = rawEmail.slice(0, at)
  const domain = rawEmail.slice(at + 1).trim().toLowerCase()
  const corrected = TYPO_DOMAIN_MAP[domain]
  if (!corrected || corrected === domain) return null
  return `${local}@${corrected}`
}
