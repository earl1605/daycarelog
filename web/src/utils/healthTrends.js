import { classifyNutritionalStatus } from './nutritionalStatus'
import { toLocalDateString } from './date'

// Walks each active child's health-record history and re-derives their nutritional
// status as of each of the last `monthsCount` month-ends, so Dashboard and Reports can
// share one trend computation instead of drifting apart. Uses TODAY's active-children
// set retroactively for past months too, since enrollment status isn't tracked historically.
export function computeNutritionalTrend(activeChildren, healthRecords, monthsCount = 6) {
  const now = new Date()
  const monthEnds = Array.from({ length: monthsCount }, (_, i) =>
    new Date(now.getFullYear(), now.getMonth() - (monthsCount - 1 - i) + 1, 0))

  return monthEnds.map(monthEnd => {
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
}

// Current-snapshot immunization detail: each active child bucketed into exactly one of
// Fully/Partially/Not Started Immunized (a valid part-to-whole split, unlike per-vaccine
// counts which double-count a child across every vaccine they've had), plus the granular
// per-vaccine coverage counts for the detail list underneath the donut.
export function computeImmunizationDetail(activeChildren, immunizations, schedule) {
  const totalExpectedDoses = schedule.reduce((sum, v) => sum + v.expectedDoses, 0)

  const buckets = { 'Fully Immunized': 0, 'Partially Immunized': 0, 'Not Started': 0 }
  activeChildren.forEach(c => {
    const givenDoses = schedule.reduce((sum, v) => {
      const doses = immunizations.filter(im => im.childId === c.id && im.vaccineName === v.name).length
      return sum + Math.min(doses, v.expectedDoses)
    }, 0)
    if (givenDoses === 0) buckets['Not Started']++
    else if (givenDoses >= totalExpectedDoses) buckets['Fully Immunized']++
    else buckets['Partially Immunized']++
  })

  const perVaccine = schedule.map(v => {
    const covered = activeChildren.filter(c =>
      immunizations.filter(im => im.childId === c.id && im.vaccineName === v.name).length >= v.expectedDoses
    ).length
    return { vaccine: v.name, covered, total: activeChildren.length }
  })

  return {
    buckets: Object.entries(buckets).map(([name, value]) => ({ name, value })),
    perVaccine,
  }
}
