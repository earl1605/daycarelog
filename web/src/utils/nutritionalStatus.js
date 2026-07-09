export function getAgeInMonths(dateOfBirth) {
  const birth = new Date(dateOfBirth)
  const now = new Date()
  return (now.getFullYear() - birth.getFullYear()) * 12 + (now.getMonth() - birth.getMonth())
}

export function getAgeInYears(dateOfBirth) {
  const birth = new Date(dateOfBirth)
  const now = new Date()
  let age = now.getFullYear() - birth.getFullYear()
  const m = now.getMonth() - birth.getMonth()
  if (m < 0 || (m === 0 && now.getDate() < birth.getDate())) age--
  return age
}

export function formatAge(dateOfBirth) {
  const months = getAgeInMonths(dateOfBirth)
  if (months < 24) return `${months} month${months !== 1 ? 's' : ''}`
  const years = Math.floor(months / 12)
  const rem = months % 12
  if (rem === 0) return `${years} yr${years !== 1 ? 's' : ''}`
  return `${years} yr${years !== 1 ? 's' : ''} ${rem} mo`
}

export function classifyNutritionalStatus(weightKg, dateOfBirth, sex) {
  if (!weightKg || !dateOfBirth) return { label: 'Unknown', color: 'gray' }
  const months = getAgeInMonths(dateOfBirth)
  if (months < 0 || months > 60) return { label: 'Out of Range', color: 'gray' }

  const medianTable = {
    male:   [3.3,4.5,5.6,6.4,7.0,7.5,7.9,8.3,8.6,8.9,9.2,9.4,9.6,10.0,10.3,10.6,10.9,11.1,11.4,11.6,11.8,12.0,12.2,12.4,12.6,12.8,13.0,13.2,13.4,13.6,13.8,14.0,14.2,14.4,14.6,14.8,14.9,15.1,15.3,15.5,15.7,15.9,16.1,16.2,16.4,16.6,16.8,17.0,17.2,17.4,17.5,17.7,17.9,18.1,18.3,18.5,18.7,18.9,19.1,19.3,19.5],
    female: [3.2,4.2,5.1,5.8,6.4,6.9,7.3,7.6,7.9,8.2,8.5,8.7,8.9,9.2,9.5,9.8,10.0,10.2,10.5,10.7,10.9,11.1,11.3,11.5,11.7,11.9,12.1,12.3,12.5,12.7,12.9,13.1,13.3,13.5,13.7,13.9,14.1,14.2,14.4,14.6,14.8,15.0,15.1,15.3,15.5,15.7,15.9,16.1,16.2,16.4,16.6,16.8,17.0,17.2,17.4,17.6,17.8,18.0,18.2,18.4,18.6],
  }

  const idx = Math.min(months, 60)
  const key = sex?.toLowerCase() === 'female' ? 'female' : 'male'
  const median = medianTable[key][idx]
  const ratio = weightKg / median

  if (ratio >= 1.20) return { label: 'Overweight', color: 'yellow' }
  if (ratio >= 0.90) return { label: 'Normal', color: 'green' }
  if (ratio >= 0.75) return { label: 'Underweight', color: 'orange' }
  return { label: 'Severely Underweight', color: 'red' }
}
