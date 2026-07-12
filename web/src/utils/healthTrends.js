import { classifyNutritionalStatus } from './nutritionalStatus'
import { toLocalDateString } from './date'

// Walks each active child's health-record/immunization history and re-derives their
// status as of each of the last `monthsCount` month-ends, so Dashboard and Reports can
// share one trend computation instead of drifting apart. Uses TODAY's active-children
// set retroactively for past months too, since enrollment status isn't tracked historically.
export function computeHealthTrends(activeChildren, healthRecords, immunizations, schedule, monthsCount = 6) {
  const now = new Date()
  const monthEnds = Array.from({ length: monthsCount }, (_, i) =>
    new Date(now.getFullYear(), now.getMonth() - (monthsCount - 1 - i) + 1, 0))

  const nutritionalTrend = monthEnds.map(monthEnd => {
    const cutoff = toLocalDateString(monthEnd)
    const counts = { Normal: 0, Underweight: 0, 'Severely Underweight': 0, Overweight: 0 }
    let classified = 0
    activeChildren.forEach(c => {
      const upToCutoff = healthRecords.filter(r => r.childId === c.id && r.measurementDate <= cutoff)
      if (upToCutoff.length === 0) return
      const latest = upToCutoff.reduce((a, b) => (a.measurementDate > b.measurementDate ? a : b))
      const status = classifyNutritionalStatus(latest.weightKg, c.dateOfBirth, c.sex)
      if (status?.label in counts) { counts[status.label]++; classified++ }
    })
    const pct = key => (classified > 0 ? Math.round((counts[key] / classified) * 100) : 0)
    return {
      month: monthEnd.toLocaleDateString('en-PH', { month: 'short' }),
      Normal: pct('Normal'), Underweight: pct('Underweight'),
      'Severely Underweight': pct('Severely Underweight'), Overweight: pct('Overweight'),
    }
  })

  const immunizationTrend = monthEnds.map(monthEnd => {
    const cutoff = toLocalDateString(monthEnd)
    const perVaccineCoverage = schedule.map(v => {
      if (activeChildren.length === 0) return 0
      const covered = activeChildren.filter(c => {
        const doses = immunizations.filter(im => im.childId === c.id && im.vaccineName === v.name && im.dateGiven <= cutoff).length
        return doses >= v.expectedDoses
      }).length
      return covered / activeChildren.length
    })
    const avg = perVaccineCoverage.length > 0
      ? Math.round((perVaccineCoverage.reduce((a, b) => a + b, 0) / perVaccineCoverage.length) * 100)
      : 0
    return { month: monthEnd.toLocaleDateString('en-PH', { month: 'short' }), coverage: avg }
  })

  return { nutritionalTrend, immunizationTrend }
}
